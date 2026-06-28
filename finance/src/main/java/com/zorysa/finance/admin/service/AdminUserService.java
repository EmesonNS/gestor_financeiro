package com.zorysa.finance.admin.service;

import com.zorysa.finance.admin.dto.AdminUserDetailsResponse;
import com.zorysa.finance.admin.dto.AdminUserResponse;
import com.zorysa.finance.admin.dto.UserStatusHistoryResponse;
import com.zorysa.finance.admin.entity.UserStatusAction;
import com.zorysa.finance.admin.entity.UserStatusHistory;
import com.zorysa.finance.admin.repository.UserStatusHistoryRepository;
import com.zorysa.finance.shared.exception.BadRequestException;
import com.zorysa.finance.users.entity.User;
import com.zorysa.finance.users.entity.UserStatus;
import com.zorysa.finance.users.repository.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final ObjectProvider<UserStatusHistoryRepository> historyRepository;
    private final Clock clock;

    public AdminUserService(UserRepository userRepository, ObjectProvider<UserStatusHistoryRepository> historyRepository, Clock clock) {
        this.userRepository = userRepository;
        this.historyRepository = historyRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public Page<AdminUserResponse> listUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<AdminUserResponse> listPendingUsers(Pageable pageable) {
        return userRepository.findByStatus(UserStatus.PENDING_APPROVAL, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public AdminUserDetailsResponse getUserDetails(UUID userId) {
        User user = findUser(userId);
        List<UserStatusHistoryResponse> history = historyRepository.stream()
                .flatMap(repository -> repository.findByUserOrderByCreatedAtDesc(user).stream())
                .map(this::toHistoryResponse)
                .toList();
        return new AdminUserDetailsResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                user.getStatus().name(),
                user.getCreatedAt(),
                user.getApprovedAt(),
                user.getRejectedAt(),
                user.getSuspendedAt(),
                user.getDeletedAt(),
                history
        );
    }

    @Transactional
    public AdminUserResponse approveUser(UUID userId, UUID adminUserId, String reason) {
        return changeStatus(userId, adminUserId, reason, UserStatusAction.APPROVED);
    }

    @Transactional
    public AdminUserResponse rejectUser(UUID userId, UUID adminUserId, String reason) {
        return changeStatus(userId, adminUserId, reason, UserStatusAction.REJECTED);
    }

    @Transactional
    public AdminUserResponse suspendUser(UUID userId, UUID adminUserId, String reason) {
        return changeStatus(userId, adminUserId, reason, UserStatusAction.SUSPENDED);
    }

    @Transactional
    public AdminUserResponse reactivateUser(UUID userId, UUID adminUserId, String reason) {
        return changeStatus(userId, adminUserId, reason, UserStatusAction.REACTIVATED);
    }

    @Transactional
    public AdminUserResponse deleteUser(UUID userId, UUID adminUserId, String reason) {
        return changeStatus(userId, adminUserId, reason, UserStatusAction.DELETED);
    }

    private AdminUserResponse changeStatus(UUID userId, UUID adminUserId, String reason, UserStatusAction action) {
        User user = findUser(userId);
        User adminUser = adminUserId == null ? null : userRepository.findById(adminUserId).orElse(null);
        UserStatus previousStatus = user.getStatus();
        Instant now = clock.instant();

        switch (action) {
            case APPROVED -> user.approve(now);
            case REJECTED -> user.reject(now);
            case SUSPENDED -> user.suspend(now);
            case REACTIVATED -> user.reactivate(now);
            case DELETED -> user.delete(now);
            case REGISTERED -> throw new BadRequestException("Acao administrativa invalida");
        }

        User saved = userRepository.save(user);
        historyRepository.ifAvailable(repository -> repository.save(new UserStatusHistory(saved, adminUser, previousStatus, saved.getStatus(), action, reason)));
        return toResponse(saved);
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("Usuario nao encontrado"));
    }

    private AdminUserResponse toResponse(User user) {
        return new AdminUserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                user.getStatus().name(),
                user.getCreatedAt(),
                user.getApprovedAt(),
                user.getRejectedAt(),
                user.getSuspendedAt()
        );
    }

    private UserStatusHistoryResponse toHistoryResponse(UserStatusHistory history) {
        UUID adminUserId = history.getAdminUser() == null ? null : history.getAdminUser().getId();
        return new UserStatusHistoryResponse(
                history.getId(),
                adminUserId,
                history.getPreviousStatus() == null ? null : history.getPreviousStatus().name(),
                history.getNewStatus().name(),
                history.getAction().name(),
                history.getReason(),
                history.getCreatedAt()
        );
    }
}
