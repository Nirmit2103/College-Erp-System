package edu.univ.erp.data.erp.jdbc;

import edu.univ.erp.data.erp.AssessmentDao;
import edu.univ.erp.domain.AssessmentComponent;
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

public class JdbcAssessmentDao implements AssessmentDao {

    private static final Logger log = LoggerFactory.getLogger(JdbcAssessmentDao.class);
    private final DataSource dataSource;

    public JdbcAssessmentDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<AssessmentComponent> listBySection(long sectionId) {
        String sql = "SELECT assessment_id, section_id, name, weight FROM assessments WHERE section_id = ? ORDER BY assessment_id";
        List<AssessmentComponent> assessments = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, sectionId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    assessments.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Error listing assessments for section: {}", sectionId, e);
            throw new RuntimeException("Database error", e);
        }
        return assessments;
    }

    @Override
    public AssessmentComponent create(long sectionId, String name, double weight) {
        String sql = "INSERT INTO assessments (section_id, name, weight) VALUES (?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, sectionId);
            stmt.setString(2, name);
            stmt.setDouble(3, weight);
            stmt.executeUpdate();
            conn.commit();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    long assessmentId = rs.getLong(1);
                    return new AssessmentComponent(assessmentId, sectionId, name, weight);
                }
            }
            throw new RuntimeException("Failed to get generated assessment ID");
        } catch (SQLException e) {
            log.error("Error creating assessment", e);
            throw new RuntimeException("Database error", e);
        }
    }

    @Override
    public void update(AssessmentComponent component) {
        String sql = "UPDATE assessments SET name = ?, weight = ? WHERE assessment_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, component.name());
            stmt.setDouble(2, component.weightPercentage());
            stmt.setLong(3, component.id());
            int rows = stmt.executeUpdate();
            conn.commit();
            if (rows == 0) {
                throw new IllegalArgumentException("Assessment not found: " + component.id());
            }
        } catch (SQLException e) {
            log.error("Error updating assessment: {}", component.id(), e);
            throw new RuntimeException("Database error", e);
        }
    }

    @Override
    public void delete(long componentId) {
        String sql = "DELETE FROM assessments WHERE assessment_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, componentId);
            int rows = stmt.executeUpdate();
            conn.commit();
            if (rows == 0) {
                throw new IllegalArgumentException("Assessment not found: " + componentId);
            }
        } catch (SQLException e) {
            log.error("Error deleting assessment: {}", componentId, e);
            throw new RuntimeException("Database error", e);
        }
    }

    @Override
    public Optional<AssessmentComponent> findById(long componentId) {
        String sql = "SELECT assessment_id, section_id, name, weight FROM assessments WHERE assessment_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, componentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Error finding assessment by ID: {}", componentId, e);
            throw new RuntimeException("Database error", e);
        }
        return Optional.empty();
    }

    private AssessmentComponent mapRow(ResultSet rs) throws SQLException {
        return new AssessmentComponent(
                rs.getLong("assessment_id"),
                rs.getLong("section_id"),
                rs.getString("name"),
                rs.getDouble("weight")
        );
    }
}
