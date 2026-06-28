package com.zorysa.finance.users.controller;

import com.zorysa.finance.auth.security.CurrentUser;
import com.zorysa.finance.users.dto.ChangePasswordRequest;
import com.zorysa.finance.users.dto.UpdateUserProfileRequest;
import com.zorysa.finance.users.dto.UserResponse;
import com.zorysa.finance.users.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserProfileController {

    private final UserService userService;
    private final CurrentUser currentUser;

    public UserProfileController(UserService userService, CurrentUser currentUser) {
        this.userService = userService;
        this.currentUser = currentUser;
    }

    @GetMapping("/me")
    UserResponse getAuthenticatedProfile() {
        return userService.getAuthenticatedProfile(currentUser.id());
    }

    @PutMapping("/me")
    UserResponse updateAuthenticatedProfile(@Valid @RequestBody UpdateUserProfileRequest request) {
        return userService.updateAuthenticatedProfile(currentUser.id(), request);
    }

    @PutMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void changeAuthenticatedPassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changeAuthenticatedPassword(currentUser.id(), request);
    }
}
