package edu.univ.erp.service.student.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import edu.univ.erp.access.MaintenanceModeGuard;
import edu.univ.erp.api.common.ApiResponse;
import edu.univ.erp.api.common.PagedResult;
import edu.univ.erp.api.types.SectionRow;
import edu.univ.erp.auth.AuthenticatedUser;
import edu.univ.erp.data.erp.AssessmentDao;
import edu.univ.erp.data.erp.CourseDao;
import edu.univ.erp.data.erp.EnrollmentDao;
import edu.univ.erp.data.erp.GradeDao;
import edu.univ.erp.data.erp.InstructorDao;
import edu.univ.erp.data.erp.SectionDao;
import edu.univ.erp.data.erp.StudentDao;
import edu.univ.erp.domain.AssessmentComponent;
import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.Enrollment;
import edu.univ.erp.domain.EnrollmentStatus;
import edu.univ.erp.domain.FinalGrade;
import edu.univ.erp.domain.GradeEntry;
import edu.univ.erp.domain.GradeView;
import edu.univ.erp.domain.Instructor;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.Student;
import edu.univ.erp.domain.TimetableEntry;
import edu.univ.erp.domain.TranscriptRecord;
import edu.univ.erp.domain.UserRole;
import edu.univ.erp.service.AccessControlService;
import edu.univ.erp.service.student.StudentService;

public class StudentServiceImpl implements StudentService {

    private final SectionDao sectionDao;
    private final CourseDao courseDao;
    private final InstructorDao instructorDao;
    private final AccessControlService accessControlService;
    private final EnrollmentDao enrollmentDao;
    private final StudentDao studentDao;
    private final GradeDao gradeDao;
    private final MaintenanceModeGuard maintenanceGuard;
    private final AssessmentDao assessmentDao;

    public StudentServiceImpl(SectionDao sectionDao,
                              CourseDao courseDao,
                              InstructorDao instructorDao,
                              EnrollmentDao enrollmentDao,
                              StudentDao studentDao,
                              GradeDao gradeDao,
                              AccessControlService accessControlService,
                              MaintenanceModeGuard maintenanceGuard,
                              AssessmentDao assessmentDao) {
        this.sectionDao = sectionDao;
        this.courseDao = courseDao;
        this.instructorDao = instructorDao;
        this.accessControlService = accessControlService;
        this.enrollmentDao = enrollmentDao;
        this.studentDao = studentDao;
        this.gradeDao = gradeDao;
        this.maintenanceGuard = maintenanceGuard;
        this.assessmentDao = assessmentDao;
    }

    @Override
    public PagedResult<SectionRow> browseCatalog(AuthenticatedUser user, int page, int pageSize) {
        accessControlService.requireRole(user, UserRole.STUDENT);

        if (page < 0 || pageSize <= 0) {
            throw new IllegalArgumentException("Invalid pagination arguments");
        }

        List<SectionRow> rows = sectionDao.listAll().stream()
                .sorted(Comparator.comparing(Section::semester)
                        .thenComparing(Section::year)
                        .thenComparing(Section::id))
                .map(this::toRow)
                .collect(Collectors.toList());

        int fromIndex = Math.min(page * pageSize, rows.size());
        int toIndex = Math.min(fromIndex + pageSize, rows.size());
        List<SectionRow> pageItems = rows.subList(fromIndex, toIndex);

        return new PagedResult<>(pageItems, page, pageSize, rows.size());
    }

    private SectionRow toRow(Section section) {
        Course course = courseDao.findById(section.courseId())
                .orElse(new Course(section.courseId(), "UNKNOWN", "Unknown Course", 0, null));
        Optional<Instructor> instructorOpt = instructorDao.findByUserId(section.instructorId());
        String instructorName = instructorOpt.map(Instructor::fullName).orElse("TBA");

        long enrolledCount = enrollmentDao.countActiveEnrollments(section.id());

        String titleWithInfo = course.title();
        if (course.prerequisiteCode() != null && !course.prerequisiteCode().isEmpty()) {
            titleWithInfo += " [Prereq: " + course.prerequisiteCode() + "]";
        }
        titleWithInfo += " (" + enrolledCount + "/" + section.capacity() + ")";

        return new SectionRow(
                section.id(),
                course.code(),
                titleWithInfo,
                course.credits(),
                instructorName,
                section.dayOfWeek(),
                section.startTime(),
                section.endTime(),
                section.room(),
                section.capacity(),
                section.semester(),
                section.year()
        );
    }

