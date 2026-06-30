package com.zorysa.finance.installments.service;

import com.zorysa.finance.categories.entity.Category;
import com.zorysa.finance.categories.service.CategoryService;
import com.zorysa.finance.categories.repository.CategoryRepository;
import com.zorysa.finance.creditcards.entity.CreditCard;
import com.zorysa.finance.creditcards.repository.CreditCardRepository;
import com.zorysa.finance.creditcards.service.CreditCardService;
import com.zorysa.finance.installments.dto.CardPurchaseResponse;
import com.zorysa.finance.installments.dto.CreateCardPurchaseRequest;
import com.zorysa.finance.installments.dto.InstallmentResponse;
import com.zorysa.finance.installments.dto.UpdateCardPurchaseRequest;
import com.zorysa.finance.installments.entity.CardPurchase;
import com.zorysa.finance.installments.entity.CreditCardInstallment;
import com.zorysa.finance.installments.entity.InstallmentStatus;
import com.zorysa.finance.installments.entity.PurchaseStatus;
import com.zorysa.finance.installments.mapper.InstallmentMapper;
import com.zorysa.finance.installments.repository.CardPurchaseRepository;
import com.zorysa.finance.installments.repository.CreditCardInstallmentRepository;
import com.zorysa.finance.invoices.entity.CreditCardInvoice;
import com.zorysa.finance.invoices.service.InvoiceService;
import com.zorysa.finance.shared.exception.BadRequestException;
import com.zorysa.finance.shared.exception.NotFoundException;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
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
public class InstallmentService {

    private final ObjectProvider<CardPurchaseRepository> cardPurchaseRepository;
    private final ObjectProvider<CreditCardInstallmentRepository> installmentRepository;
    private final ObjectProvider<CreditCardRepository> creditCardRepository;
    private final ObjectProvider<CategoryRepository> categoryRepository;
    private final CreditCardService creditCardService;
    private final CategoryService categoryService;
    private final InvoiceService invoiceService;
    private final InstallmentMapper installmentMapper;

    public InstallmentService(
            ObjectProvider<CardPurchaseRepository> cardPurchaseRepository,
            ObjectProvider<CreditCardInstallmentRepository> installmentRepository,
            ObjectProvider<CreditCardRepository> creditCardRepository,
            ObjectProvider<CategoryRepository> categoryRepository,
            CreditCardService creditCardService,
            CategoryService categoryService,
            InvoiceService invoiceService,
            InstallmentMapper installmentMapper
    ) {
        this.cardPurchaseRepository = cardPurchaseRepository;
        this.installmentRepository = installmentRepository;
        this.creditCardRepository = creditCardRepository;
        this.categoryRepository = categoryRepository;
        this.creditCardService = creditCardService;
        this.categoryService = categoryService;
        this.invoiceService = invoiceService;
        this.installmentMapper = installmentMapper;
    }

    @Transactional
    public CardPurchaseResponse createPurchase(UUID userId, UUID creditCardId, CreateCardPurchaseRequest request) {
        CreditCard creditCard = findOwnedCreditCard(userId, creditCardId);
        creditCardService.validateCanReceivePurchases(userId, creditCardId);
        validateExpenseCategory(userId, request.categoryId());

        CardPurchase purchase = new CardPurchase(
                userId,
                creditCardId,
                request.categoryId(),
                request.description().trim(),
                request.totalAmount(),
                request.purchaseDate(),
                request.installmentCount(),
                normalizeOptional(request.notes())
        );
        CardPurchase saved = cardPurchaseRepository().save(purchase);
        List<CreditCardInstallment> installments = generateInstallments(
                userId,
                saved.getId(),
                creditCard,
                request.totalAmount(),
                request.purchaseDate(),
                request.installmentCount()
        );
        List<CreditCardInstallment> savedInstallments = installmentRepository().saveAll(installments);
        refreshInvoiceTotals(savedInstallments);
        return toPurchaseResponse(saved, savedInstallments);
    }

