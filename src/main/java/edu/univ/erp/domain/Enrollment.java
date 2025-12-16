package edu.univ.erp.domain;

public record Enrollment(
        long id,
        long studentId,
        long sectionId,
        EnrollmentStatus status
) {
}

