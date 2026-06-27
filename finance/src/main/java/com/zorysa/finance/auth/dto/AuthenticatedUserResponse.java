package com.zorysa.finance.auth.dto;

import java.util.UUID;

public record AuthenticatedUserResponse(
        UUID id,
        String name,
        String email,
        String role
) {
}
