package com.zorysa.finance.invoices.repository;

import com.zorysa.finance.invoices.entity.CreditCardInvoice;
import com.zorysa.finance.invoices.entity.InvoiceStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface InvoiceRepository extends JpaRepository<CreditCardInvoice, UUID>, JpaSpecificationExecutor<CreditCardInvoice> {

    Optional<CreditCardInvoice> findByIdAndUserId(UUID id, UUID userId);

    Page<CreditCardInvoice> findAllByUserIdAndCreditCardId(UUID userId, UUID creditCardId, Pageable pageable);

    List<CreditCardInvoice> findAllByUserIdAndCreditCardIdAndStatusNot(
            UUID userId,
            UUID creditCardId,
            InvoiceStatus status
    );

    Optional<CreditCardInvoice> findByCreditCardIdAndReferenceMonthAndReferenceYear(
            UUID creditCardId,
            short referenceMonth,
            short referenceYear
    );

    Optional<CreditCardInvoice> findFirstByUserIdAndCreditCardIdAndClosingDateGreaterThanEqualOrderByClosingDateAsc(
            UUID userId,
            UUID creditCardId,
            java.time.LocalDate closingDate
    );

    default Optional<CreditCardInvoice> findCurrentByUserIdAndCreditCardId(UUID userId, UUID creditCardId) {
        return findFirstByUserIdAndCreditCardIdAndClosingDateGreaterThanEqualOrderByClosingDateAsc(
                userId,
                creditCardId,
                java.time.LocalDate.now()
        );
    }
}
