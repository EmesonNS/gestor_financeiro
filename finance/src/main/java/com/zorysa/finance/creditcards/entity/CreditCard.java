package com.zorysa.finance.creditcards.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "credit_cards")
public class CreditCard {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "limit_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal limitAmount;

    @Column(name = "closing_day", nullable = false, columnDefinition = "smallint")
    private Short closingDay;

    @Column(name = "due_day", nullable = false, columnDefinition = "smallint")
    private Short dueDay;

    @Column(nullable = false)
    private boolean archived;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected CreditCard() {
    }

    public CreditCard(UUID userId, String name, BigDecimal limitAmount, int closingDay, int dueDay) {
        this.userId = userId;
        this.name = name;
        this.limitAmount = limitAmount;
        this.closingDay = (short) closingDay;
        this.dueDay = (short) dueDay;
        this.archived = false;
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

    public String getName() {
        return name;
    }

    public BigDecimal getLimitAmount() {
        return limitAmount;
    }

    public int getClosingDay() {
        return closingDay;
    }

    public int getDueDay() {
        return dueDay;
    }

    public boolean isArchived() {
        return archived;
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

    public BigDecimal usedLimit(BigDecimal usedAmount) {
        return usedAmount;
    }

    public BigDecimal availableLimit(BigDecimal usedAmount) {
        BigDecimal available = limitAmount.subtract(usedAmount);
        return available.signum() < 0 ? BigDecimal.ZERO : available;
    }

    public void archive() {
        archived = true;
    }

    public boolean canReceivePurchases() {
        return !archived;
    }

    public void updateDetails(String name, BigDecimal limitAmount, int closingDay, int dueDay) {
        this.name = name;
        this.limitAmount = limitAmount;
        this.closingDay = (short) closingDay;
        this.dueDay = (short) dueDay;
    }
}
