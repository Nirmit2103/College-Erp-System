package edu.univ.erp.domain;

public record Instructor(
        long userId,
        String fullName,
        String department,
        String designation
) {
}

