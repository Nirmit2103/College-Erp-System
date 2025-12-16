package edu.univ.erp.service.instructor.dto;

public record SectionStats(
        int enrollmentCount,
        Double average,
        Double highest,
        Double lowest
) {
}

