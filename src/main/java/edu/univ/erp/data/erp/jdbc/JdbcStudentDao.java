package edu.univ.erp.data.erp.jdbc;

import edu.univ.erp.data.erp.StudentDao;
import edu.univ.erp.domain.Student;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class JdbcStudentDao implements StudentDao {

    private static final Logger log = LoggerFactory.getLogger(JdbcStudentDao.class);
    private final DataSource dataSource;

    public JdbcStudentDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void deleteByUserId(long userId) {
        String sql = "DELETE FROM students WHERE user_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.executeUpdate();                                                     
            conn.commit();
        } catch (SQLException e) {
            log.error("Error deleting student profile: {}", userId, e);
            throw new RuntimeException("Database error", e);
        }
    }

    @Override
    public Optional<Student> findByUserId(long userId) {
        String sql = "SELECT user_id, roll_no, program, year FROM students WHERE user_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Error finding student by user ID: {}", userId, e);
            throw new RuntimeException("Database error", e);
        }
        return Optional.empty();
    }

    @Override
    public Student create(long userId, String rollNo, String program, int year) {
        String sql = "INSERT INTO students (user_id, roll_no, program, year) VALUES (?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.setString(2, rollNo);
            stmt.setString(3, program);
            stmt.setInt(4, year);
            stmt.executeUpdate();
            conn.commit();
            return new Student(userId, rollNo, program, year);
        } catch (SQLException e) {
            log.error("Error creating student: {}", userId, e);
            if (e.getSQLState().equals("23000")) {
                throw new IllegalArgumentException("Student profile already exists for user: " + userId, e);
            }
            throw new RuntimeException("Database error", e);
        }
    }

    private Student mapRow(ResultSet rs) throws SQLException {
        return new Student(
                rs.getLong("user_id"),
                rs.getString("roll_no"),
                rs.getString("program"),
                rs.getInt("year")
        );
    }
}

