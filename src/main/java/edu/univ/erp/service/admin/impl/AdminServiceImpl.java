package edu.univ.erp.service.admin.impl;

import edu.univ.erp.api.common.ApiResponse;
import edu.univ.erp.auth.AuthenticatedUser;
import edu.univ.erp.auth.hash.PasswordHasher;
import edu.univ.erp.data.auth.AuthUserDao;
import edu.univ.erp.data.auth.AuthUserRecord;
import edu.univ.erp.data.erp.CourseDao;
import edu.univ.erp.data.erp.InstructorDao;
import edu.univ.erp.data.erp.SectionDao;
import edu.univ.erp.data.erp.EnrollmentDao;
import edu.univ.erp.data.erp.GradeDao;
import edu.univ.erp.data.erp.AssessmentDao;
import edu.univ.erp.data.erp.StudentDao;
import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.Instructor;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.UserRole;
import edu.univ.erp.service.AccessControlService;
import edu.univ.erp.service.BackupService;
import edu.univ.erp.service.admin.AdminService;
import edu.univ.erp.service.exception.AccessDeniedException;

import java.nio.file.Path;
import java.util.List;

public class AdminServiceImpl implements AdminService {

    private final AuthUserDao authUserDao;
    private final StudentDao studentDao;
    private final InstructorDao instructorDao;
    private final CourseDao courseDao;
    private final SectionDao sectionDao;
    private final AssessmentDao assessmentDao;
    private final EnrollmentDao enrollmentDao;
    private final GradeDao gradeDao;
    private final PasswordHasher passwordHasher;
    private final AccessControlService accessControlService;
    private final BackupService backupService;

    public AdminServiceImpl(AuthUserDao authUserDao,
                           StudentDao studentDao,
                           InstructorDao instructorDao,
                           CourseDao courseDao,
                           SectionDao sectionDao,
                           AssessmentDao assessmentDao,
                           EnrollmentDao enrollmentDao,
                           GradeDao gradeDao,
                           PasswordHasher passwordHasher,
                           AccessControlService accessControlService,
                           BackupService backupService) {
        this.authUserDao = authUserDao;
        this.studentDao = studentDao;
        this.instructorDao = instructorDao;
        this.courseDao = courseDao;
        this.sectionDao = sectionDao;
        this.assessmentDao = assessmentDao;
        this.enrollmentDao = enrollmentDao;
        this.gradeDao = gradeDao;
        this.passwordHasher = passwordHasher;
        this.accessControlService = accessControlService;
        this.backupService = backupService;
    }

