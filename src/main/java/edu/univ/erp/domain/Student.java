package edu.univ.erp.domain;

public record Student(
        long userId,
        String rollNumber,
        String program,
        int year
) {
}

