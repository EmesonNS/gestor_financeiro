package com.zorysa.finance.installments.repository;

import com.zorysa.finance.installments.entity.CreditCardInstallment;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CreditCardInstallmentRepository
        extends JpaRepository<CreditCardInstallment, UUID>, JpaSpecificationExecutor<CreditCardInstallment> {

    Page<CreditCardInstallment> findAllByUserId(UUID userId, Pageable pageable);

    Page<CreditCardInstallment> findAllByUserIdAndPurchaseId(UUID userId, UUID purchaseId, Pageable pageable);

    List<CreditCardInstallment> findAllByUserIdAndPurchaseIdOrderByInstallmentNumberAsc(UUID userId, UUID purchaseId);

    List<CreditCardInstallment> findAllByInvoiceId(UUID invoiceId);

    @Query(value = """
            select count(installment) > 0
            from credit_card_installments installment
            join credit_card_invoices invoice on invoice.id = installment.invoice_id
            where installment.purchase_id = :purchaseId
              and invoice.status = 'PAID'::invoice_status
            """, nativeQuery = true)
    boolean existsPaidInvoiceInstallmentByPurchaseId(@Param("purchaseId") UUID purchaseId);

    @Query(value = """
            select coalesce(sum(installment.amount), 0)
             from credit_card_installments installment
             where installment.invoice_id = :invoiceId
               and installment.status <> 'CANCELED'::installment_status
            """, nativeQuery = true)
    BigDecimal sumAmountByInvoiceId(@Param("invoiceId") UUID invoiceId);

    @Query(value = """
            select coalesce(sum(installment.amount), 0)
            from credit_card_installments installment
            join credit_card_purchases purchase on purchase.id = installment.purchase_id
            join credit_card_invoices invoice on invoice.id = installment.invoice_id
            where purchase.user_id = :userId
              and purchase.credit_card_id = :creditCardId
              and purchase.status = 'ACTIVE'::purchase_status
              and installment.status = 'OPEN'::installment_status
              and invoice.status <> 'PAID'::invoice_status
            """, nativeQuery = true)
    BigDecimal sumOpenAmountByUserIdAndCreditCardId(@Param("userId") UUID userId, @Param("creditCardId") UUID creditCardId);
}
