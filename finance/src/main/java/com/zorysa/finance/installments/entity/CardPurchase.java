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
import java.time.LocalDate;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "credit_card_purchases")
public class CardPurchase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "credit_card_id", nullable = false)
    private UUID creditCardId;

    @Column(name = "category_id", nullable = false)
    private UUID categoryId;

    @Column(nullable = false, length = 180)
    private String description;

    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "purchase_date", nullable = false)
    private LocalDate purchaseDate;

    @Column(name = "installment_count", nullable = false)
    private Integer installmentCount;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "purchase_status")
    private PurchaseStatus status;

    @Column(columnDefinition = "text")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected CardPurchase() {
    }

    public CardPurchase(
            UUID userId,
            UUID creditCardId,
            UUID categoryId,
            String description,
            BigDecimal totalAmount,
            LocalDate purchaseDate,
            int installmentCount,
            String notes
    ) {
        this.userId = userId;
        this.creditCardId = creditCardId;
        this.categoryId = categoryId;
        this.description = description;
        this.totalAmount = totalAmount;
        this.purchaseDate = purchaseDate;
        this.installmentCount = installmentCount;
        this.status = PurchaseStatus.ACTIVE;
        this.notes = notes;
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

    public UUID getCategoryId() {
        return categoryId;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public int getInstallmentCount() {
        return installmentCount;
    }

    public PurchaseStatus getStatus() {
        return status;
    }

    public String getNotes() {
        return notes;
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

    public void updateDetails(
            UUID categoryId,
            String description,
            BigDecimal totalAmount,
            LocalDate purchaseDate,
            int installmentCount,
            String notes
    ) {
        this.categoryId = categoryId;
        this.description = description;
        this.totalAmount = totalAmount;
        this.purchaseDate = purchaseDate;
        this.installmentCount = installmentCount;
        this.notes = notes;
        this.status = PurchaseStatus.ACTIVE;
    }

    public void cancel() {
        status = PurchaseStatus.CANCELED;
    }
}
