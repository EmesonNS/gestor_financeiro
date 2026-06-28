package com.zorysa.finance.admin.controller;

import com.zorysa.finance.admin.dto.AdminStatusChangeRequest;
import com.zorysa.finance.admin.dto.AdminUserDetailsResponse;
import com.zorysa.finance.admin.dto.AdminUserResponse;
import com.zorysa.finance.admin.service.AdminUserService;
import com.zorysa.finance.auth.security.CurrentUser;
import com.zorysa.finance.shared.dto.PageResponse;
import com.zorysa.finance.shared.dto.PageResponse;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;
    private final CurrentUser currentUser;

    public AdminUserController(AdminUserService adminUserService, CurrentUser currentUser) {
        this.adminUserService = adminUserService;
        this.currentUser = currentUser;
    }

    @GetMapping
    PageResponse<AdminUserResponse> listUsers(Pageable pageable) {
        return PageResponse.from(adminUserService.listUsers(pageable));
    }

    @GetMapping("/pending")
    PageResponse<AdminUserResponse> listPendingUsers(Pageable pageable) {
        return PageResponse.from(adminUserService.listPendingUsers(pageable));
    }

    @GetMapping("/{userId}")
    AdminUserDetailsResponse getUserDetails(@PathVariable UUID userId) {
        return adminUserService.getUserDetails(userId);
    }

    @PatchMapping("/{userId}/approve")
    AdminUserResponse approveUser(@PathVariable UUID userId, @RequestBody(required = false) AdminStatusChangeRequest request) {
        return adminUserService.approveUser(userId, currentUser.id(), reasonOf(request));
    }

    @PatchMapping("/{userId}/reject")
    AdminUserResponse rejectUser(@PathVariable UUID userId, @RequestBody AdminStatusChangeRequest request) {
        return adminUserService.rejectUser(userId, currentUser.id(), reasonOf(request));
    }

    @PatchMapping("/{userId}/suspend")
    AdminUserResponse suspendUser(@PathVariable UUID userId, @RequestBody AdminStatusChangeRequest request) {
        return adminUserService.suspendUser(userId, currentUser.id(), reasonOf(request));
    }

    @PatchMapping("/{userId}/reactivate")
    AdminUserResponse reactivateUser(@PathVariable UUID userId, @RequestBody(required = false) AdminStatusChangeRequest request) {
        return adminUserService.reactivateUser(userId, currentUser.id(), reasonOf(request));
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteUser(@PathVariable UUID userId, @RequestBody(required = false) AdminStatusChangeRequest request) {
        adminUserService.deleteUser(userId, currentUser.id(), reasonOf(request));
    }

    private String reasonOf(AdminStatusChangeRequest request) {
        return request == null ? null : request.reason();
    }
}
