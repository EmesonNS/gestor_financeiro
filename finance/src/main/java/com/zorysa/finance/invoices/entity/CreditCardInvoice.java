package com.zorysa.finance.invoices.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "credit_card_invoices")
public class CreditCardInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "credit_card_id", nullable = false)
    private UUID creditCardId;

    @Column(name = "reference_month", nullable = false, columnDefinition = "smallint")
    private Short referenceMonth;

    @Column(name = "reference_year", nullable = false, columnDefinition = "smallint")
    private Short referenceYear;

    @Column(name = "closing_date", nullable = false)
    private LocalDate closingDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "invoice_status")
    private InvoiceStatus status;

    @Column(name = "paid_at")
    private LocalDate paidAt;

    @Column(name = "payment_account_id")
    private UUID paymentAccountId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected CreditCardInvoice() {
    }

    public CreditCardInvoice(
            UUID userId,
            UUID creditCardId,
            int referenceMonth,
            int referenceYear,
            LocalDate closingDate,
            LocalDate dueDate,
            BigDecimal totalAmount,
            InvoiceStatus status
    ) {
        this.userId = userId;
        this.creditCardId = creditCardId;
        this.referenceMonth = (short) referenceMonth;
        this.referenceYear = (short) referenceYear;
        this.closingDate = closingDate;
        this.dueDate = dueDate;
        this.totalAmount = totalAmount;
        this.status = status;
    }

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getCreditCardId() {
        return creditCardId;
    }

    public int getReferenceMonth() {
        return referenceMonth.intValue();
    }

    public int getReferenceYear() {
        return referenceYear.intValue();
    }

    public LocalDate getClosingDate() {
        return closingDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public LocalDate getPaidAt() {
        return paidAt;
    }

    public UUID getPaymentAccountId() {
        return paymentAccountId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public boolean belongsTo(UUID userId) {
        return this.userId.equals(userId);
    }

    public void updateTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void reschedule(LocalDate closingDate, LocalDate dueDate) {
        this.closingDate = closingDate;
        this.dueDate = dueDate;
    }

    public void markAsPaid(UUID paymentAccountId, LocalDate paidAt) {
        this.paymentAccountId = paymentAccountId;
        this.paidAt = paidAt;
        this.status = InvoiceStatus.PAID;
    }

    public boolean isPaid() {
        return status == InvoiceStatus.PAID;
    }

    public boolean canBePaid() {
        return !isPaid();
    }

    public boolean canReceiveInstallments() {
        return !isPaid();
    }
}