    @Override
    public List<SectionRow> myRegistrations(AuthenticatedUser user) {
        accessControlService.requireRole(user, UserRole.STUDENT);

        return enrollmentDao.listByStudent(user.userId()).stream()
                .map(enrollment -> sectionDao.findById(enrollment.sectionId()).map(this::toRow))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    public List<TimetableEntry> timetable(AuthenticatedUser user) {
        accessControlService.requireRole(user, UserRole.STUDENT);

        return enrollmentDao.listByStudent(user.userId()).stream()
                .map(enrollment -> sectionDao.findById(enrollment.sectionId())
                        .flatMap(section -> courseDao.findById(section.courseId())
                                .map(course -> new TimetableEntry(
                                        section.id(),
                                        course.code(),
                                        course.title(),
                                        section.dayOfWeek(),
                                        section.startTime(),
                                        section.endTime(),
                                        section.room()
                                ))))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .sorted(Comparator.comparing(TimetableEntry::dayOfWeek)
                        .thenComparing(TimetableEntry::startTime))
                .collect(Collectors.toList());
    }

    @Override
    public List<GradeView> gradeReport(AuthenticatedUser user) {
        accessControlService.requireRole(user, UserRole.STUDENT);

        return enrollmentDao.listByStudent(user.userId()).stream()
                .flatMap(enrollment -> gradeViewsForEnrollment(enrollment).stream())
                .collect(Collectors.toList());
    }

    @Override
    public byte[] exportTranscriptCsv(AuthenticatedUser user) {
        accessControlService.requireRole(user, UserRole.STUDENT);
        List<TranscriptRecord> records = transcriptRecords(user);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             CSVPrinter printer = new CSVPrinter(new java.io.OutputStreamWriter(out, StandardCharsets.UTF_8),
                     CSVFormat.DEFAULT.builder().setHeader("Course Code", "Title", "Credits", "Final %", "Letter", "Semester", "Year").build())) {
            for (TranscriptRecord record : records) {
                printer.printRecord(
                        record.courseCode(),
                        record.courseTitle(),
                        record.credits(),
                        record.finalPercentage(),
                        record.letterGrade(),
                        record.semester(),
                        record.year()
                );
            }
            printer.flush();
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to generate transcript CSV", e);
        }
    }

