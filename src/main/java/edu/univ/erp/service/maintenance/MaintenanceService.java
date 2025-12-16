package edu.univ.erp.service.maintenance;

import edu.univ.erp.auth.AuthenticatedUser;
import edu.univ.erp.domain.MaintenanceSetting;

public interface MaintenanceService {

    MaintenanceSetting currentSetting();

    MaintenanceSetting toggle(AuthenticatedUser admin);
}

