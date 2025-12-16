package edu.univ.erp.domain;

public record FinalGrade(
        long enrollmentId,
        double percentage,
        String letterGrade
) {
}

