package com.zorysa.finance.bills.service;

import com.zorysa.finance.accounts.repository.AccountRepository;
import com.zorysa.finance.bills.dto.BillResponse;
import com.zorysa.finance.bills.dto.CreateBillRequest;
import com.zorysa.finance.bills.dto.PayBillRequest;
import com.zorysa.finance.bills.dto.UpdateBillRequest;
import com.zorysa.finance.bills.entity.Bill;
import com.zorysa.finance.bills.entity.BillStatus;
import com.zorysa.finance.bills.mapper.BillMapper;
import com.zorysa.finance.bills.repository.BillRepository;
import com.zorysa.finance.categories.entity.Category;
import com.zorysa.finance.categories.repository.CategoryRepository;
import com.zorysa.finance.shared.exception.BadRequestException;
import com.zorysa.finance.shared.exception.NotFoundException;
import com.zorysa.finance.transactions.dto.CreateTransactionRequest;
import com.zorysa.finance.transactions.dto.TransactionResponse;
import com.zorysa.finance.transactions.entity.TransactionStatus;
import com.zorysa.finance.transactions.entity.TransactionType;
import com.zorysa.finance.transactions.service.TransactionService;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BillService {

    private final ObjectProvider<BillRepository> billRepository;
    private final ObjectProvider<AccountRepository> accountRepository;
    private final ObjectProvider<CategoryRepository> categoryRepository;
    private final TransactionService transactionService;
    private final BillMapper billMapper;

    public BillService(
            ObjectProvider<BillRepository> billRepository,
            ObjectProvider<AccountRepository> accountRepository,
            ObjectProvider<CategoryRepository> categoryRepository,
            TransactionService transactionService,
            BillMapper billMapper
    ) {
        this.billRepository = billRepository;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
        this.transactionService = transactionService;
        this.billMapper = billMapper;
    }

    @Transactional(readOnly = true)
    public Page<BillResponse> listBills(
            UUID userId,
            BillStatus status,
            LocalDate startDueDate,
            LocalDate endDueDate,
            UUID categoryId,
            UUID accountId,
            Boolean overdue,
            Pageable pageable
    ) {
        return billRepository().findAll(
                filterBy(userId, status, startDueDate, endDueDate, categoryId, accountId, overdue),
                pageable
        ).map(billMapper::toResponse);
    }

    @Transactional
    public BillResponse createBill(UUID userId, CreateBillRequest request) {
        validateCategory(userId, request.categoryId());
        validateAccount(userId, request.accountId());
        validateInitialStatus(request.status(), request.accountId());

        Bill bill = new Bill(
                userId,
                request.description().trim(),
                request.amount(),
                request.dueDate(),
                request.categoryId(),
                request.accountId(),
                request.status(),
                null,
                null
        );
        return billMapper.toResponse(billRepository().save(bill));
    }

    @Transactional(readOnly = true)
    public BillResponse getBill(UUID userId, UUID billId) {
        return billMapper.toResponse(findOwnedBill(userId, billId));
    }

    @Transactional
    public BillResponse updateBill(UUID userId, UUID billId, UpdateBillRequest request) {
        Bill bill = findOwnedBill(userId, billId);
        if (bill.isPaid()) {
            throw new BadRequestException("Conta paga nao pode ser editada");
        }
        validateCategory(userId, request.categoryId());
        validateAccount(userId, request.accountId());
        validateInitialStatus(request.status(), request.accountId());
        bill.updateDetails(
                request.description().trim(),
                request.amount(),
                request.dueDate(),
                request.categoryId(),
                request.accountId(),
                request.status()
        );
        return billMapper.toResponse(billRepository().save(bill));
    }

    @Transactional
    public void deleteBill(UUID userId, UUID billId) {
        Bill bill = findOwnedBill(userId, billId);
        if (bill.isPaid()) {
            throw new BadRequestException("Conta paga nao pode ser excluida");
        }
        billRepository().delete(bill);
    }

    @Transactional
    public BillResponse payBill(UUID userId, UUID billId, PayBillRequest request) {
        Bill bill = findOwnedBill(userId, billId);
        if (bill.isPaid()) {
            throw new BadRequestException("Conta ja foi paga");
        }
        validateCategory(userId, bill.getCategoryId());
        validateAccount(userId, request.accountId());

        TransactionResponse transaction = transactionService.createTransaction(
                userId,
                new CreateTransactionRequest(
                        bill.getDescription(),
                        bill.getAmount(),
                        TransactionType.EXPENSE,
                        request.paidAt(),
                        bill.getCategoryId(),
                        request.accountId(),
                        TransactionStatus.PAID,
                        "Conta a pagar: " + bill.getDescription()
                )
        );
        bill.markAsPaid(request.accountId(), request.paidAt(), transaction.id());
        return billMapper.toResponse(billRepository().save(bill));
    }

    private Specification<Bill> filterBy(
            UUID userId,
            BillStatus status,
            LocalDate startDueDate,
            LocalDate endDueDate,
            UUID categoryId,
            UUID accountId,
            Boolean overdue
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("userId"), userId));
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            if (startDueDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("dueDate"), startDueDate));
            }
            if (endDueDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("dueDate"), endDueDate));
            }
            if (categoryId != null) {
                predicates.add(criteriaBuilder.equal(root.get("categoryId"), categoryId));
            }
            if (accountId != null) {
                predicates.add(criteriaBuilder.equal(root.get("accountId"), accountId));
            }
            if (Boolean.TRUE.equals(overdue)) {
                predicates.add(criteriaBuilder.equal(root.get("status"), BillStatus.PENDING));
                predicates.add(criteriaBuilder.lessThan(root.get("dueDate"), LocalDate.now()));
            } else if (Boolean.FALSE.equals(overdue)) {
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.notEqual(root.get("status"), BillStatus.PENDING),
                        criteriaBuilder.greaterThanOrEqualTo(root.get("dueDate"), LocalDate.now())
                ));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Bill findOwnedBill(UUID userId, UUID billId) {
        return billRepository().findByIdAndUserId(billId, userId)
                .orElseThrow(() -> new NotFoundException("Conta a pagar nao encontrada"));
    }

    private void validateCategory(UUID userId, UUID categoryId) {
        Category category = categoryRepository().findByIdAndUserIdOrDefault(categoryId, userId)
                .orElseThrow(() -> new NotFoundException("Categoria nao encontrada"));
        if (!category.getType().name().equals(TransactionType.EXPENSE.name())) {
            throw new BadRequestException("Conta a pagar exige categoria de despesa");
        }
    }

    private void validateAccount(UUID userId, UUID accountId) {
        if (accountId == null) {
            return;
        }
        accountRepository().findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new NotFoundException("Conta nao encontrada"));
    }

    private void validateInitialStatus(BillStatus status, UUID accountId) {
        if (status == BillStatus.PAID && accountId == null) {
            throw new BadRequestException("Conta financeira obrigatoria para conta paga");
        }
    }

    private BillRepository billRepository() {
        return billRepository.getIfAvailable(() -> {
            throw new IllegalStateException("BillRepository nao disponivel");
        });
    }

    private AccountRepository accountRepository() {
        return accountRepository.getIfAvailable(() -> {
            throw new IllegalStateException("AccountRepository nao disponivel");
        });
    }

    private CategoryRepository categoryRepository() {
        return categoryRepository.getIfAvailable(() -> {
            throw new IllegalStateException("CategoryRepository nao disponivel");
        });
    }
}
