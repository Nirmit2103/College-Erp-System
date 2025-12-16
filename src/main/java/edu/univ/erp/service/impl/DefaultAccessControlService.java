package edu.univ.erp.service.impl;

import edu.univ.erp.auth.AuthenticatedUser;
import edu.univ.erp.domain.UserRole;
import edu.univ.erp.service.AccessControlService;
import edu.univ.erp.service.exception.AccessDeniedException;

import java.util.EnumSet;

public class DefaultAccessControlService implements AccessControlService {

    @Override
    public void requireRole(AuthenticatedUser user, UserRole role) {
        if (user == null) {
            throw new AccessDeniedException("You must be logged in to perform this action.");
        }
        if (user.role() != role) {
            throw new AccessDeniedException("You are not allowed to perform this action.");
        }
    }

    public void requireRoleIn(AuthenticatedUser user, EnumSet<UserRole> roles) {
        if (user == null) {
            throw new AccessDeniedException("You must be logged in to perform this action.");
        }
        if (!roles.contains(user.role())) {
            throw new AccessDeniedException("You are not allowed to perform this action.");
        }
    }

    @Override
    public void requireMaintenanceOff(boolean maintenanceOn) {
        if (maintenanceOn) {
            throw new AccessDeniedException("Maintenance mode is currently ON. Please try again later.");
        }
    }

    @Override
    public void requireOwnership(long ownerId, long currentUserId) {
        if (ownerId != currentUserId) {
            throw new AccessDeniedException("You are not allowed to access this record.");
        }
    }
}

