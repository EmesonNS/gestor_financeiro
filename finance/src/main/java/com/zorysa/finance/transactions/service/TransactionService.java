package com.zorysa.finance.transactions.service;

import com.zorysa.finance.accounts.entity.Account;
import com.zorysa.finance.accounts.repository.AccountRepository;
import com.zorysa.finance.categories.entity.Category;
import com.zorysa.finance.categories.repository.CategoryRepository;
import com.zorysa.finance.shared.exception.BadRequestException;
import com.zorysa.finance.shared.exception.NotFoundException;
import com.zorysa.finance.transactions.dto.CreateTransactionRequest;
import com.zorysa.finance.transactions.dto.MarkTransactionAsPaidRequest;
import com.zorysa.finance.transactions.dto.TransactionResponse;
import com.zorysa.finance.transactions.dto.UpdateTransactionRequest;
import com.zorysa.finance.transactions.entity.Transaction;
import com.zorysa.finance.transactions.entity.TransactionStatus;
import com.zorysa.finance.transactions.entity.TransactionType;
import com.zorysa.finance.transactions.mapper.TransactionMapper;
import com.zorysa.finance.transactions.repository.TransactionRepository;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
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
public class TransactionService {

    private final ObjectProvider<TransactionRepository> transactionRepository;
    private final ObjectProvider<AccountRepository> accountRepository;
    private final ObjectProvider<CategoryRepository> categoryRepository;
    private final TransactionMapper transactionMapper;

    public TransactionService(
            ObjectProvider<TransactionRepository> transactionRepository,
            ObjectProvider<AccountRepository> accountRepository,
            ObjectProvider<CategoryRepository> categoryRepository,
            TransactionMapper transactionMapper
    ) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
        this.transactionMapper = transactionMapper;
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> listTransactions(
            UUID userId,
            LocalDate startDate,
            LocalDate endDate,
            TransactionType type,
            TransactionStatus status,
            UUID categoryId,
            UUID accountId,
            Pageable pageable
    ) {
        return transactionRepository().findAll(
                filterBy(userId, startDate, endDate, type, status, categoryId, accountId),
                pageable
        ).map(transactionMapper::toResponse);
    }

    @Transactional
    public TransactionResponse createTransaction(UUID userId, CreateTransactionRequest request) {
        TransactionRepository repository = transactionRepository();
        validateCategory(userId, request.categoryId(), request.type());
        validateRealizedStatus(request.type(), request.status());
        Account account = accountForNewImpact(userId, request.accountId(), request.type(), request.status());
        validateSufficientBalance(account, projectedImpact(request.amount(), request.type(), request.status()));

        Transaction transaction = new Transaction(
                userId,
                request.description().trim(),
                request.amount(),
                request.type(),
                request.transactionDate(),
                request.categoryId(),
                request.accountId(),
                request.status(),
                normalizeOptional(request.notes())
        );
        applyImpact(account, transaction.balanceImpact());
        return transactionMapper.toResponse(repository.save(transaction));
    }

    @Transactional(readOnly = true)
    public TransactionResponse getTransaction(UUID userId, UUID transactionId) {
        return transactionMapper.toResponse(findOwnedTransaction(userId, transactionId));
    }

    @Transactional
    public TransactionResponse updateTransaction(UUID userId, UUID transactionId, UpdateTransactionRequest request) {
        Transaction transaction = findOwnedTransaction(userId, transactionId);
        UUID oldAccountId = transaction.getAccountId();
        BigDecimal oldImpact = transaction.balanceImpact();

        validateCategory(userId, request.categoryId(), request.type());
        validateRealizedStatus(request.type(), request.status());
        Account newAccount = accountForNewImpact(userId, request.accountId(), request.type(), request.status());
        BigDecimal newImpact = projectedImpact(request.amount(), request.type(), request.status());
        validateSufficientBalanceForReplacement(newAccount, request.accountId(), oldAccountId, oldImpact, newImpact);

        reverseImpact(userId, oldAccountId, oldImpact);
        transaction.updateDetails(
                request.description().trim(),
                request.amount(),
                request.type(),
                request.transactionDate(),
                request.categoryId(),
                request.accountId(),
                request.status(),
                normalizeOptional(request.notes())
        );
        applyImpact(newAccount, transaction.balanceImpact());
        return transactionMapper.toResponse(transactionRepository().save(transaction));
    }

    @Transactional
    public void deleteTransaction(UUID userId, UUID transactionId) {
        Transaction transaction = findOwnedTransaction(userId, transactionId);
        reverseImpact(userId, transaction.getAccountId(), transaction.balanceImpact());
        transactionRepository().delete(transaction);
    }

    @Transactional
    public TransactionResponse markAsPaid(UUID userId, UUID transactionId, MarkTransactionAsPaidRequest request) {
        categoryRepository();
        Transaction transaction = findOwnedTransaction(userId, transactionId);
        UUID oldAccountId = transaction.getAccountId();
        BigDecimal oldImpact = transaction.balanceImpact();
        UUID newAccountId = request != null && request.accountId() != null ? request.accountId() : oldAccountId;
        LocalDate paidDate = request != null && request.paidDate() != null ? request.paidDate() : transaction.getTransactionDate();

        if (newAccountId == null) {
            throw new BadRequestException("Conta obrigatoria para transacao realizada");
        }
        Account newAccount = findOwnedAccount(userId, newAccountId);
        TransactionStatus newStatus = transaction.getType() == TransactionType.INCOME
                ? TransactionStatus.RECEIVED
                : TransactionStatus.PAID;
        BigDecimal newImpact = projectedImpact(transaction.getAmount(), transaction.getType(), newStatus);
        validateSufficientBalanceForReplacement(newAccount, newAccountId, oldAccountId, oldImpact, newImpact);

        reverseImpact(userId, oldAccountId, oldImpact);
        transaction.markAsPaid(newAccountId);
        transaction.updateDetails(
                transaction.getDescription(),
                transaction.getAmount(),
                transaction.getType(),
                paidDate,
                transaction.getCategoryId(),
                transaction.getAccountId(),
                transaction.getStatus(),
                transaction.getNotes()
        );
        applyImpact(newAccount, transaction.balanceImpact());
        return transactionMapper.toResponse(transactionRepository().save(transaction));
    }

