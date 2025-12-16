package edu.univ.erp.service.instructor;

import edu.univ.erp.api.common.ApiResponse;
import edu.univ.erp.auth.AuthenticatedUser;
import edu.univ.erp.domain.AssessmentComponent;
import edu.univ.erp.domain.Section;
import edu.univ.erp.service.instructor.dto.GradebookRow;
import edu.univ.erp.service.instructor.dto.SectionStats;

import java.util.List;

public interface InstructorService {

    List<Section> mySections(AuthenticatedUser instructor);

    List<AssessmentComponent> listAssessments(AuthenticatedUser instructor, long sectionId);

    List<GradebookRow> gradebook(AuthenticatedUser instructor, long sectionId);

    ApiResponse<AssessmentComponent> defineAssessment(AuthenticatedUser instructor, long sectionId, String name, double weight);

    ApiResponse<Void> recordScore(AuthenticatedUser instructor, long sectionId, long enrollmentId, long componentId, double score);

    ApiResponse<Void> computeFinalGrades(AuthenticatedUser instructor, long sectionId);

    SectionStats stats(AuthenticatedUser instructor, long sectionId);
}