    @Override
    public byte[] exportTranscriptPdf(AuthenticatedUser user) {
        accessControlService.requireRole(user, UserRole.STUDENT);
        List<TranscriptRecord> records = transcriptRecords(user);

        try (PDDocument document = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                PDType1Font titleFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                PDType1Font headerFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                PDType1Font bodyFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

                float y = 750;
                float margin = 50;
                float tableWidth = page.getMediaBox().getWidth() - 2 * margin;

                contentStream.beginText();
                contentStream.setFont(titleFont, 18);
                contentStream.newLineAtOffset(margin, y);
                contentStream.showText("ACADEMIC TRANSCRIPT");
                contentStream.endText();
                y -= 30;

                contentStream.beginText();
                contentStream.setFont(bodyFont, 12);
                contentStream.newLineAtOffset(margin, y);
                contentStream.showText("Student: " + user.username());
                contentStream.endText();
                y -= 20;

                contentStream.beginText();
                contentStream.setFont(bodyFont, 12);
                contentStream.newLineAtOffset(margin, y);
                contentStream.showText("Date: " + java.time.LocalDate.now().toString());
                contentStream.endText();
                y -= 40;

                contentStream.beginText();
                contentStream.setFont(headerFont, 10);
                contentStream.newLineAtOffset(margin, y);
                contentStream.showText("Code");
                contentStream.newLineAtOffset(80, 0);
                contentStream.showText("Course Title");
                contentStream.newLineAtOffset(180, 0);
                contentStream.showText("Credits");
                contentStream.newLineAtOffset(60, 0);
                contentStream.showText("Grade %");
                contentStream.newLineAtOffset(60, 0);
                contentStream.showText("Letter");
                contentStream.endText();
                y -= 20;

                contentStream.moveTo(margin, y);
                contentStream.lineTo(margin + tableWidth, y);
                contentStream.stroke();
                y -= 15;

                for (TranscriptRecord record : records) {
                    if (y < 100) {

                        break;
                    }

                    contentStream.beginText();
                    contentStream.setFont(bodyFont, 9);
                    contentStream.newLineAtOffset(margin, y);
                    contentStream.showText(record.courseCode());
                    contentStream.newLineAtOffset(80, 0);
                    String title = record.courseTitle();
                    if (title.length() > 25) title = title.substring(0, 22) + "...";
                    contentStream.showText(title);
                    contentStream.newLineAtOffset(180, 0);
                    contentStream.showText(String.valueOf(record.credits()));
                    contentStream.newLineAtOffset(60, 0);
                    contentStream.showText(String.format("%.1f", record.finalPercentage()));
                    contentStream.newLineAtOffset(60, 0);
                    contentStream.showText(record.letterGrade());
                    contentStream.endText();
                    y -= 15;
                }

                y -= 20;
                double totalCredits = records.stream().mapToInt(TranscriptRecord::credits).sum();
                double avgGrade = records.stream().mapToDouble(TranscriptRecord::finalPercentage).average().orElse(0.0);

                contentStream.beginText();
                contentStream.setFont(headerFont, 11);
                contentStream.newLineAtOffset(margin, y);
                contentStream.showText(String.format("Total Credits: %.0f    Average Grade: %.2f%%", totalCredits, avgGrade));
                contentStream.endText();
            }

            document.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to generate transcript PDF", e);
        }
    }

    @Override
    public List<TranscriptRecord> transcriptRecords(AuthenticatedUser user) {
        accessControlService.requireRole(user, UserRole.STUDENT);

        List<Optional<TranscriptRecord>> records = enrollmentDao.listByStudent(user.userId()).stream()
                .map(this::toTranscriptRecord)
                .filter(Optional::isPresent)
                .collect(Collectors.toList());

        Map<String, TranscriptRecord> latest = new java.util.HashMap<>();
        for (Optional<TranscriptRecord> opt : records) {
            if (opt.isEmpty()) continue;
            TranscriptRecord rec = opt.get();
            String courseCode = rec.courseCode();

            TranscriptRecord existing = latest.get(courseCode);
            if (existing == null) {
                latest.put(courseCode, rec);
            } else {

                if (rec.year() > existing.year()) {
                    latest.put(courseCode, rec);
                } else if (rec.year() == existing.year()) {
                    try {
                        int semRec = Integer.parseInt(rec.semester());
                        int semExist = Integer.parseInt(existing.semester());
                        if (semRec > semExist) {
                            latest.put(courseCode, rec);
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
        return latest.values().stream().collect(Collectors.toList());
    }

    @Override
    public ApiResponse<Void> register(AuthenticatedUser user, long sectionId) {
        accessControlService.requireRole(user, UserRole.STUDENT);
        accessControlService.requireMaintenanceOff(maintenanceGuard.isMaintenanceOn());

        Optional<Student> studentOpt = studentDao.findByUserId(user.userId());
        if (studentOpt.isEmpty()) {
            return ApiResponse.failure("PROFILE_MISSING", "Student profile not found. Contact administration.");
        }

        Optional<Section> sectionOpt = sectionDao.findById(sectionId);
        if (sectionOpt.isEmpty()) {
            return ApiResponse.failure("NOT_FOUND", "Section not found.");
        }
        Section section = sectionOpt.get();

        Optional<Course> courseOpt = courseDao.findById(section.courseId());
        if (courseOpt.isPresent()) {
            Course course = courseOpt.get();
            if (course.prerequisiteCode() != null && !course.prerequisiteCode().isEmpty()) {

                boolean hasPrerequisite = transcriptRecords(user).stream()
                        .anyMatch(rec -> rec.courseCode().equals(course.prerequisiteCode()) 
                                && rec.letterGrade() != null 
                                && !rec.letterGrade().equals("F"));
                if (!hasPrerequisite) {
                    return ApiResponse.failure("PREREQUISITE_NOT_MET", 
                            "Prerequisite not met. You must complete " + course.prerequisiteCode() + " first.");
                }
            }
        }

        Optional<Enrollment> existing = enrollmentDao.findByStudentAndSection(user.userId(), sectionId);
        if (existing.isPresent() && existing.get().status() == EnrollmentStatus.REGISTERED) {
            return ApiResponse.failure("DUPLICATE", "You are already registered for this section.");
        }

        long activeCount = enrollmentDao.countActiveEnrollments(sectionId);
        if (activeCount >= section.capacity()) {
            return ApiResponse.failure("SECTION_FULL", "Section is already full.");
        }

        if (existing.isPresent()) {
            enrollmentDao.updateStatus(existing.get().id(), EnrollmentStatus.REGISTERED);

            try {
                long courseId = section.courseId();
                List<Enrollment> active = enrollmentDao.listByStudent(user.userId());
                for (Enrollment e : active) {
                    if (e.id() != existing.get().id()) {
                        Optional<Section> sOpt = sectionDao.findById(e.sectionId());
                        if (sOpt.isPresent() && sOpt.get().courseId() == courseId) {

                            enrollmentDao.updateStatus(e.id(), EnrollmentStatus.DROPPED);
                            try { gradeDao.deleteByEnrollmentId(e.id()); } catch (Exception ignore) {}
                        }
                    }
                }
            } catch (Exception ignore) {}
        } else {
            enrollmentDao.save(new Enrollment(0, user.userId(), sectionId, EnrollmentStatus.REGISTERED));

            try {
                long courseId = section.courseId();
                List<Enrollment> active = enrollmentDao.listByStudent(user.userId());
                for (Enrollment e : active) {

                    if (e.sectionId() != sectionId) {
                        Optional<Section> sOpt = sectionDao.findById(e.sectionId());
                        if (sOpt.isPresent() && sOpt.get().courseId() == courseId) {
                            enrollmentDao.updateStatus(e.id(), EnrollmentStatus.DROPPED);
                            try { gradeDao.deleteByEnrollmentId(e.id()); } catch (Exception ignore) {}
                        }
                    }
                }
            } catch (Exception ignore) {}
        }

        return ApiResponse.success(null);
    }

    @Override
    public ApiResponse<Void> drop(AuthenticatedUser user, long sectionId) {
        accessControlService.requireRole(user, UserRole.STUDENT);
        accessControlService.requireMaintenanceOff(maintenanceGuard.isMaintenanceOn());

        Optional<Enrollment> enrollmentOpt = enrollmentDao.findByStudentAndSection(user.userId(), sectionId);
        if (enrollmentOpt.isEmpty()) {
            return ApiResponse.failure("NOT_REGISTERED", "You are not registered in this section.");
        }

        Enrollment enrollment = enrollmentOpt.get();
        if (enrollment.status() != EnrollmentStatus.REGISTERED) {
            return ApiResponse.failure("NOT_ACTIVE", "Cannot drop a section that is not active.");
        }

        java.time.LocalDate now = java.time.LocalDate.now();
        Optional<Section> sectionOpt = sectionDao.findById(sectionId);
        if (sectionOpt.isPresent()) {
            Section section = sectionOpt.get();

            try {
                int semesterNum = Integer.parseInt(section.semester());
                if (semesterNum == 1 && now.getMonthValue() >= 3) {
                    return ApiResponse.failure("DEADLINE_PASSED", "Drop deadline has passed for this semester.");
                } else if (semesterNum == 2 && now.getMonthValue() >= 9) {
                    return ApiResponse.failure("DEADLINE_PASSED", "Drop deadline has passed for this semester.");
                }
            } catch (NumberFormatException ignored) {

            }
        }

        enrollmentDao.updateStatus(enrollment.id(), EnrollmentStatus.DROPPED);
        return ApiResponse.success(null);
    }

    private List<GradeView> gradeViewsForEnrollment(Enrollment enrollment) {
        Optional<Section> sectionOpt = sectionDao.findById(enrollment.sectionId());
        if (sectionOpt.isEmpty()) {
            return List.of();
        }
        Section section = sectionOpt.get();
        Course course = courseDao.findById(section.courseId())
                .orElse(new Course(section.courseId(), "UNKNOWN", "Unknown Course", 0, null));

        List<GradeEntry> entries = gradeDao.listByEnrollment(enrollment.id()).stream()
                .sorted(Comparator.comparing(GradeEntry::componentId))
                .collect(Collectors.toList());
        List<GradeView> views = entries.stream()
                .map(entry -> {
                    String componentName = assessmentDao.findById(entry.componentId())
                            .map(AssessmentComponent::name)
                            .orElse("Component " + entry.componentId());
                    return new GradeView(
                            course.code(),
                            course.title(),
                            componentName,
                            entry.score(),
                            null,
                            null
                    );
                })
                .collect(Collectors.toCollection(java.util.ArrayList::new));

        FinalGrade finalGrade = gradeDao.findFinalGrade(enrollment.id());
        if (finalGrade != null) {
            views.add(new GradeView(
                    course.code(),
                    course.title(),
                    "Final Grade",
                    finalGrade.percentage(),
                    finalGrade.percentage(),
                    finalGrade.letterGrade()
            ));
        }
        return views;
    }

    private Optional<TranscriptRecord> toTranscriptRecord(Enrollment enrollment) {
        Optional<Section> sectionOpt = sectionDao.findById(enrollment.sectionId());
        if (sectionOpt.isEmpty()) {
            return Optional.empty();
        }
        Section section = sectionOpt.get();
        Course course = courseDao.findById(section.courseId())
                .orElse(new Course(section.courseId(), "UNKNOWN", "Unknown Course", 0, null));
        FinalGrade finalGrade = gradeDao.findFinalGrade(enrollment.id());
        if (finalGrade == null) {
            return Optional.empty();
        }
        return Optional.of(new TranscriptRecord(
                course.code(),
                course.title(),
                course.credits(),
                finalGrade.percentage(),
                finalGrade.letterGrade(),
                section.semester(),
                section.year()
        ));
    }
}

