package edu.univ.erp.domain;

public record GradeEntry(
        long id,
        long enrollmentId,
        long componentId,
        double score
) {
}

