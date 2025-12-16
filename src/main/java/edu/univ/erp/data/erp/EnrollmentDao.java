package edu.univ.erp.data.erp;

import java.util.List;
import java.util.Optional;

import edu.univ.erp.domain.Enrollment;
import edu.univ.erp.domain.EnrollmentStatus;

public interface EnrollmentDao {

    Optional<Enrollment> findByStudentAndSection(long studentUserId, long sectionId);

    long countActiveEnrollments(long sectionId);

    Enrollment save(Enrollment enrollment);

    void updateStatus(long enrollmentId, EnrollmentStatus status);

    List<Enrollment> listByStudent(long studentUserId);

    List<Enrollment> listBySection(long sectionId);

    void deleteByStudentId(long studentUserId);

    void deleteById(long enrollmentId);
}

