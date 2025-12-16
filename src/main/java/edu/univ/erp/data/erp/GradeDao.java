package edu.univ.erp.data.erp;

import java.util.List;

import edu.univ.erp.domain.FinalGrade;
import edu.univ.erp.domain.GradeEntry;

public interface GradeDao {

    List<GradeEntry> listByEnrollment(long enrollmentId);

    void upsertScore(long enrollmentId, long componentId, double score);

    void deleteScoresForComponent(long sectionId, long componentId);

    FinalGrade findFinalGrade(long enrollmentId);

    void upsertFinalGrade(FinalGrade finalGrade);

    void deleteByEnrollmentId(long enrollmentId);
}

