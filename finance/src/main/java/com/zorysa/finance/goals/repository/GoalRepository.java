package com.zorysa.finance.goals.repository;

import com.zorysa.finance.goals.entity.Goal;
import com.zorysa.finance.goals.entity.GoalStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GoalRepository extends JpaRepository<Goal, UUID> {

    Optional<Goal> findByIdAndUserId(UUID id, UUID userId);

    Page<Goal> findAllByUserId(UUID userId, Pageable pageable);

    Page<Goal> findAllByUserIdAndStatus(UUID userId, GoalStatus status, Pageable pageable);
}