    @Transactional(readOnly = true)
    public Page<CardPurchaseResponse> listPurchases(
            UUID userId,
            UUID creditCardId,
            PurchaseStatus status,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    ) {
        findOwnedCreditCard(userId, creditCardId);
        return cardPurchaseRepository().findAll(filterPurchases(userId, creditCardId, status, startDate, endDate), pageable)
                .map(purchase -> toPurchaseResponse(purchase, installmentsForPurchase(userId, purchase.getId())));
    }

    @Transactional(readOnly = true)
    public CardPurchaseResponse getPurchase(UUID userId, UUID purchaseId) {
        CardPurchase purchase = findOwnedPurchase(userId, purchaseId);
        return toPurchaseResponse(purchase, installmentsForPurchase(userId, purchaseId));
    }

    @Transactional
    public CardPurchaseResponse updatePurchase(UUID userId, UUID purchaseId, UpdateCardPurchaseRequest request) {
        CardPurchase purchase = findOwnedPurchase(userId, purchaseId);
        validatePurchaseCanBeChanged(purchaseId);
        CreditCard creditCard = findOwnedCreditCard(userId, purchase.getCreditCardId());
        creditCardService.validateCanReceivePurchases(userId, purchase.getCreditCardId());
        validateExpenseCategory(userId, request.categoryId());

        List<CreditCardInstallment> oldInstallments = installmentsForPurchase(userId, purchaseId);
        oldInstallments.forEach(CreditCardInstallment::cancel);
        installmentRepository().saveAll(oldInstallments);

        purchase.updateDetails(
                request.categoryId(),
                request.description().trim(),
                request.totalAmount(),
                request.purchaseDate(),
                request.installmentCount(),
                normalizeOptional(request.notes())
        );
        CardPurchase saved = cardPurchaseRepository().save(purchase);
        List<CreditCardInstallment> newInstallments = generateInstallments(
                userId,
                saved.getId(),
                creditCard,
                request.totalAmount(),
                request.purchaseDate(),
                request.installmentCount()
        );
        List<CreditCardInstallment> savedInstallments = installmentRepository().saveAll(newInstallments);
        List<CreditCardInstallment> affected = new ArrayList<>(oldInstallments);
        affected.addAll(savedInstallments);
        refreshInvoiceTotals(affected);
        return toPurchaseResponse(saved, savedInstallments);
    }

    @Transactional
    public void deletePurchase(UUID userId, UUID purchaseId) {
        CardPurchase purchase = findOwnedPurchase(userId, purchaseId);
        validatePurchaseCanBeChanged(purchaseId);
        List<CreditCardInstallment> installments = installmentsForPurchase(userId, purchaseId);
        installments.forEach(CreditCardInstallment::cancel);
        installmentRepository().saveAll(installments);
        purchase.cancel();
        cardPurchaseRepository().save(purchase);
        refreshInvoiceTotals(installments);
    }

    @Transactional(readOnly = true)
    public Page<InstallmentResponse> listInstallments(
            UUID userId,
            InstallmentStatus status,
            UUID cardId,
            Integer month,
            Integer year,
            Pageable pageable
    ) {
        if (cardId != null) {
            findOwnedCreditCard(userId, cardId);
        }
        return installmentRepository().findAll(filterInstallments(userId, status, cardId, month, year, null), pageable)
                .map(installmentMapper::toInstallmentResponse);
    }

    @Transactional(readOnly = true)
    public Page<InstallmentResponse> listFutureInstallments(
            UUID userId,
            UUID cardId,
            Integer fromMonth,
            Integer fromYear,
            Pageable pageable
    ) {
        if (cardId != null) {
            findOwnedCreditCard(userId, cardId);
        }
        YearMonth from = fromMonth != null && fromYear != null
                ? YearMonth.of(fromYear, fromMonth)
                : YearMonth.now();
        return installmentRepository().findAll(filterFutureInstallments(userId, cardId, from), pageable)
                .map(installmentMapper::toInstallmentResponse);
    }

