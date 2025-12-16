package edu.univ.erp.data.erp.jdbc;

import edu.univ.erp.data.erp.SectionDao;
import edu.univ.erp.domain.Section;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcSectionDao implements SectionDao {

    private static final Logger log = LoggerFactory.getLogger(JdbcSectionDao.class);
    private final DataSource dataSource;

    public JdbcSectionDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<Section> listAll() {
        String sql = "SELECT section_id, course_id, instructor_id, day_of_week, start_time, end_time, " +
                     "room, capacity, semester, year FROM sections ORDER BY year, semester, course_id";
        List<Section> sections = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                sections.add(mapRow(rs));
            }
        } catch (SQLException e) {
            log.error("Error listing sections", e);
            throw new RuntimeException("Database error", e);
        }
        return sections;
    }

    @Override
    public Optional<Section> findById(long id) {
        String sql = "SELECT section_id, course_id, instructor_id, day_of_week, start_time, end_time, " +
                     "room, capacity, semester, year FROM sections WHERE section_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Error finding section by ID: {}", id, e);
            throw new RuntimeException("Database error", e);
        }
        return Optional.empty();
    }

    @Override
    public Section create(long courseId, long instructorId, DayOfWeek dayOfWeek,
                         LocalTime startTime, LocalTime endTime, String room,
                         int capacity, String semester, int year) {
        String sql = "INSERT INTO sections (course_id, instructor_id, day_of_week, start_time, end_time, " +
                     "room, capacity, semester, year) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, courseId);
            stmt.setLong(2, instructorId);
            stmt.setString(3, dayOfWeek.name());
            stmt.setTime(4, java.sql.Time.valueOf(startTime));
            stmt.setTime(5, java.sql.Time.valueOf(endTime));
            stmt.setString(6, room);
            stmt.setInt(7, capacity);
            stmt.setString(8, semester);
            stmt.setInt(9, year);
            stmt.executeUpdate();
            conn.commit();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    long sectionId = rs.getLong(1);
                    return new Section(sectionId, courseId, instructorId, dayOfWeek, startTime, endTime,
                            room, capacity, semester, year);
                }
            }
            throw new RuntimeException("Failed to get generated section ID");
        } catch (SQLException e) {
            log.error("Error creating section", e);
            throw new RuntimeException("Database error", e);
        }
    }

    @Override
    public void updateInstructor(long sectionId, long instructorId) {
        String sql = "UPDATE sections SET instructor_id = ? WHERE section_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, instructorId);
            stmt.setLong(2, sectionId);
            int rows = stmt.executeUpdate();
            conn.commit();
            if (rows == 0) {
                throw new IllegalArgumentException("Section not found: " + sectionId);
            }
        } catch (SQLException e) {
            log.error("Error updating instructor for section: {}", sectionId, e);
            throw new RuntimeException("Database error", e);
        }
    }

    @Override
    public void deleteById(long sectionId) {
        String sql = "DELETE FROM sections WHERE section_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, sectionId);
            stmt.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            log.error("Error deleting section: {}", sectionId, e);
            throw new RuntimeException("Database error", e);
        }
    }

    private Section mapRow(ResultSet rs) throws SQLException {
        return new Section(
                rs.getLong("section_id"),
                rs.getLong("course_id"),
                rs.getLong("instructor_id"),
                DayOfWeek.valueOf(rs.getString("day_of_week")),
                rs.getTime("start_time").toLocalTime(),
                rs.getTime("end_time").toLocalTime(),
                rs.getString("room"),
                rs.getInt("capacity"),
                rs.getString("semester"),
                rs.getInt("year")
        );
    }
}
