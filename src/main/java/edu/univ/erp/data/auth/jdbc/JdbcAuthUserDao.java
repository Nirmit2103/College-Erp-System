package edu.univ.erp.data.auth.jdbc;

import edu.univ.erp.data.auth.AuthUserDao;
import edu.univ.erp.data.auth.AuthUserRecord;
import edu.univ.erp.domain.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcAuthUserDao implements AuthUserDao {

    private static final Logger log = LoggerFactory.getLogger(JdbcAuthUserDao.class);
    private final DataSource dataSource;

    public JdbcAuthUserDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<AuthUserRecord> findByUsername(String username) {
        String sql = "SELECT user_id, username, role, password_hash, active, last_login " +
                     "FROM auth_users WHERE username = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username.toLowerCase());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Error finding user by username: {}", username, e);
            throw new RuntimeException("Database error", e);
        }
        return Optional.empty();
    }

    @Override
    public void updateLastLogin(long userId) {
        String sql = "UPDATE auth_users SET last_login = CURRENT_TIMESTAMP WHERE user_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            log.error("Error updating last login for user: {}", userId, e);
            throw new RuntimeException("Database error", e);
        }
    }

    @Override
    public void recordFailedAttempt(String username) {
        String sql = "INSERT INTO auth_login_attempts (username, success) VALUES (?, FALSE)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username.toLowerCase());
            stmt.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            log.error("Error recording failed attempt for: {}", username, e);

        }
    }

    @Override
    public void resetFailedAttempts(long userId) {

        log.debug("Resetting failed attempts for user: {}", userId);
    }

    @Override
    public long createUser(String username, UserRole role, String passwordHash, boolean active) {
        String sql = "INSERT INTO auth_users (username, role, password_hash, active) VALUES (?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, username.toLowerCase());
            stmt.setString(2, role.name());
            stmt.setString(3, passwordHash);
            stmt.setBoolean(4, active);
            stmt.executeUpdate();
            conn.commit();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
            throw new RuntimeException("Failed to get generated user ID");
        } catch (SQLException e) {
            log.error("Error creating user: {}", username, e);
            if (e.getSQLState().equals("23000")) {
                throw new IllegalArgumentException("Username already exists: " + username, e);
            }
            throw new RuntimeException("Database error", e);
        }
    }

    private AuthUserRecord mapRow(ResultSet rs) throws SQLException {
        long userId = rs.getLong("user_id");
        String username = rs.getString("username");
        UserRole role = UserRole.valueOf(rs.getString("role"));
        String passwordHash = rs.getString("password_hash");
        boolean active = rs.getBoolean("active");
        Timestamp lastLoginTs = rs.getTimestamp("last_login");
        OffsetDateTime lastLogin = lastLoginTs != null 
                ? OffsetDateTime.ofInstant(lastLoginTs.toInstant(), java.time.ZoneId.systemDefault())
                : null;

        return new AuthUserRecord(userId, username, role, passwordHash, active, lastLogin);
    }

    @Override
    public List<AuthUserRecord> listAllUsers() {
        String sql = "SELECT user_id, username, role, password_hash, active, last_login FROM auth_users ORDER BY user_id";
        List<AuthUserRecord> users = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                users.add(mapRow(rs));
            }
        } catch (SQLException e) {
            log.error("Error listing all users", e);
            throw new RuntimeException("Database error", e);
        }
        return users;
    }

    @Override
    public void deleteUser(long userId) {
        String sql = "DELETE FROM auth_users WHERE user_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            log.error("Error deleting user: {}", userId, e);
            throw new RuntimeException("Database error", e);
        }
    }

    @Override
    public void updatePassword(long userId, String newPasswordHash) {
        String sql = "UPDATE auth_users SET password_hash = ? WHERE user_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newPasswordHash);
            stmt.setLong(2, userId);
            stmt.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            log.error("Error updating password for user: {}", userId, e);
            throw new RuntimeException("Database error", e);
        }
    }

    @Override
    public int getFailedAttemptCount(String username) {
        String sql = "SELECT COUNT(*) FROM auth_login_attempts " +
                     "WHERE username = ? AND attempt_time > DATE_SUB(NOW(), INTERVAL 24 HOUR) AND success = FALSE";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username.toLowerCase());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            log.error("Error getting failed attempt count for: {}", username, e);

        }
        return 0;
    }
}
