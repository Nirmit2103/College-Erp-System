package edu.univ.erp.domain;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record TimetableEntry(
        long sectionId,
        String courseCode,
        String courseTitle,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        String room
) {
}

