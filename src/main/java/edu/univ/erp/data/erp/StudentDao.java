package edu.univ.erp.data.erp;

import java.util.Optional;

import edu.univ.erp.domain.Student;

public interface StudentDao {

    Optional<Student> findByUserId(long userId);

    Student create(long userId, String rollNo, String program, int year);

    void deleteByUserId(long userId);
}

