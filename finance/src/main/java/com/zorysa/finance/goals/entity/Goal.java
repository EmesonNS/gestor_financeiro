package com.zorysa.finance.goals.entity;

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
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "goals")
public class Goal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "target_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal targetAmount;

    @Column(name = "current_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal currentAmount;

    @Column
    private LocalDate deadline;

    @Column(columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "goal_status")
    private GoalStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Goal() {
    }

    public Goal(UUID userId, String name, BigDecimal targetAmount, BigDecimal currentAmount, LocalDate deadline, String description) {
        this.userId = userId;
        this.name = name;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
        this.deadline = deadline;
        this.description = description;
        refreshStatusFromProgress();
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

    public BigDecimal getTargetAmount() {
        return targetAmount;
    }

    public BigDecimal getCurrentAmount() {
        return currentAmount;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public String getDescription() {
        return description;
    }

    public GoalStatus getStatus() {
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

    public BigDecimal completionPercentage() {
        BigDecimal percentage = currentAmount.multiply(new BigDecimal("100"))
                .divide(targetAmount, 2, RoundingMode.HALF_UP);
        return percentage.min(new BigDecimal("100.00"));
    }

    public void updateProgress(BigDecimal currentAmount) {
        this.currentAmount = currentAmount;
        refreshStatusFromProgress();
    }

    public void cancel() {
        this.status = GoalStatus.CANCELED;
    }

    public void updateDetails(String name, BigDecimal targetAmount, BigDecimal currentAmount, LocalDate deadline, String description) {
        this.name = name;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
        this.deadline = deadline;
        this.description = description;
        refreshStatusFromProgress();
    }

    private void refreshStatusFromProgress() {
        if (targetAmount != null && currentAmount != null && currentAmount.compareTo(targetAmount) >= 0) {
            status = GoalStatus.COMPLETED;
            return;
        }
        status = GoalStatus.ACTIVE;
    }
}
