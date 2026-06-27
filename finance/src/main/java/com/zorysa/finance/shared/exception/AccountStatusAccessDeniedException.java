package com.zorysa.finance.shared.exception;

import org.springframework.security.access.AccessDeniedException;

public class AccountStatusAccessDeniedException extends AccessDeniedException {

    private final String code;
    private final String userStatus;

    public AccountStatusAccessDeniedException(String message, String code, String userStatus) {
        super(message);
        this.code = code;
        this.userStatus = userStatus;
    }

    public String getCode() {
        return code;
    }

    public String getUserStatus() {
        return userStatus;
    }
}
