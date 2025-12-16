package edu.univ.erp.service.instructor.dto;

import java.util.Map;

public record GradebookRow(
        long enrollmentId,
        long studentUserId,
        String rollNumber,
        Map<Long, Double> componentScores,
        Double finalPercentage,
        String letterGrade
) {
}

