package com.zorysa.finance.goals.service;

import com.zorysa.finance.goals.dto.CreateGoalRequest;
import com.zorysa.finance.goals.dto.GoalResponse;
import com.zorysa.finance.goals.dto.UpdateGoalProgressRequest;
import com.zorysa.finance.goals.dto.UpdateGoalRequest;
import com.zorysa.finance.goals.entity.Goal;
import com.zorysa.finance.goals.entity.GoalStatus;
import com.zorysa.finance.goals.mapper.GoalMapper;
import com.zorysa.finance.goals.repository.GoalRepository;
import com.zorysa.finance.shared.exception.NotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GoalService {

    private final ObjectProvider<GoalRepository> goalRepository;
    private final GoalMapper goalMapper;

    public GoalService(ObjectProvider<GoalRepository> goalRepository, GoalMapper goalMapper) {
        this.goalRepository = goalRepository;
        this.goalMapper = goalMapper;
    }

    @Transactional(readOnly = true)
    public Page<GoalResponse> listGoals(UUID userId, GoalStatus status, Pageable pageable) {
        Page<Goal> goals = status == null
                ? goalRepository().findAllByUserId(userId, pageable)
                : goalRepository().findAllByUserIdAndStatus(userId, status, pageable);
        return goals.map(goalMapper::toResponse);
    }

    @Transactional
    public GoalResponse createGoal(UUID userId, CreateGoalRequest request) {
        Goal goal = new Goal(
                userId,
                request.name().trim(),
                request.targetAmount(),
                request.currentAmount(),
                request.deadline(),
                request.description()
        );
        return goalMapper.toResponse(goalRepository().save(goal));
    }

    @Transactional(readOnly = true)
    public GoalResponse getGoal(UUID userId, UUID goalId) {
        return goalMapper.toResponse(findOwnedGoal(userId, goalId));
    }

    @Transactional
    public GoalResponse updateGoal(UUID userId, UUID goalId, UpdateGoalRequest request) {
        Goal goal = findOwnedGoal(userId, goalId);
        goal.updateDetails(
                request.name().trim(),
                request.targetAmount(),
                request.currentAmount(),
                request.deadline(),
                request.description()
        );
        return goalMapper.toResponse(goalRepository().save(goal));
    }

    @Transactional
    public void deleteGoal(UUID userId, UUID goalId) {
        Goal goal = findOwnedGoal(userId, goalId);
        goalRepository().delete(goal);
    }

    @Transactional
    public GoalResponse updateProgress(UUID userId, UUID goalId, UpdateGoalProgressRequest request) {
        Goal goal = findOwnedGoal(userId, goalId);
        goal.updateProgress(request.currentAmount());
        return goalMapper.toResponse(goalRepository().save(goal));
    }

    public BigDecimal completionPercentage(BigDecimal currentAmount, BigDecimal targetAmount) {
        BigDecimal percentage = currentAmount.multiply(new BigDecimal("100"))
                .divide(targetAmount, 2, RoundingMode.HALF_UP);
        return percentage.min(new BigDecimal("100.00"));
    }

    private Goal findOwnedGoal(UUID userId, UUID goalId) {
        return goalRepository().findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new NotFoundException("Meta financeira nao encontrada"));
    }

    private GoalRepository goalRepository() {
        return goalRepository.getIfAvailable(() -> {
            throw new IllegalStateException("GoalRepository nao disponivel");
        });
    }
}
