package com.zorysa.finance.goals.controller;

import com.zorysa.finance.auth.security.CurrentUser;
import com.zorysa.finance.goals.dto.CreateGoalRequest;
import com.zorysa.finance.goals.dto.GoalResponse;
import com.zorysa.finance.goals.dto.UpdateGoalProgressRequest;
import com.zorysa.finance.goals.dto.UpdateGoalRequest;
import com.zorysa.finance.goals.entity.GoalStatus;
import com.zorysa.finance.goals.service.GoalService;
import com.zorysa.finance.shared.dto.PageResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/goals")
public class GoalController {

    private final GoalService goalService;
    private final CurrentUser currentUser;

    public GoalController(GoalService goalService, CurrentUser currentUser) {
        this.goalService = goalService;
        this.currentUser = currentUser;
    }

    @GetMapping
    PageResponse<GoalResponse> listGoals(
            @RequestParam(required = false) GoalStatus status,
            Pageable pageable
    ) {
        return PageResponse.from(goalService.listGoals(currentUser.id(), status, pageable));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    GoalResponse createGoal(@Valid @RequestBody CreateGoalRequest request) {
        return goalService.createGoal(currentUser.id(), request);
    }

    @GetMapping("/{id}")
    GoalResponse getGoal(@PathVariable UUID id) {
        return goalService.getGoal(currentUser.id(), id);
    }

    @PutMapping("/{id}")
    GoalResponse updateGoal(@PathVariable UUID id, @Valid @RequestBody UpdateGoalRequest request) {
        return goalService.updateGoal(currentUser.id(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteGoal(@PathVariable UUID id) {
        goalService.deleteGoal(currentUser.id(), id);
    }

    @PatchMapping("/{id}/progress")
    GoalResponse updateProgress(@PathVariable UUID id, @Valid @RequestBody UpdateGoalProgressRequest request) {
        return goalService.updateProgress(currentUser.id(), id, request);
    }
}