    @Override
    public ApiResponse<Void> createUser(AuthenticatedUser admin, String username, String role, String tempPassword) {
        try {
            accessControlService.requireRole(admin, UserRole.ADMIN);

            if (username == null || username.trim().isEmpty()) {
                return ApiResponse.failure("VALIDATION_ERROR", "Username cannot be empty");
            }

            if (tempPassword == null || tempPassword.length() < 6) {
                return ApiResponse.failure("VALIDATION_ERROR", "Password must be at least 6 characters");
            }

            UserRole userRole;
            try {
                userRole = UserRole.valueOf(role.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ApiResponse.failure("VALIDATION_ERROR", "Invalid role: " + role);
            }

            if (authUserDao.findByUsername(username).isPresent()) {
                return ApiResponse.failure("DUPLICATE_USERNAME", "Username already exists: " + username);
            }

            String passwordHash = passwordHasher.hash(tempPassword.toCharArray());
            long userId = authUserDao.createUser(username, userRole, passwordHash, true);

            if (userRole == UserRole.STUDENT) {
                String rollNo = "STU" + String.format("%03d", userId);
                studentDao.create(userId, rollNo, "B.Tech CSE", 1);
            }

            return ApiResponse.success(null);
        } catch (AccessDeniedException e) {
            return ApiResponse.failure("ACCESS_DENIED", e.getMessage());
        } catch (Exception e) {
            return ApiResponse.failure("ERROR", "Failed to create user: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<Course> createCourse(AuthenticatedUser admin, Course course) {
        try {
            accessControlService.requireRole(admin, UserRole.ADMIN);

            if (course.code() == null || course.code().trim().isEmpty()) {
                return ApiResponse.failure("VALIDATION_ERROR", "Course code cannot be empty");
            }

            if (course.title() == null || course.title().trim().isEmpty()) {
                return ApiResponse.failure("VALIDATION_ERROR", "Course title cannot be empty");
            }

            if (course.credits() <= 0) {
                return ApiResponse.failure("VALIDATION_ERROR", "Credits must be positive");
            }

            Course created = courseDao.create(course.code(), course.title(), course.credits());
            return ApiResponse.success(created);
        } catch (AccessDeniedException e) {
            return ApiResponse.failure("ACCESS_DENIED", e.getMessage());
        } catch (Exception e) {
            return ApiResponse.failure("ERROR", "Failed to create course: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<Section> createSection(AuthenticatedUser admin, Section section) {
        try {
            accessControlService.requireRole(admin, UserRole.ADMIN);

            if (!courseDao.findById(section.courseId()).isPresent()) {
                return ApiResponse.failure("VALIDATION_ERROR", "Course not found: " + section.courseId());
            }

            if (!instructorDao.findByUserId(section.instructorId()).isPresent()) {
                return ApiResponse.failure("VALIDATION_ERROR", "Instructor not found: " + section.instructorId());
            }

            if (section.capacity() <= 0) {
                return ApiResponse.failure("VALIDATION_ERROR", "Capacity must be positive");
            }

            if (section.room() == null || section.room().trim().isEmpty()) {
                return ApiResponse.failure("VALIDATION_ERROR", "Room cannot be empty");
            }

                String semester = section.semester();
                if (semester != null) {
                if (semester.equalsIgnoreCase("odd")) semester = "1";
                else if (semester.equalsIgnoreCase("even")) semester = "2";
                }

                Section created = sectionDao.create(
                    section.courseId(),
                    section.instructorId(),
                    section.dayOfWeek(),
                    section.startTime(),
                    section.endTime(),
                    section.room(),
                    section.capacity(),
                    semester,
                    section.year()
            );

                try {
                    assessmentDao.create(created.id(), "Quiz 1", 20.0);
                    assessmentDao.create(created.id(), "Quiz 2", 15.0);
                    assessmentDao.create(created.id(), "Midsem", 30.0);
                    assessmentDao.create(created.id(), "Endsem", 35.0);
                } catch (Exception ignored) {
                }

            return ApiResponse.success(created);
        } catch (AccessDeniedException e) {
            return ApiResponse.failure("ACCESS_DENIED", e.getMessage());
        } catch (Exception e) {
            return ApiResponse.failure("ERROR", "Failed to create section: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<Void> assignInstructor(AuthenticatedUser admin, long sectionId, long instructorUserId) {
        try {
            accessControlService.requireRole(admin, UserRole.ADMIN);

            if (!sectionDao.findById(sectionId).isPresent()) {
                return ApiResponse.failure("VALIDATION_ERROR", "Section not found: " + sectionId);
            }

            if (!instructorDao.findByUserId(instructorUserId).isPresent()) {
                return ApiResponse.failure("VALIDATION_ERROR", "Instructor not found: " + instructorUserId);
            }

            sectionDao.updateInstructor(sectionId, instructorUserId);
            return ApiResponse.success(null);
        } catch (AccessDeniedException e) {
            return ApiResponse.failure("ACCESS_DENIED", e.getMessage());
        } catch (Exception e) {
            return ApiResponse.failure("ERROR", "Failed to assign instructor: " + e.getMessage());
        }
    }

    @Override
    public List<Course> listCourses(AuthenticatedUser admin) {
        accessControlService.requireRole(admin, UserRole.ADMIN);
        return courseDao.listAll();
    }

    @Override
    public List<Section> listSections(AuthenticatedUser admin) {
        accessControlService.requireRole(admin, UserRole.ADMIN);
        return sectionDao.listAll();
    }

    @Override
    public List<Instructor> listInstructors(AuthenticatedUser admin) {
        accessControlService.requireRole(admin, UserRole.ADMIN);

        var authUsers = authUserDao.listAllUsers();
        return instructorDao.listAll().stream()
                .filter(inst -> authUsers.stream().anyMatch(u -> u.userId() == inst.userId() && u.role() == UserRole.INSTRUCTOR))
                .toList();
    }

    @Override
    public List<AuthUserRecord> listUsers(AuthenticatedUser admin) {
        accessControlService.requireRole(admin, UserRole.ADMIN);
        return authUserDao.listAllUsers();
    }

    @Override
    public ApiResponse<Void> deleteUser(AuthenticatedUser admin, long userId) {
        try {
            accessControlService.requireRole(admin, UserRole.ADMIN);

            edu.univ.erp.data.auth.AuthUserRecord record = authUserDao.listAllUsers().stream()
                    .filter(r -> r.userId() == userId)
                    .findFirst().orElse(null);

            if (record != null && record.role() == UserRole.INSTRUCTOR) {

                try {
                    boolean removed = instructorDao.deleteByUserId(userId);
                    if (removed) {

                        for (Section s : sectionDao.listAll()) {
                            if (s.instructorId() == userId) {
                                sectionDao.updateInstructor(s.id(), 0L);
                            }
                        }
                    }
                } catch (Exception ignore) {

                }
            }

            if (record != null && record.role() == UserRole.STUDENT) {

                try {

                    var enrollments = enrollmentDao.listByStudent(userId);
                    for (var e : enrollments) {
                        try { gradeDao.deleteByEnrollmentId(e.id()); } catch (Exception ignore) {}
                    }

                    try { enrollmentDao.deleteByStudentId(userId); } catch (Exception ignore) {}

                    try { studentDao.deleteByUserId(userId); } catch (Exception ignore) {}
                } catch (Exception ignore) {}
            }

            authUserDao.deleteUser(userId);
            return ApiResponse.success(null);
        } catch (AccessDeniedException e) {
            return ApiResponse.failure("ACCESS_DENIED", e.getMessage());
        } catch (Exception e) {
            return ApiResponse.failure("ERROR", "Failed to delete user: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<Void> deleteCourse(AuthenticatedUser admin, long courseId) {
        try {
            accessControlService.requireRole(admin, UserRole.ADMIN);

            if (!courseDao.findById(courseId).isPresent()) {
                return ApiResponse.failure("NOT_FOUND", "Course not found: " + courseId);
            }

            List<Section> sections = sectionDao.listAll().stream().filter(s -> s.courseId() == courseId).toList();
            for (Section s : sections) {
                long sectionId = s.id();

                var enrollments = enrollmentDao.listBySection(sectionId);
                for (var e : enrollments) {
                    try { gradeDao.deleteByEnrollmentId(e.id()); } catch (Exception ignore) {}
                    try { enrollmentDao.deleteById(e.id()); } catch (Exception ignore) {}
                }

                var comps = assessmentDao.listBySection(sectionId);
                for (var c : comps) {
                    try { assessmentDao.delete(c.id()); } catch (Exception ignore) {}
                }

                try { sectionDao.deleteById(sectionId); } catch (Exception ignore) {}
            }

            try { courseDao.deleteById(courseId); } catch (Exception e) {
                return ApiResponse.failure("ERROR", "Failed to delete course: " + e.getMessage());
            }

            return ApiResponse.success(null);
        } catch (AccessDeniedException e) {
            return ApiResponse.failure("ACCESS_DENIED", e.getMessage());
        } catch (Exception e) {
            return ApiResponse.failure("ERROR", "Failed to delete course: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<Void> deleteSection(AuthenticatedUser admin, long sectionId) {
        try {
            accessControlService.requireRole(admin, UserRole.ADMIN);

            var sectionOpt = sectionDao.findById(sectionId);
            if (sectionOpt.isEmpty()) {
                return ApiResponse.failure("NOT_FOUND", "Section not found: " + sectionId);
            }

            var enrollments = enrollmentDao.listBySection(sectionId);
            for (var e : enrollments) {
                try { gradeDao.deleteByEnrollmentId(e.id()); } catch (Exception ignore) {}
                try { enrollmentDao.deleteById(e.id()); } catch (Exception ignore) {}
            }

            var comps = assessmentDao.listBySection(sectionId);
            for (var c : comps) {
                try { assessmentDao.delete(c.id()); } catch (Exception ignore) {}
            }

            try { sectionDao.deleteById(sectionId); } catch (Exception e) { return ApiResponse.failure("ERROR", "Failed to delete section: " + e.getMessage()); }

            return ApiResponse.success(null);
        } catch (AccessDeniedException e) {
            return ApiResponse.failure("ACCESS_DENIED", e.getMessage());
        } catch (Exception e) {
            return ApiResponse.failure("ERROR", "Failed to delete section: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<edu.univ.erp.domain.Instructor> createInstructorProfile(AuthenticatedUser admin, long userId, String firstName, String lastName, String department) {
        try {
            accessControlService.requireRole(admin, UserRole.ADMIN);

            if (firstName == null || firstName.isBlank() || lastName == null || lastName.isBlank()) {
                return ApiResponse.failure("VALIDATION_ERROR", "First and last name are required for instructor profile");
            }

            if (department == null || department.isBlank()) {
                department = "General";
            }

            boolean found = authUserDao.listAllUsers().stream().anyMatch(r -> r.userId() == userId);
            if (!found) {
                return ApiResponse.failure("NOT_FOUND", "User not found: " + userId);
            }

            Instructor created = instructorDao.create(userId, firstName, lastName, department);
            return ApiResponse.success(created);
        } catch (AccessDeniedException e) {
            return ApiResponse.failure("ACCESS_DENIED", e.getMessage());
        } catch (Exception e) {
            return ApiResponse.failure("ERROR", "Failed to create instructor profile: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<String> reconcileInstructorProfiles(AuthenticatedUser admin) {
        try {
            accessControlService.requireRole(admin, UserRole.ADMIN);
            var authUsers = authUserDao.listAllUsers().stream()
                    .filter(u -> u.role() == UserRole.INSTRUCTOR)
                    .toList();
            int created = 0;
            for (var u : authUsers) {
                long userId = u.userId();
                if (instructorDao.findByUserId(userId).isEmpty()) {

                    String username = u.username();
                    String first = username;
                    String last = "";
                    if (username.contains(".")) {
                        String[] p = username.split("\\.");
                        first = p[0];
                        last = p.length > 1 ? p[1] : "";
                    }
                    instructorDao.create(userId, first, last, "General");
                    created++;
                }
            }
            String msg = "Reconcile completed. Created profiles: " + created;
            return ApiResponse.success(msg);
        } catch (AccessDeniedException e) {
            return ApiResponse.failure("ACCESS_DENIED", e.getMessage());
        } catch (Exception e) {
            return ApiResponse.failure("ERROR", "Reconcile failed: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<String> createBackup(AuthenticatedUser admin) {
        try {
            accessControlService.requireRole(admin, UserRole.ADMIN);
            Path backupPath = backupService.createBackup();
            String backupName = backupPath.getFileName().toString();
            return ApiResponse.success(backupName);
        } catch (AccessDeniedException e) {
            return ApiResponse.failure("ACCESS_DENIED", e.getMessage());
        } catch (Exception e) {
            return ApiResponse.failure("ERROR", "Backup failed: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<Void> restoreBackup(AuthenticatedUser admin, String backupName) {
        try {
            accessControlService.requireRole(admin, UserRole.ADMIN);
            backupService.restoreBackup(backupName);
            return ApiResponse.success(null);
        } catch (AccessDeniedException e) {
            return ApiResponse.failure("ACCESS_DENIED", e.getMessage());
        } catch (Exception e) {
            return ApiResponse.failure("ERROR", "Restore failed: " + e.getMessage());
        }
    }

    @Override
    public List<String> listBackups(AuthenticatedUser admin) {
        accessControlService.requireRole(admin, UserRole.ADMIN);
        return backupService.listBackups();
    }

    @Override
    public ApiResponse<Void> deleteBackup(AuthenticatedUser admin, String backupName) {
        try {
            accessControlService.requireRole(admin, UserRole.ADMIN);
            boolean success = backupService.deleteBackup(backupName);
            if (success) {
                return ApiResponse.success(null);
            } else {
                return ApiResponse.failure("NOT_FOUND", "Backup not found or could not be deleted");
            }
        } catch (AccessDeniedException e) {
            return ApiResponse.failure("ACCESS_DENIED", e.getMessage());
        } catch (Exception e) {
            return ApiResponse.failure("ERROR", "Delete failed: " + e.getMessage());
        }
    }
}

