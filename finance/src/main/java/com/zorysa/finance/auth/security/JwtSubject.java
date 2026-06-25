package com.zorysa.finance.auth.security;

import java.util.UUID;

public record JwtSubject(UUID userId, String email) {
}
