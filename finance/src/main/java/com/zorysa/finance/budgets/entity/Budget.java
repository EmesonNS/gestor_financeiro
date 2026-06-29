package com.zorysa.finance.budgets.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "budgets")
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "category_id", nullable = false)
    private UUID categoryId;

    @Column(name = "start_month", nullable = false, columnDefinition = "smallint")
    private Short startMonth;

    @Column(name = "start_year", nullable = false, columnDefinition = "smallint")
    private Short startYear;

    @Column(name = "end_month", columnDefinition = "smallint")
    private Short endMonth;

    @Column(name = "end_year", columnDefinition = "smallint")
    private Short endYear;

    @Column(name = "limit_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal limitAmount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Budget() {
    }

    public Budget(UUID userId, UUID categoryId, int startMonth, int startYear, Integer endMonth, Integer endYear, BigDecimal limitAmount) {
        this.userId = userId;
        this.categoryId = categoryId;
        this.startMonth = (short) startMonth;
        this.startYear = (short) startYear;
        this.endMonth = endMonth == null ? null : endMonth.shortValue();
        this.endYear = endYear == null ? null : endYear.shortValue();
        this.limitAmount = limitAmount;
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

    public UUID getCategoryId() {
        return categoryId;
    }

    public int getStartMonth() {
        return startMonth;
    }

    public int getStartYear() {
        return startYear;
    }

    public Integer getEndMonth() {
        return endMonth == null ? null : endMonth.intValue();
    }

    public Integer getEndYear() {
        return endYear == null ? null : endYear.intValue();
    }

    public BigDecimal getLimitAmount() {
        return limitAmount;
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

    public boolean hasOpenEndedPeriod() {
        return endMonth == null && endYear == null;
    }

    public boolean isActiveIn(int month, int year) {
        int requested = periodValue(month, year);
        return periodValue(startMonth, startYear) <= requested
                && (hasOpenEndedPeriod() || periodValue(endMonth, endYear) >= requested);
    }

    public boolean overlaps(int startMonth, int startYear, int endMonth, int endYear) {
        return overlaps(startMonth, startYear, Integer.valueOf(endMonth), Integer.valueOf(endYear));
    }

    public boolean overlaps(int startMonth, int startYear, Integer endMonth, Integer endYear) {
        int existingStart = periodValue(this.startMonth, this.startYear);
        int requestedStart = periodValue(startMonth, startYear);
        Integer existingEnd = hasOpenEndedPeriod() ? null : periodValue(this.endMonth, this.endYear);
        Integer requestedEnd = endMonth == null || endYear == null ? null : periodValue(endMonth, endYear);

        boolean existingEndsAfterRequestedStarts = existingEnd == null || existingEnd >= requestedStart;
        boolean requestedEndsAfterExistingStarts = requestedEnd == null || requestedEnd >= existingStart;
        return existingEndsAfterRequestedStarts && requestedEndsAfterExistingStarts;
    }

    public BigDecimal remainingAmount(BigDecimal spentAmount) {
        return limitAmount.subtract(spentAmount);
    }

    public BigDecimal usagePercentage(BigDecimal spentAmount) {
        return spentAmount.multiply(new BigDecimal("100"))
                .divide(limitAmount, 2, RoundingMode.HALF_UP);
    }

    public boolean isExceeded(BigDecimal spentAmount) {
        return spentAmount.compareTo(limitAmount) > 0;
    }

    public void updateDetails(UUID categoryId, int startMonth, int startYear, Integer endMonth, Integer endYear, BigDecimal limitAmount) {
        this.categoryId = categoryId;
        this.startMonth = (short) startMonth;
        this.startYear = (short) startYear;
        this.endMonth = endMonth == null ? null : endMonth.shortValue();
        this.endYear = endYear == null ? null : endYear.shortValue();
        this.limitAmount = limitAmount;
    }

    private int periodValue(Number month, Number year) {
        return year.intValue() * 100 + month.intValue();
    }
}
