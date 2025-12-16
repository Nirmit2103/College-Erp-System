package edu.univ.erp.data.erp;

import java.util.List;
import java.util.Optional;

import edu.univ.erp.domain.Course;

public interface CourseDao {

    Optional<Course> findById(long id);

    List<Course> listAll();

    Course create(String code, String title, int credits);

    void deleteById(long courseId);
}