    @Transactional
    public TransactionResponse cancelTransaction(UUID userId, UUID transactionId) {
        Transaction transaction = findOwnedTransaction(userId, transactionId);
        UUID oldAccountId = transaction.getAccountId();
        BigDecimal oldImpact = transaction.balanceImpact();

        reverseImpact(userId, oldAccountId, oldImpact);
        transaction.cancel();
        return transactionMapper.toResponse(transactionRepository().save(transaction));
    }

    private Specification<Transaction> filterBy(
            UUID userId,
            LocalDate startDate,
            LocalDate endDate,
            TransactionType type,
            TransactionStatus status,
            UUID categoryId,
            UUID accountId
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("userId"), userId));
            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("transactionDate"), startDate));
            }
            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("transactionDate"), endDate));
            }
            if (type != null) {
                predicates.add(criteriaBuilder.equal(root.get("type"), type));
            }
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            if (categoryId != null) {
                predicates.add(criteriaBuilder.equal(root.get("categoryId"), categoryId));
            }
            if (accountId != null) {
                predicates.add(criteriaBuilder.equal(root.get("accountId"), accountId));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Transaction findOwnedTransaction(UUID userId, UUID transactionId) {
        return transactionRepository().findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new NotFoundException("Transacao nao encontrada"));
    }

    private void validateCategory(UUID userId, UUID categoryId, TransactionType transactionType) {
        Category category = categoryRepository().findByIdAndUserIdOrDefault(categoryId, userId)
                .orElseThrow(() -> new NotFoundException("Categoria nao encontrada"));
        if (!category.getType().name().equals(transactionType.name())) {
            throw new BadRequestException("Categoria incompativel com o tipo da transacao");
        }
    }

    private Account accountForNewImpact(UUID userId, UUID accountId, TransactionType type, TransactionStatus status) {
        BigDecimal impact = projectedImpact(BigDecimal.ONE, type, status);
        if (impact.compareTo(BigDecimal.ZERO) == 0) {
            return accountId == null ? null : findOwnedAccount(userId, accountId);
        }
        if (accountId == null) {
            throw new BadRequestException("Conta obrigatoria para transacao realizada");
        }
        return findOwnedAccount(userId, accountId);
    }

    private void validateRealizedStatus(TransactionType type, TransactionStatus status) {
        if (type == TransactionType.EXPENSE && status == TransactionStatus.RECEIVED) {
            throw new BadRequestException("Despesa nao pode ser recebida");
        }
        if (type == TransactionType.INCOME && status == TransactionStatus.PAID) {
            throw new BadRequestException("Receita nao pode ser paga");
        }
    }

    private BigDecimal projectedImpact(BigDecimal amount, TransactionType type, TransactionStatus status) {
        if (type == TransactionType.EXPENSE && status == TransactionStatus.PAID) {
            return amount.negate();
        }
        if (type == TransactionType.INCOME && status == TransactionStatus.RECEIVED) {
            return amount;
        }
        return BigDecimal.ZERO;
    }

    private Account findOwnedAccount(UUID userId, UUID accountId) {
        return accountRepository().findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new NotFoundException("Conta nao encontrada"));
    }

    private void reverseImpact(UUID userId, UUID accountId, BigDecimal impact) {
        if (impact.compareTo(BigDecimal.ZERO) == 0 || accountId == null) {
            return;
        }
        Account account = findOwnedAccount(userId, accountId);
        account.applyBalanceImpact(impact.negate());
        accountRepository().save(account);
    }

    private void applyImpact(Account account, BigDecimal impact) {
        if (account == null || impact.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }
        validateSufficientBalance(account, impact);
        account.applyBalanceImpact(impact);
        accountRepository().save(account);
    }

    private void validateSufficientBalance(Account account, BigDecimal impact) {
        if (account == null || impact.compareTo(BigDecimal.ZERO) >= 0) {
            return;
        }
        if (account.getCurrentBalance().add(impact).compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Saldo insuficiente na conta financeira");
        }
    }

    private void validateSufficientBalanceForReplacement(
            Account account,
            UUID newAccountId,
            UUID oldAccountId,
            BigDecimal oldImpact,
            BigDecimal newImpact
    ) {
        if (account == null || newImpact.compareTo(BigDecimal.ZERO) >= 0) {
            return;
        }
        BigDecimal availableBalance = account.getCurrentBalance();
        if (oldAccountId != null && oldAccountId.equals(newAccountId)) {
            availableBalance = availableBalance.subtract(oldImpact);
        }
        if (availableBalance.add(newImpact).compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Saldo insuficiente na conta financeira");
        }
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private TransactionRepository transactionRepository() {
        return transactionRepository.getIfAvailable(() -> {
            throw new IllegalStateException("TransactionRepository nao disponivel");
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
