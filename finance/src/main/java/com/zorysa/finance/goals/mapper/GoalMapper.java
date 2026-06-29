package com.zorysa.finance.goals.mapper;

import com.zorysa.finance.goals.dto.GoalResponse;
import com.zorysa.finance.goals.entity.Goal;
import org.springframework.stereotype.Component;

@Component
public class GoalMapper {

    public GoalResponse toResponse(Goal goal) {
        return new GoalResponse(
                goal.getId(),
                goal.getName(),
                goal.getTargetAmount(),
                goal.getCurrentAmount(),
                goal.getDeadline(),
                goal.getDescription(),
                goal.getStatus(),
                goal.completionPercentage(),
                goal.getCreatedAt(),
                goal.getUpdatedAt()
        );
    }
}
