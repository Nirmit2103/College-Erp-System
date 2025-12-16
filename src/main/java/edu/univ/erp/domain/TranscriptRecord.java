package edu.univ.erp.domain;

public record TranscriptRecord(
        String courseCode,
        String courseTitle,
        int credits,
        double finalPercentage,
        String letterGrade,
        String semester,
        int year
) {
}

