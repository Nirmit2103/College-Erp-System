package edu.univ.erp.data.erp.jdbc;

import edu.univ.erp.data.erp.EnrollmentDao;
import edu.univ.erp.domain.Enrollment;
import edu.univ.erp.domain.EnrollmentStatus;
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

public class JdbcEnrollmentDao implements EnrollmentDao {

    private static final Logger log = LoggerFactory.getLogger(JdbcEnrollmentDao.class);
    private final DataSource dataSource;

    public JdbcEnrollmentDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<Enrollment> findByStudentAndSection(long studentUserId, long sectionId) {
        String sql = "SELECT enrollment_id, student_id, section_id, status, enrolled_at, dropped_at " +
                     "FROM enrollments WHERE student_id = ? AND section_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, studentUserId);
            stmt.setLong(2, sectionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Error finding enrollment: student={}, section={}", studentUserId, sectionId, e);
            throw new RuntimeException("Database error", e);
        }
        return Optional.empty();
    }

    @Override
    public long countActiveEnrollments(long sectionId) {
        String sql = "SELECT COUNT(*) FROM enrollments WHERE section_id = ? AND status = 'ACTIVE'";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, sectionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            log.error("Error counting enrollments for section: {}", sectionId, e);
            throw new RuntimeException("Database error", e);
        }
        return 0;
    }

    @Override
    public Enrollment save(Enrollment enrollment) {
        if (enrollment.id() == 0) {
            return create(enrollment);
        } else {
            return update(enrollment);
        }
    }

    private Enrollment create(Enrollment enrollment) {
        String sql = "INSERT INTO enrollments (student_id, section_id, status) VALUES (?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, enrollment.studentId());
            stmt.setLong(2, enrollment.sectionId());
            stmt.setString(3, enrollment.status().name());
            stmt.executeUpdate();
            conn.commit();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    long enrollmentId = rs.getLong(1);
                    return new Enrollment(enrollmentId, enrollment.studentId(), enrollment.sectionId(),
                            enrollment.status());
                }
            }
            throw new RuntimeException("Failed to get generated enrollment ID");
        } catch (SQLException e) {
            log.error("Error creating enrollment", e);
            if ("23000".equals(e.getSQLState())) {
                throw new IllegalArgumentException("Duplicate enrollment: student=" + enrollment.studentId() +
                        ", section=" + enrollment.sectionId(), e);
            }
            throw new RuntimeException("Database error", e);
        }
    }

    private Enrollment update(Enrollment enrollment) {
        String sql = "UPDATE enrollments SET status = ? WHERE enrollment_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, enrollment.status().name());
            stmt.setLong(2, enrollment.id());
            stmt.executeUpdate();
            conn.commit();
            return enrollment;
        } catch (SQLException e) {
            log.error("Error updating enrollment: {}", enrollment.id(), e);
            throw new RuntimeException("Database error", e);
        }
    }

    @Override
    public void updateStatus(long enrollmentId, EnrollmentStatus status) {
        String sql = "UPDATE enrollments SET status = ?, dropped_at = ? WHERE enrollment_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            if (status == EnrollmentStatus.DROPPED) {
                stmt.setTimestamp(2, java.sql.Timestamp.from(java.time.Instant.now()));
            } else {
                stmt.setTimestamp(2, null);
            }
            stmt.setLong(3, enrollmentId);
            stmt.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            log.error("Error updating enrollment status: {}", enrollmentId, e);
            throw new RuntimeException("Database error", e);
        }
    }

    @Override
    public List<Enrollment> listByStudent(long studentUserId) {
        String sql = "SELECT enrollment_id, student_id, section_id, status " +
                     "FROM enrollments WHERE student_id = ? ORDER BY enrollment_id DESC";
        List<Enrollment> enrollments = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, studentUserId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    enrollments.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Error listing enrollments for student: {}", studentUserId, e);
            throw new RuntimeException("Database error", e);
        }
        return enrollments;
    }

    @Override
    public List<Enrollment> listBySection(long sectionId) {
        String sql = "SELECT enrollment_id, student_id, section_id, status " +
                     "FROM enrollments WHERE section_id = ? ORDER BY enrollment_id";
        List<Enrollment> enrollments = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, sectionId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    enrollments.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Error listing enrollments for section: {}", sectionId, e);
            throw new RuntimeException("Database error", e);
        }
        return enrollments;
    }

    @Override
    public void deleteByStudentId(long studentUserId) {
        String sql = "DELETE FROM enrollments WHERE student_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, studentUserId);
            stmt.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            log.error("Error deleting enrollments for student: {}", studentUserId, e);
            throw new RuntimeException("Database error", e);
        }
    }

    @Override
    public void deleteById(long enrollmentId) {
        String sql = "DELETE FROM enrollments WHERE enrollment_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, enrollmentId);
            stmt.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            log.error("Error deleting enrollment id {}", enrollmentId, e);
            throw new RuntimeException("Database error", e);
        }
    }

    private Enrollment mapRow(ResultSet rs) throws SQLException {
        long enrollmentId = rs.getLong("enrollment_id");
        long studentId = rs.getLong("student_id");
        long sectionId = rs.getLong("section_id");
        EnrollmentStatus status = EnrollmentStatus.valueOf(rs.getString("status"));
        return new Enrollment(enrollmentId, studentId, sectionId, status);
    }
}
