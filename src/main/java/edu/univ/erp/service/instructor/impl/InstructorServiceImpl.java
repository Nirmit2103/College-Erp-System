package edu.univ.erp.service.instructor.impl;

import edu.univ.erp.access.MaintenanceModeGuard;
import edu.univ.erp.api.common.ApiResponse;
import edu.univ.erp.auth.AuthenticatedUser;
import edu.univ.erp.data.auth.AuthUserDao;
import edu.univ.erp.data.erp.AssessmentDao;
import edu.univ.erp.data.erp.EnrollmentDao;
import edu.univ.erp.data.erp.GradeDao;
import edu.univ.erp.data.erp.InstructorDao;
import edu.univ.erp.data.erp.SectionDao;
import edu.univ.erp.data.erp.StudentDao;
import edu.univ.erp.domain.AssessmentComponent;
import edu.univ.erp.domain.Enrollment;
import edu.univ.erp.domain.FinalGrade;
import edu.univ.erp.domain.GradeEntry;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.Student;
import edu.univ.erp.domain.UserRole;
import edu.univ.erp.service.AccessControlService;
import edu.univ.erp.service.exception.AccessDeniedException;
import edu.univ.erp.service.instructor.InstructorService;
import edu.univ.erp.service.instructor.dto.GradebookRow;
import edu.univ.erp.service.instructor.dto.SectionStats;

