package edu.univ.erp.domain;

public record GradeView(
        String courseCode,
        String courseTitle,
        String componentName,
        double score,
        Double finalPercentage,
        String letterGrade
) {
}

