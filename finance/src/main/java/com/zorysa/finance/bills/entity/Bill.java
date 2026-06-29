package com.zorysa.finance.bills.entity;

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
@Table(name = "bills")
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "account_id")
    private UUID accountId;

    @Column(name = "category_id", nullable = false)
    private UUID categoryId;

    @Column(name = "transaction_id")
    private UUID transactionId;

    @Column(nullable = false, length = 180)
    private String description;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "paid_at")
    private LocalDate paidAt;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "bill_status")
    private BillStatus status;

    @Column(name = "recurrence_id")
    private UUID recurrenceId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Bill() {
    }

    public Bill(
            UUID userId,
            String description,
            BigDecimal amount,
            LocalDate dueDate,
            UUID categoryId,
            UUID accountId,
            BillStatus status,
            LocalDate paidAt,
            UUID transactionId
    ) {
        this.userId = userId;
        this.description = description;
        this.amount = amount;
        this.dueDate = dueDate;
        this.categoryId = categoryId;
        this.accountId = accountId;
        this.status = status;
        this.paidAt = paidAt;
        this.transactionId = transactionId;
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

    public UUID getAccountId() {
        return accountId;
    }

    public UUID getCategoryId() {
        return categoryId;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDate getPaidAt() {
        return paidAt;
    }

    public BillStatus getStatus() {
        return status;
    }

    public UUID getRecurrenceId() {
        return recurrenceId;
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

    public boolean isOverdue(LocalDate currentDate) {
        return status == BillStatus.PENDING && dueDate.isBefore(currentDate);
    }

    public boolean isUpcoming(LocalDate currentDate, int daysAhead) {
        LocalDate limit = currentDate.plusDays(daysAhead);
        return status == BillStatus.PENDING
                && !dueDate.isBefore(currentDate)
                && !dueDate.isAfter(limit);
    }

    public boolean isPaid() {
        return status == BillStatus.PAID;
    }

    public void markAsPaid(UUID accountId, LocalDate paidAt, UUID transactionId) {
        this.accountId = accountId;
        this.paidAt = paidAt;
        this.transactionId = transactionId;
        this.status = BillStatus.PAID;
    }

    public void updateDetails(
            String description,
            BigDecimal amount,
            LocalDate dueDate,
            UUID categoryId,
            UUID accountId,
            BillStatus status
    ) {
        this.description = description;
        this.amount = amount;
        this.dueDate = dueDate;
        this.categoryId = categoryId;
        this.accountId = accountId;
        this.status = status;
        if (status != BillStatus.PAID) {
            this.paidAt = null;
        }
    }
}
