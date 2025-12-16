package edu.univ.erp.data.erp;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import edu.univ.erp.domain.Section;

public interface SectionDao {

    List<Section> listAll();

    Optional<Section> findById(long id);

    Section create(long courseId, long instructorId, DayOfWeek dayOfWeek,
                   LocalTime startTime, LocalTime endTime, String room,
                   int capacity, String semester, int year);

    void updateInstructor(long sectionId, long instructorId);

    void deleteById(long sectionId);
}

