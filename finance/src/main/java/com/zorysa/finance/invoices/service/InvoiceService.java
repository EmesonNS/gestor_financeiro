package com.zorysa.finance.invoices.service;

import com.zorysa.finance.accounts.entity.Account;
import com.zorysa.finance.accounts.repository.AccountRepository;
import com.zorysa.finance.creditcards.entity.CreditCard;
import com.zorysa.finance.creditcards.repository.CreditCardRepository;
import com.zorysa.finance.invoices.dto.InvoiceResponse;
import com.zorysa.finance.invoices.dto.PayInvoiceRequest;
import com.zorysa.finance.invoices.entity.CreditCardInvoice;
import com.zorysa.finance.invoices.entity.InvoiceStatus;
import com.zorysa.finance.invoices.mapper.InvoiceMapper;
import com.zorysa.finance.invoices.repository.InvoiceRepository;
import com.zorysa.finance.shared.exception.BadRequestException;
import com.zorysa.finance.shared.exception.NotFoundException;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
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
public class InvoiceService {

    private final ObjectProvider<InvoiceRepository> invoiceRepository;
    private final ObjectProvider<CreditCardRepository> creditCardRepository;
    private final ObjectProvider<AccountRepository> accountRepository;
    private final InvoiceMapper invoiceMapper;

    public InvoiceService(
            ObjectProvider<InvoiceRepository> invoiceRepository,
            ObjectProvider<CreditCardRepository> creditCardRepository,
            ObjectProvider<AccountRepository> accountRepository,
            InvoiceMapper invoiceMapper
    ) {
        this.invoiceRepository = invoiceRepository;
        this.creditCardRepository = creditCardRepository;
        this.accountRepository = accountRepository;
        this.invoiceMapper = invoiceMapper;
    }

    @Transactional(readOnly = true)
    public Page<InvoiceResponse> listInvoices(UUID userId, UUID creditCardId, InvoiceStatus status, Integer year, Pageable pageable) {
        validateOwnedCreditCard(userId, creditCardId);
        return invoiceRepository().findAll(filterBy(userId, creditCardId, status, year), pageable)
                .map(invoiceMapper::toResponse);
    }

    @Transactional
    public InvoiceResponse getCurrentInvoice(UUID userId, UUID creditCardId) {
        CreditCard creditCard = validateOwnedCreditCard(userId, creditCardId);
        CreditCardInvoice invoice = invoiceRepository().findCurrentByUserIdAndCreditCardId(userId, creditCardId)
                .orElseGet(() -> getOrCreateInvoice(userId, creditCardId, LocalDate.now(), creditCard.getClosingDay(), creditCard.getDueDay()));
        invoice.updateTotalAmount(calculateTotalAmount(invoice.getId()));
        return invoiceMapper.toResponse(invoiceRepository().save(invoice));
    }

    @Transactional(readOnly = true)
    public InvoiceResponse getInvoice(UUID userId, UUID invoiceId) {
        CreditCardInvoice invoice = findOwnedInvoice(userId, invoiceId);
        return invoiceMapper.toResponse(invoice);
    }

    @Transactional
    public InvoiceResponse payInvoice(UUID userId, UUID invoiceId, PayInvoiceRequest request) {
        CreditCardInvoice invoice = findOwnedInvoice(userId, invoiceId);
        validateCanPay(invoice);
        Account account = findOwnedAccount(userId, request.paymentAccountId());
        applyPaymentBalanceImpact(account, invoice.getTotalAmount());
        invoice.markAsPaid(request.paymentAccountId(), request.paidAt());
        accountRepository().save(account);
        return invoiceMapper.toResponse(invoiceRepository().save(invoice));
    }

    @Transactional
    public CreditCardInvoice getOrCreateInvoice(UUID userId, UUID creditCardId, LocalDate purchaseDate, int closingDay, int dueDay) {
        YearMonth reference = referencePeriodFor(purchaseDate, closingDay);
        return invoiceRepository().findByCreditCardIdAndReferenceMonthAndReferenceYear(
                        creditCardId,
                        (short) reference.getMonthValue(),
                        (short) reference.getYear()
                )
                .orElseGet(() -> invoiceRepository().save(new CreditCardInvoice(
                        userId,
                        creditCardId,
                        reference.getMonthValue(),
                        reference.getYear(),
                        calculateClosingDate(reference.getMonthValue(), reference.getYear(), closingDay),
                        calculateDueDate(reference.getMonthValue(), reference.getYear(), dueDay, closingDay),
                        calculateTotalAmount(null),
                        InvoiceStatus.OPEN
                )));
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateTotalAmount(UUID invoiceId) {
        return BigDecimal.ZERO;
    }

    public LocalDate calculateClosingDate(int referenceMonth, int referenceYear, int closingDay) {
        YearMonth month = YearMonth.of(referenceYear, referenceMonth);
        return month.atDay(Math.min(closingDay, month.lengthOfMonth()));
    }

    public LocalDate calculateDueDate(int referenceMonth, int referenceYear, int dueDay, int closingDay) {
        YearMonth month = YearMonth.of(referenceYear, referenceMonth);
        if (dueDay <= closingDay) {
            month = month.plusMonths(1);
        }
        return month.atDay(Math.min(dueDay, month.lengthOfMonth()));
    }

    public void validateCanPay(CreditCardInvoice invoice) {
        if (!invoice.canBePaid()) {
            throw new BadRequestException("Fatura ja foi paga");
        }
    }

    public void applyPaymentBalanceImpact(Account account, BigDecimal totalAmount) {
        BigDecimal impact = totalAmount.negate();
        if (account.getCurrentBalance().add(impact).compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Saldo insuficiente na conta financeira");
        }
        account.applyBalanceImpact(impact);
    }

    private Specification<CreditCardInvoice> filterBy(UUID userId, UUID creditCardId, InvoiceStatus status, Integer year) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("userId"), userId));
            predicates.add(criteriaBuilder.equal(root.get("creditCardId"), creditCardId));
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            if (year != null) {
                predicates.add(criteriaBuilder.equal(root.get("referenceYear"), year));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private YearMonth referencePeriodFor(LocalDate purchaseDate, int closingDay) {
        if (purchaseDate.getDayOfMonth() > closingDay) {
            return YearMonth.from(purchaseDate).plusMonths(1);
        }
        return YearMonth.from(purchaseDate);
    }

    private CreditCard validateOwnedCreditCard(UUID userId, UUID creditCardId) {
        return creditCardRepository().findByIdAndUserId(creditCardId, userId)
                .orElseThrow(() -> new NotFoundException("Cartao de credito nao encontrado"));
    }

    private CreditCardInvoice findOwnedInvoice(UUID userId, UUID invoiceId) {
        return invoiceRepository().findByIdAndUserId(invoiceId, userId)
                .orElseThrow(() -> new NotFoundException("Fatura nao encontrada"));
    }

    private Account findOwnedAccount(UUID userId, UUID accountId) {
        return accountRepository().findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new NotFoundException("Conta nao encontrada"));
    }

    private InvoiceRepository invoiceRepository() {
        return invoiceRepository.getIfAvailable(() -> {
            throw new IllegalStateException("InvoiceRepository nao disponivel");
        });
    }

    private CreditCardRepository creditCardRepository() {
        return creditCardRepository.getIfAvailable(() -> {
            throw new IllegalStateException("CreditCardRepository nao disponivel");
        });
    }

    private AccountRepository accountRepository() {
        return accountRepository.getIfAvailable(() -> {
            throw new IllegalStateException("AccountRepository nao disponivel");
        });
    }
}
