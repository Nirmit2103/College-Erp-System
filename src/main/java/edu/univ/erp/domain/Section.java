package edu.univ.erp.domain;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record Section(
        long id,
        long courseId,
        long instructorId,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        String room,
        int capacity,
        String semester,
        int year
) {
}
