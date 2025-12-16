package edu.univ.erp.data.erp.jdbc;

import edu.univ.erp.data.erp.CourseDao;
import edu.univ.erp.domain.Course;
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

public class JdbcCourseDao implements CourseDao {

    private static final Logger log = LoggerFactory.getLogger(JdbcCourseDao.class);
    private final DataSource dataSource;

    public JdbcCourseDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<Course> findById(long id) {
        String sql = "SELECT course_id, code, title, credits, prerequisite_code FROM courses WHERE course_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Error finding course by ID: {}", id, e);
            throw new RuntimeException("Database error", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Course> listAll() {
        String sql = "SELECT course_id, code, title, credits, prerequisite_code FROM courses ORDER BY code";
        List<Course> courses = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                courses.add(mapRow(rs));
            }
        } catch (SQLException e) {
            log.error("Error listing courses", e);
            throw new RuntimeException("Database error", e);
        }
        return courses;
    }

    @Override
    public Course create(String code, String title, int credits) {
        String sql = "INSERT INTO courses (code, title, credits) VALUES (?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, code);
            stmt.setString(2, title);
            stmt.setInt(3, credits);
            stmt.executeUpdate();
            conn.commit();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    long courseId = rs.getLong(1);
                    return new Course(courseId, code, title, credits, null);
                }
            }
            throw new RuntimeException("Failed to get generated course ID");
        } catch (SQLException e) {
            log.error("Error creating course: {}", code, e);
            if (e.getSQLState().equals("23000")) {
                throw new IllegalArgumentException("Course code already exists: " + code, e);
            }
            throw new RuntimeException("Database error", e);
        }
    }

    private Course mapRow(ResultSet rs) throws SQLException {
        String prerequisiteCode = rs.getString("prerequisite_code");
        return new Course(
                rs.getLong("course_id"),
                rs.getString("code"),
                rs.getString("title"),
                rs.getInt("credits"),
                prerequisiteCode
        );
    }

    @Override
    public void deleteById(long courseId) {
        String sql = "DELETE FROM courses WHERE course_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, courseId);
            stmt.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            log.error("Error deleting course: {}", courseId, e);
            throw new RuntimeException("Database error", e);
        }
    }
}