    @Transactional(readOnly = true)
    public Page<InstallmentResponse> listPurchaseInstallments(UUID userId, UUID purchaseId, Pageable pageable) {
        findOwnedPurchase(userId, purchaseId);
        return installmentRepository().findAllByUserIdAndPurchaseId(userId, purchaseId, pageable)
                .map(installmentMapper::toInstallmentResponse);
    }

    public List<CreditCardInstallment> generateInstallments(
            UUID userId,
            UUID purchaseId,
            CreditCard creditCard,
            BigDecimal totalAmount,
            LocalDate purchaseDate,
            int installmentCount
    ) {
        List<BigDecimal> amounts = installmentAmounts(totalAmount, installmentCount);
        List<CreditCardInstallment> installments = new ArrayList<>();
        for (int index = 0; index < installmentCount; index++) {
            LocalDate installmentDate = purchaseDate.plusMonths(index);
            CreditCardInvoice invoice = invoiceService.getOrCreateInvoice(
                    userId,
                    creditCard.getId(),
                    installmentDate,
                    creditCard.getClosingDay(),
                    creditCard.getDueDay()
            );
            if (!invoice.canReceiveInstallments()) {
                throw new BadRequestException("Fatura paga nao pode receber parcelas");
            }
            installments.add(new CreditCardInstallment(
                    userId,
                    purchaseId,
                    invoice.getId(),
                    index + 1,
                    installmentCount,
                    amounts.get(index),
                    invoice.getReferenceMonth(),
                    invoice.getReferenceYear(),
                    InstallmentStatus.OPEN
            ));
        }
        return installments;
    }

    public List<BigDecimal> generateInstallments(BigDecimal totalAmount, LocalDate purchaseDate, int installmentCount) {
        return installmentAmounts(totalAmount, installmentCount);
    }

    public void validatePurchaseCanBeChanged(UUID purchaseId) {
        if (installmentRepository().existsPaidInvoiceInstallmentByPurchaseId(purchaseId)) {
            throw new BadRequestException("Compra possui parcela em fatura paga");
        }
    }

    private List<BigDecimal> installmentAmounts(BigDecimal totalAmount, int installmentCount) {
        BigDecimal base = totalAmount.divide(BigDecimal.valueOf(installmentCount), 2, RoundingMode.DOWN);
        List<BigDecimal> amounts = new ArrayList<>();
        BigDecimal accumulated = BigDecimal.ZERO;
        for (int index = 1; index <= installmentCount; index++) {
            BigDecimal amount = index == installmentCount ? totalAmount.subtract(accumulated) : base;
            amounts.add(amount);
            accumulated = accumulated.add(amount);
        }
        return amounts;
    }

