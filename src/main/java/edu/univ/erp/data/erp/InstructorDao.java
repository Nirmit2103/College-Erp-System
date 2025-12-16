package edu.univ.erp.data.erp;

import java.util.List;
import java.util.Optional;

import edu.univ.erp.domain.Instructor;

public interface InstructorDao {

    Optional<Instructor> findByUserId(long userId);

    List<Instructor> listAll();

    Instructor create(long userId, String firstName, String lastName, String department);

    boolean deleteByUserId(long userId);
}

