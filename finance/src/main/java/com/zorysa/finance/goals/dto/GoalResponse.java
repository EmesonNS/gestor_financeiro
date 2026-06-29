package com.zorysa.finance.goals.dto;

import com.zorysa.finance.goals.entity.GoalStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record GoalResponse(
        UUID id,
        String name,
        BigDecimal targetAmount,
        BigDecimal currentAmount,
        LocalDate deadline,
        String description,
        GoalStatus status,
        BigDecimal completionPercentage,
        Instant createdAt,
        Instant updatedAt
) {
}