    private Specification<CardPurchase> filterPurchases(
            UUID userId,
            UUID creditCardId,
            PurchaseStatus status,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("userId"), userId));
            predicates.add(criteriaBuilder.equal(root.get("creditCardId"), creditCardId));
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("purchaseDate"), startDate));
            }
            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("purchaseDate"), endDate));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Specification<CreditCardInstallment> filterInstallments(
            UUID userId,
            InstallmentStatus status,
            UUID cardId,
            Integer month,
            Integer year,
            UUID purchaseId
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("userId"), userId));
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            if (month != null) {
                predicates.add(criteriaBuilder.equal(root.get("competenceMonth"), month.shortValue()));
            }
            if (year != null) {
                predicates.add(criteriaBuilder.equal(root.get("competenceYear"), year.shortValue()));
            }
            if (purchaseId != null) {
                predicates.add(criteriaBuilder.equal(root.get("purchaseId"), purchaseId));
            }
            if (cardId != null) {
                var purchase = query.from(CardPurchase.class);
                predicates.add(criteriaBuilder.equal(purchase.get("id"), root.get("purchaseId")));
                predicates.add(criteriaBuilder.equal(purchase.get("creditCardId"), cardId));
                predicates.add(criteriaBuilder.equal(purchase.get("userId"), userId));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Specification<CreditCardInstallment> filterFutureInstallments(UUID userId, UUID cardId, YearMonth from) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("userId"), userId));
            predicates.add(criteriaBuilder.notEqual(root.get("status"), InstallmentStatus.CANCELED));
            predicates.add(criteriaBuilder.or(
                    criteriaBuilder.greaterThan(root.get("competenceYear"), (short) from.getYear()),
                    criteriaBuilder.and(
                            criteriaBuilder.equal(root.get("competenceYear"), (short) from.getYear()),
                            criteriaBuilder.greaterThanOrEqualTo(root.get("competenceMonth"), (short) from.getMonthValue())
                    )
            ));
            if (cardId != null) {
                var purchase = query.from(CardPurchase.class);
                predicates.add(criteriaBuilder.equal(purchase.get("id"), root.get("purchaseId")));
                predicates.add(criteriaBuilder.equal(purchase.get("creditCardId"), cardId));
                predicates.add(criteriaBuilder.equal(purchase.get("userId"), userId));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private CardPurchaseResponse toPurchaseResponse(CardPurchase purchase, List<CreditCardInstallment> installments) {
        return installmentMapper.toPurchaseResponse(
                purchase,
                installments.stream().map(installmentMapper::toInstallmentResponse).toList()
        );
    }

    private List<CreditCardInstallment> installmentsForPurchase(UUID userId, UUID purchaseId) {
        return installmentRepository().findAllByUserIdAndPurchaseIdOrderByInstallmentNumberAsc(userId, purchaseId);
    }

    private void refreshInvoiceTotals(List<CreditCardInstallment> installments) {
        installments.stream()
                .map(CreditCardInstallment::getInvoiceId)
                .distinct()
                .forEach(invoiceService::refreshInvoiceTotal);
    }

    private CardPurchase findOwnedPurchase(UUID userId, UUID purchaseId) {
        return cardPurchaseRepository().findByIdAndUserId(purchaseId, userId)
                .orElseThrow(() -> new NotFoundException("Compra no cartao nao encontrada"));
    }

    private CreditCard findOwnedCreditCard(UUID userId, UUID creditCardId) {
        return creditCardRepository().findByIdAndUserId(creditCardId, userId)
                .orElseThrow(() -> new NotFoundException("Cartao de credito nao encontrado"));
    }

    private void validateExpenseCategory(UUID userId, UUID categoryId) {
        categoryService.getCategory(userId, categoryId);
        Category category = categoryRepository().findByIdAndUserIdOrDefault(categoryId, userId)
                .orElseThrow(() -> new NotFoundException("Categoria nao encontrada"));
        if (!category.getType().name().equals("EXPENSE")) {
            throw new BadRequestException("Compra no cartao exige categoria de despesa");
        }
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private CardPurchaseRepository cardPurchaseRepository() {
        return cardPurchaseRepository.getIfAvailable(() -> {
            throw new IllegalStateException("CardPurchaseRepository nao disponivel");
        });
    }

    private CreditCardInstallmentRepository installmentRepository() {
        return installmentRepository.getIfAvailable(() -> {
            throw new IllegalStateException("CreditCardInstallmentRepository nao disponivel");
        });
    }

    private CreditCardRepository creditCardRepository() {
        return creditCardRepository.getIfAvailable(() -> {
            throw new IllegalStateException("CreditCardRepository nao disponivel");
        });
    }

    private CategoryRepository categoryRepository() {
        return categoryRepository.getIfAvailable(() -> {
            throw new IllegalStateException("CategoryRepository nao disponivel");
        });
    }
}
