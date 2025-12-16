package edu.univ.erp.api.types;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record SectionRow(
        long sectionId,
        String courseCode,
        String courseTitle,
        int credits,
        String instructorName,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        String room,
        int capacity,
        String semester,
        int year
) {
}

