package edu.univ.erp.domain;

import java.time.OffsetDateTime;

public record MaintenanceSetting(
        boolean maintenanceOn,
        OffsetDateTime lastUpdated,
        String updatedBy
) {
}

