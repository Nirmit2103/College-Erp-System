package edu.univ.erp.auth;

import edu.univ.erp.domain.UserRole;

public record AuthenticatedUser(
        long userId,
        String username,
        UserRole role,
        boolean maintenanceMode
) {
}

