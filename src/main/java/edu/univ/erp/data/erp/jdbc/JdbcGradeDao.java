package edu.univ.erp.data.erp.jdbc;

import edu.univ.erp.data.erp.GradeDao;
import edu.univ.erp.domain.FinalGrade;
import edu.univ.erp.domain.GradeEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcGradeDao implements GradeDao {

    private static final Logger log = LoggerFactory.getLogger(JdbcGradeDao.class);
    private final DataSource dataSource;

    public JdbcGradeDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<GradeEntry> listByEnrollment(long enrollmentId) {
        String sql = "SELECT grade_entry_id, enrollment_id, assessment_id, score " +
                     "FROM grade_entries WHERE enrollment_id = ? ORDER BY assessment_id";
        List<GradeEntry> entries = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, enrollmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    entries.add(mapGradeEntry(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Error listing grade entries for enrollment: {}", enrollmentId, e);
            throw new RuntimeException("Database error", e);
        }
        return entries;
    }

    @Override
    public FinalGrade findFinalGrade(long enrollmentId) {
        String sql = "SELECT enrollment_id, final_percentage, letter_grade FROM final_grades WHERE enrollment_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, enrollmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapFinalGrade(rs);
                }
            }
        } catch (SQLException e) {
            log.error("Error finding final grade for enrollment: {}", enrollmentId, e);
            throw new RuntimeException("Database error", e);
        }
        return null;
    }

    @Override
    public void upsertScore(long enrollmentId, long componentId, double score) {
        String sql = "INSERT INTO grade_entries (enrollment_id, assessment_id, score) " +
                     "VALUES (?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE score = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, enrollmentId);
            stmt.setLong(2, componentId);
            stmt.setDouble(3, score);
            stmt.setDouble(4, score);
            stmt.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            log.error("Error upserting score: enrollment={}, component={}", enrollmentId, componentId, e);
            throw new RuntimeException("Database error", e);
        }
    }

    @Override
    public void deleteScoresForComponent(long sectionId, long componentId) {

        String sql = "DELETE ge FROM grade_entries ge " +
                     "JOIN enrollments e ON ge.enrollment_id = e.enrollment_id " +
                     "WHERE e.section_id = ? AND ge.assessment_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, sectionId);
            stmt.setLong(2, componentId);
            stmt.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            log.error("Error deleting scores for component: section={}, component={}", sectionId, componentId, e);
            throw new RuntimeException("Database error", e);
        }
    }

    @Override
    public void upsertFinalGrade(FinalGrade finalGrade) {
        String sql = "INSERT INTO final_grades (enrollment_id, final_percentage, letter_grade) " +
                     "VALUES (?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE final_percentage = ?, letter_grade = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, finalGrade.enrollmentId());
            stmt.setDouble(2, finalGrade.percentage());
            stmt.setString(3, finalGrade.letterGrade());
            stmt.setDouble(4, finalGrade.percentage());
            stmt.setString(5, finalGrade.letterGrade());
            stmt.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            log.error("Error upserting final grade: enrollment={}", finalGrade.enrollmentId(), e);
            throw new RuntimeException("Database error", e);
        }
    }

    @Override
    public void deleteByEnrollmentId(long enrollmentId) {
        String deleteEntries = "DELETE FROM grade_entries WHERE enrollment_id = ?";
        String deleteFinal = "DELETE FROM final_grades WHERE enrollment_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt1 = conn.prepareStatement(deleteEntries);
             PreparedStatement stmt2 = conn.prepareStatement(deleteFinal)) {
            stmt1.setLong(1, enrollmentId);
            stmt1.executeUpdate();
            stmt2.setLong(1, enrollmentId);
            stmt2.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            log.error("Error deleting grades for enrollment: {}", enrollmentId, e);
            throw new RuntimeException("Database error", e);
        }
    }

    private GradeEntry mapGradeEntry(ResultSet rs) throws SQLException {
        return new GradeEntry(
                rs.getLong("grade_entry_id"),
                rs.getLong("enrollment_id"),
                rs.getLong("assessment_id"),
                rs.getDouble("score")
        );
    }

    private FinalGrade mapFinalGrade(ResultSet rs) throws SQLException {
        return new FinalGrade(
                rs.getLong("enrollment_id"),
                rs.getDouble("final_percentage"),
                rs.getString("letter_grade")
        );
    }
}
