package edu.univ.erp.data.erp;

import edu.univ.erp.domain.MaintenanceSetting;

import java.time.OffsetDateTime;
import java.util.concurrent.atomic.AtomicReference;

public class InMemoryMaintenanceDao implements MaintenanceDao {

    private final AtomicReference<MaintenanceSetting> state =
            new AtomicReference<>(new MaintenanceSetting(false, OffsetDateTime.now(), "system"));

    @Override
    public MaintenanceSetting fetch() {
        return state.get();
    }

    @Override
    public MaintenanceSetting update(boolean maintenanceOn, String updatedBy) {
        MaintenanceSetting updated = new MaintenanceSetting(maintenanceOn, OffsetDateTime.now(), updatedBy);
        state.set(updated);
        return updated;
    }
}

