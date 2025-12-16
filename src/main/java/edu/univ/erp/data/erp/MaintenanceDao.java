package edu.univ.erp.data.erp;

import edu.univ.erp.domain.MaintenanceSetting;

public interface MaintenanceDao {

    MaintenanceSetting fetch();

    MaintenanceSetting update(boolean maintenanceOn, String updatedBy);
}

