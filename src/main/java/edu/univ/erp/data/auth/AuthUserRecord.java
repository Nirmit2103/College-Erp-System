package edu.univ.erp.data.auth;

import edu.univ.erp.domain.UserRole;

import java.time.OffsetDateTime;

public record AuthUserRecord(
        long userId,
        String username,
        UserRole role,
        String passwordHash,
        boolean active,
        OffsetDateTime lastLogin
) {
}

