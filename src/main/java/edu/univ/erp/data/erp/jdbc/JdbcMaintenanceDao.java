package edu.univ.erp.data.erp.jdbc;

import edu.univ.erp.data.erp.MaintenanceDao;
import edu.univ.erp.domain.MaintenanceSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneId;

public class JdbcMaintenanceDao implements MaintenanceDao {

    private static final Logger log = LoggerFactory.getLogger(JdbcMaintenanceDao.class);
    private final DataSource dataSource;
    private static final String SETTING_KEY = "maintenance_on";

    public JdbcMaintenanceDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public MaintenanceSetting fetch() {
        String sql = "SELECT setting_key, setting_value, updated_at FROM maintenance_settings WHERE setting_key = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, SETTING_KEY);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    boolean maintenanceOn = Boolean.parseBoolean(rs.getString("setting_value"));
                    OffsetDateTime updatedAt = rs.getTimestamp("updated_at") != null
                            ? OffsetDateTime.ofInstant(rs.getTimestamp("updated_at").toInstant(), ZoneId.systemDefault())
                            : null;

                    String updatedBy = "system";
                    return new MaintenanceSetting(maintenanceOn, updatedAt, updatedBy);
                }
            }
        } catch (SQLException e) {
            log.error("Error getting maintenance setting", e);
            throw new RuntimeException("Database error", e);
        }

        return new MaintenanceSetting(false, null, "system");
    }

    @Override
    public MaintenanceSetting update(boolean maintenanceOn, String updatedBy) {
        String sql = "INSERT INTO maintenance_settings (setting_key, setting_value) " +
                     "VALUES (?, ?) " +
                     "ON DUPLICATE KEY UPDATE setting_value = ?, updated_at = CURRENT_TIMESTAMP";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String value = String.valueOf(maintenanceOn);
            stmt.setString(1, SETTING_KEY);
            stmt.setString(2, value);
            stmt.setString(3, value);
            stmt.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            log.error("Error setting maintenance mode: {}", maintenanceOn, e);
            throw new RuntimeException("Database error", e);
        }

        return new MaintenanceSetting(maintenanceOn, OffsetDateTime.now(), updatedBy);
    }
}
