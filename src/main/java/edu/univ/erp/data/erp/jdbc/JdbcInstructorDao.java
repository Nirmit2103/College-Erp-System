package edu.univ.erp.data.erp.jdbc;

import edu.univ.erp.data.erp.InstructorDao;
import edu.univ.erp.domain.Instructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcInstructorDao implements InstructorDao {

    private static final Logger log = LoggerFactory.getLogger(JdbcInstructorDao.class);
    private final DataSource dataSource;

    public JdbcInstructorDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<Instructor> findByUserId(long userId) {
        String sql = "SELECT user_id, first_name, last_name, department, title FROM instructors WHERE user_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Error finding instructor by user ID: {}", userId, e);
            throw new RuntimeException("Database error", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Instructor> listAll() {
        String sql = "SELECT user_id, first_name, last_name, department, title FROM instructors ORDER BY last_name, first_name";
        List<Instructor> instructors = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                instructors.add(mapRow(rs));
            }
        } catch (SQLException e) {
            log.error("Error listing instructors", e);
            throw new RuntimeException("Database error", e);
        }
        return instructors;
    }

    @Override
    public Instructor create(long userId, String firstName, String lastName, String department) {
        String sql = "INSERT INTO instructors (user_id, first_name, last_name, department, title) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.setString(2, firstName);
            stmt.setString(3, lastName);
            stmt.setString(4, department);
            stmt.setString(5, "Assistant Professor");
            stmt.executeUpdate();
            conn.commit();
            String fullName = firstName + " " + lastName;
            return new Instructor(userId, fullName, department, "Assistant Professor");
        } catch (SQLException e) {
            log.error("Error creating instructor: {}", userId, e);
            if (e.getSQLState().equals("23000")) {
                throw new IllegalArgumentException("Instructor profile already exists for user: " + userId, e);
            }
            throw new RuntimeException("Database error", e);
        }
    }

    @Override
    public boolean deleteByUserId(long userId) {
        String sql = "DELETE FROM instructors WHERE user_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            int affected = stmt.executeUpdate();
            conn.commit();
            return affected > 0;
        } catch (SQLException e) {
            log.error("Error deleting instructor by user ID: {}", userId, e);
            throw new RuntimeException("Database error", e);
        }
    }

    private Instructor mapRow(ResultSet rs) throws SQLException {
        long userId = rs.getLong("user_id");
        String firstName = rs.getString("first_name");
        String lastName = rs.getString("last_name");
        String department = rs.getString("department");
        String title = rs.getString("title");
        String fullName = firstName + " " + lastName;
        return new Instructor(userId, fullName, department, title);
    }
}

