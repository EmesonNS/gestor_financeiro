package com.zorysa.finance.installments.entity;

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
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "credit_card_installments")
public class CreditCardInstallment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "purchase_id", nullable = false)
    private UUID purchaseId;

    @Column(name = "invoice_id", nullable = false)
    private UUID invoiceId;

    @Column(name = "installment_number", nullable = false)
    private Integer installmentNumber;

    @Column(name = "total_installments", nullable = false)
    private Integer totalInstallments;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "competence_month", nullable = false, columnDefinition = "smallint")
    private Short competenceMonth;

    @Column(name = "competence_year", nullable = false, columnDefinition = "smallint")
    private Short competenceYear;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "installment_status")
    private InstallmentStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected CreditCardInstallment() {
    }

    public CreditCardInstallment(
            UUID userId,
            UUID purchaseId,
            UUID invoiceId,
            int installmentNumber,
            int totalInstallments,
            BigDecimal amount,
            int competenceMonth,
            int competenceYear,
            InstallmentStatus status
    ) {
        this.userId = userId;
        this.purchaseId = purchaseId;
        this.invoiceId = invoiceId;
        this.installmentNumber = installmentNumber;
        this.totalInstallments = totalInstallments;
        this.amount = amount;
        this.competenceMonth = (short) competenceMonth;
        this.competenceYear = (short) competenceYear;
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

    public UUID getPurchaseId() {
        return purchaseId;
    }

    public UUID getInvoiceId() {
        return invoiceId;
    }

    public int getInstallmentNumber() {
        return installmentNumber;
    }

    public int getTotalInstallments() {
        return totalInstallments;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public int getCompetenceMonth() {
        return competenceMonth.intValue();
    }

    public int getCompetenceYear() {
        return competenceYear.intValue();
    }

    public InstallmentStatus getStatus() {
        return status;
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

    public void markAsPaid() {
        status = InstallmentStatus.PAID;
    }

    public void cancel() {
        status = InstallmentStatus.CANCELED;
    }
}
