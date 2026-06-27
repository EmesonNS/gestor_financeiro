package com.zorysa.finance.shared.exception;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        String code,
        String userStatus,
        List<String> details
) {
    public ApiErrorResponse(
            Instant timestamp,
            int status,
            String error,
            String message,
            String path,
            List<String> details
    ) {
        this(timestamp, status, error, message, path, null, null, details);
    }
}
