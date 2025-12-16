package edu.univ.erp.service;

import edu.univ.erp.auth.AuthenticatedUser;
import edu.univ.erp.domain.UserRole;
import edu.univ.erp.service.exception.AccessDeniedException;

public interface AccessControlService {

    void requireRole(AuthenticatedUser user, UserRole role) throws AccessDeniedException;

    void requireMaintenanceOff(boolean maintenanceOn) throws AccessDeniedException;

    void requireOwnership(long ownerId, long currentUserId) throws AccessDeniedException;
}

