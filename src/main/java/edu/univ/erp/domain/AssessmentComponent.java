package edu.univ.erp.domain;

public record AssessmentComponent(
        long id,
        long sectionId,
        String name,
        double weightPercentage
) {
}

