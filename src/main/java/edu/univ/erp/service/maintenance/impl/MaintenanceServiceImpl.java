package edu.univ.erp.service.maintenance.impl;

import edu.univ.erp.access.MaintenanceModeGuard;
import edu.univ.erp.auth.AuthenticatedUser;
import edu.univ.erp.data.erp.MaintenanceDao;
import edu.univ.erp.domain.MaintenanceSetting;
import edu.univ.erp.domain.UserRole;
import edu.univ.erp.service.maintenance.MaintenanceService;
import edu.univ.erp.service.exception.AccessDeniedException;

public class MaintenanceServiceImpl implements MaintenanceService {

    private final MaintenanceDao maintenanceDao;
    private final MaintenanceModeGuard guard;

    public MaintenanceServiceImpl(MaintenanceDao maintenanceDao, MaintenanceModeGuard guard) {
        this.maintenanceDao = maintenanceDao;
        this.guard = guard;
    }

    @Override
    public MaintenanceSetting currentSetting() {
        MaintenanceSetting setting = maintenanceDao.fetch();
        guard.update(setting);
        return setting;
    }

    @Override
    public MaintenanceSetting toggle(AuthenticatedUser admin) {
        if (admin == null || admin.role() != UserRole.ADMIN) {
            throw new AccessDeniedException("Only administrators can toggle maintenance mode.");
        }
        MaintenanceSetting current = maintenanceDao.fetch();
        boolean newStatus = !current.maintenanceOn();
        MaintenanceSetting updated = maintenanceDao.update(newStatus, admin.username());
        guard.update(updated);
        return updated;
    }
}

