package edu.univ.erp.domain;

public record Course(
        long id,
        String code,
        String title,
        int credits,
        String prerequisiteCode
) {
}