import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class InstructorServiceImpl implements InstructorService {

    private final SectionDao sectionDao;
    private final EnrollmentDao enrollmentDao;
    private final AssessmentDao assessmentDao;
    private final GradeDao gradeDao;
    private final StudentDao studentDao;
    private final InstructorDao instructorDao;
    private final AuthUserDao authUserDao;
    private final AccessControlService accessControlService;
    private final MaintenanceModeGuard maintenanceGuard;
    public InstructorServiceImpl(SectionDao sectionDao,
                                 EnrollmentDao enrollmentDao,
                                 AssessmentDao assessmentDao,
                                 GradeDao gradeDao,
                                 StudentDao studentDao,
                                 InstructorDao instructorDao,
                                 AuthUserDao authUserDao,
                                 AccessControlService accessControlService,
                                 MaintenanceModeGuard maintenanceGuard) {
        this.sectionDao = sectionDao;
        this.enrollmentDao = enrollmentDao;
        this.assessmentDao = assessmentDao;
        this.gradeDao = gradeDao;
        this.studentDao = studentDao;
        this.instructorDao = instructorDao;
        this.authUserDao = authUserDao;
        this.accessControlService = accessControlService;
        this.maintenanceGuard = maintenanceGuard;
    }

    public InstructorServiceImpl(SectionDao sectionDao,
                                 EnrollmentDao enrollmentDao,
                                 AssessmentDao assessmentDao,
                                 GradeDao gradeDao,
                                 StudentDao studentDao,
                                 AccessControlService accessControlService,
                                 MaintenanceModeGuard maintenanceGuard) {
        this(sectionDao, enrollmentDao, assessmentDao, gradeDao, studentDao, null, null, accessControlService, maintenanceGuard);
    }

    @Override
    public List<Section> mySections(AuthenticatedUser instructor) {
        accessControlService.requireRole(instructor, UserRole.INSTRUCTOR);
        return sectionDao.listAll().stream()
                .filter(section -> section.instructorId() == instructor.userId())
                .sorted(Comparator.comparing(Section::semester)
                        .thenComparing(Section::year)
                        .thenComparing(Section::id))
                .collect(Collectors.toList());
    }

    @Override
    public List<AssessmentComponent> listAssessments(AuthenticatedUser instructor, long sectionId) {
        ensureInstructorOwnsSection(instructor, sectionId);
        List<AssessmentComponent> existing = assessmentDao.listBySection(sectionId);
        if (existing == null || existing.isEmpty()) {

            assessmentDao.create(sectionId, "Quiz 1", 20.0);
            assessmentDao.create(sectionId, "Quiz 2", 15.0);
            assessmentDao.create(sectionId, "Midsem", 30.0);
            assessmentDao.create(sectionId, "Endsem", 35.0);
            existing = assessmentDao.listBySection(sectionId);
        }
        return existing;
    }

    @Override
    public List<GradebookRow> gradebook(AuthenticatedUser instructor, long sectionId) {
        ensureInstructorOwnsSection(instructor, sectionId);
        List<Enrollment> enrollments = enrollmentDao.listBySection(sectionId);

        return enrollments.stream()
                .map(enrollment -> {
                    Student student = studentDao.findByUserId(enrollment.studentId())
                            .orElse(new Student(enrollment.studentId(), "UNKNOWN", "Unknown", 0));
                    List<GradeEntry> entries = gradeDao.listByEnrollment(enrollment.id());
                    Map<Long, Double> scores = entries.stream()
                            .collect(Collectors.toMap(
                                    GradeEntry::componentId,
                                    GradeEntry::score
                            ));
                    FinalGrade finalGrade = gradeDao.findFinalGrade(enrollment.id());
                    Double finalPercentage = finalGrade != null ? finalGrade.percentage() : null;
                    String letter = finalGrade != null ? finalGrade.letterGrade() : null;

                    return new GradebookRow(
                            enrollment.id(),
                            enrollment.studentId(),
                            student.rollNumber(),
                            scores,
                            finalPercentage,
                            letter
                    );
                })
                .sorted(Comparator.comparing(GradebookRow::rollNumber))
                .collect(Collectors.toList());
    }

    @Override
    public ApiResponse<AssessmentComponent> defineAssessment(AuthenticatedUser instructor,
                                                             long sectionId,
                                                             String name,
                                                             double weight) {

        return ApiResponse.failure("NOT_ALLOWED", "Assessments are fixed for this course. Edit not permitted.");
    }

    @Override
    public ApiResponse<Void> recordScore(AuthenticatedUser instructor,
                                         long sectionId,
                                         long enrollmentId,
                                         long componentId,
                                         double score) {
        ensureInstructorOwnsSection(instructor, sectionId);
        accessControlService.requireMaintenanceOff(maintenanceGuard.isMaintenanceOn());

        if (score < 0 || score > 100) {
            return ApiResponse.failure("VALIDATION", "Score must be between 0 and 100.");
        }

        AssessmentComponent component = assessmentDao.findById(componentId)
                .orElse(null);
        if (component == null || component.sectionId() != sectionId) {
            return ApiResponse.failure("NOT_FOUND", "Assessment component not found for this section.");
        }

        Optional<Enrollment> enrollmentOpt = enrollmentDao.listBySection(sectionId).stream()
                .filter(e -> e.id() == enrollmentId)
                .findFirst();
        if (enrollmentOpt.isEmpty()) {
            return ApiResponse.failure("NOT_FOUND", "Enrollment not found for this section.");
        }

        gradeDao.upsertScore(enrollmentId, componentId, score);
        return ApiResponse.success(null);
    }

    @Override
    public ApiResponse<Void> computeFinalGrades(AuthenticatedUser instructor, long sectionId) {
        ensureInstructorOwnsSection(instructor, sectionId);
        accessControlService.requireMaintenanceOff(maintenanceGuard.isMaintenanceOn());
        List<AssessmentComponent> components = assessmentDao.listBySection(sectionId);
        if (components.isEmpty()) {
            return ApiResponse.failure("NO_COMPONENTS", "Define assessments before computing final grades.");
        }

        double totalWeight = components.stream().mapToDouble(AssessmentComponent::weightPercentage).sum();
        if (totalWeight == 0) {
            return ApiResponse.failure("INVALID_WEIGHTS", "Assessment component weights must sum to more than 0.");
        }

        List<Enrollment> enrollments = enrollmentDao.listBySection(sectionId);
        for (Enrollment enrollment : enrollments) {
            Map<Long, Double> scores = gradeDao.listByEnrollment(enrollment.id()).stream()
                    .collect(Collectors.toMap(GradeEntry::componentId, GradeEntry::score, (a, b) -> b));

            double weightedSum = 0.0;
            double totalUsedWeight = 0.0;

            for (AssessmentComponent component : components) {
                Double score = scores.get(component.id());
                if (score != null) {

                    weightedSum += (score * component.weightPercentage() / 100.0);
                    totalUsedWeight += component.weightPercentage();
                }
            }

            double finalPercentage = totalUsedWeight > 0 ? weightedSum : 0.0;
            String letter = toLetterGrade(finalPercentage);
            gradeDao.upsertFinalGrade(new FinalGrade(enrollment.id(), finalPercentage, letter));
        }

        return ApiResponse.success(null);
    }

    @Override
    public SectionStats stats(AuthenticatedUser instructor, long sectionId) {
        ensureInstructorOwnsSection(instructor, sectionId);

        List<Enrollment> enrollments = enrollmentDao.listBySection(sectionId);
        if (enrollments.isEmpty()) {
            return new SectionStats(0, null, null, null);
        }

        DoubleSummaryStatistics stats = enrollments.stream()
                .map(enrollment -> gradeDao.findFinalGrade(enrollment.id()))
                .filter(java.util.Objects::nonNull)
                .mapToDouble(FinalGrade::percentage)
                .summaryStatistics();

        if (stats.getCount() == 0) {
            return new SectionStats(enrollments.size(), null, null, null);
        }

        return new SectionStats(
                enrollments.size(),
                stats.getAverage(),
                stats.getMax(),
                stats.getMin()
        );
    }

    private void ensureInstructorOwnsSection(AuthenticatedUser instructor, long sectionId) {
        accessControlService.requireRole(instructor, UserRole.INSTRUCTOR);
        Optional<Section> sectionOpt = sectionDao.findById(sectionId);
        if (sectionOpt.isEmpty() || sectionOpt.get().instructorId() != instructor.userId()) {
            throw new AccessDeniedException("Not your section.");
        }
    }

    private String toLetterGrade(double percentage) {
        if (percentage >= 85) {
            return "A";
        } else if (percentage >= 75) {
            return "B+";
        } else if (percentage >= 65) {
            return "B";
        } else if (percentage >= 55) {
            return "C";
        } else if (percentage >= 45) {
            return "D";
        }
        return "F";
    }
}
