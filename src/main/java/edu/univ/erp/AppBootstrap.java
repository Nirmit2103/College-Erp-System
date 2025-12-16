package edu.univ.erp;

import edu.univ.erp.access.MaintenanceModeGuard;
import edu.univ.erp.auth.AuthService;
import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.auth.hash.BCryptPasswordHasher;
import edu.univ.erp.auth.hash.PasswordHasher;
import edu.univ.erp.auth.impl.DatabaseAuthService;
import edu.univ.erp.data.DataSourceFactory;
import edu.univ.erp.data.auth.AuthUserDao;
import edu.univ.erp.data.auth.InMemoryAuthUserDao;
import edu.univ.erp.data.auth.jdbc.JdbcAuthUserDao;
import edu.univ.erp.data.erp.*;
import edu.univ.erp.data.erp.jdbc.*;
import edu.univ.erp.domain.UserRole;
import edu.univ.erp.domain.Section;
import edu.univ.erp.service.AccessControlService;
import edu.univ.erp.service.impl.DefaultAccessControlService;
import edu.univ.erp.service.maintenance.MaintenanceService;
import edu.univ.erp.service.maintenance.impl.MaintenanceServiceImpl;
import edu.univ.erp.util.ConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.Properties;

public class AppBootstrap {

    private static final Logger log = LoggerFactory.getLogger(AppBootstrap.class);

    private final AuthService authService;
    private final AuthUserDao authUserDao;
    private final MaintenanceService maintenanceService;
    private final MaintenanceModeGuard maintenanceGuard;
    private final AccessControlService accessControlService;
    private final edu.univ.erp.service.student.StudentService studentService;
    private final edu.univ.erp.service.instructor.InstructorService instructorService;
    private final edu.univ.erp.service.admin.AdminService adminService;
    private final AssessmentDao assessmentDao;
    private final Properties config;

    public AppBootstrap() {
        this.config = ConfigLoader.load();
        String storageMode = config.getProperty("storage.mode", "memory").toLowerCase();

        PasswordHasher hasher = new BCryptPasswordHasher();
        this.authUserDao = createAuthUserDao(storageMode);
        if ("memory".equals(storageMode)) {
            seedUsers((InMemoryAuthUserDao) authUserDao, hasher);
        }

        this.maintenanceGuard = new MaintenanceModeGuard();
        MaintenanceDao maintenanceDao = createMaintenanceDao(storageMode);
        this.maintenanceService = new MaintenanceServiceImpl(maintenanceDao, maintenanceGuard);

        SessionManager sessionManager = new SessionManager();
        this.authService = new DatabaseAuthService(
                authUserDao,
                hasher,
                sessionManager,
                maintenanceGuard::isMaintenanceOn
        );

        this.accessControlService = new DefaultAccessControlService();

        SectionDao sectionDao = createSectionDao(storageMode);
        CourseDao courseDao = createCourseDao(storageMode);
        InstructorDao instructorDao = createInstructorDao(storageMode);
        StudentDao studentDao = createStudentDao(storageMode);
        EnrollmentDao enrollmentDao = createEnrollmentDao(storageMode);
        GradeDao gradeDao = createGradeDao(storageMode);
        this.assessmentDao = createAssessmentDao(storageMode);

        this.studentService = new edu.univ.erp.service.student.impl.StudentServiceImpl(
                sectionDao,
                courseDao,
                instructorDao,
                enrollmentDao,
                studentDao,
                gradeDao,
                accessControlService,
                maintenanceGuard,
                this.assessmentDao
        );
        this.instructorService = new edu.univ.erp.service.instructor.impl.InstructorServiceImpl(
                sectionDao,
                enrollmentDao,
                this.assessmentDao,
                gradeDao,
                studentDao,
            instructorDao,
            authUserDao,
            accessControlService,
            maintenanceGuard
        );

        edu.univ.erp.service.BackupService backupService = new edu.univ.erp.service.impl.BackupServiceImpl();

        this.adminService = new edu.univ.erp.service.admin.impl.AdminServiceImpl(
                authUserDao,
                studentDao,
                instructorDao,
                courseDao,
            sectionDao,
            this.assessmentDao,
            enrollmentDao,
            gradeDao,
            hasher,
            accessControlService,
            backupService
        );

        maintenanceService.currentSetting();

        try {
            var users = authUserDao.listAllUsers().stream().map(u -> u.userId()).toList();
            for (Section s : sectionDao.listAll()) {
                for (var enrollment : enrollmentDao.listBySection(s.id())) {
                    long studentId = enrollment.studentId();
                    boolean authExists = users.contains(studentId);
                    boolean profileExists = studentDao.findByUserId(studentId).isPresent();
                    if (!authExists || !profileExists) {

                        var stuEnrollments = enrollmentDao.listByStudent(studentId);
                        for (var e : stuEnrollments) {
                            try { gradeDao.deleteByEnrollmentId(e.id()); } catch (Exception ignore) {}
                        }
                        try { enrollmentDao.deleteByStudentId(studentId); } catch (Exception ignore) {}
                        try { studentDao.deleteByUserId(studentId); } catch (Exception ignore) {}
                    }
                }
            }
        } catch (Exception ignored) {
        }

        try {
            for (Section s : sectionDao.listAll()) {
                var comps = this.assessmentDao.listBySection(s.id());
                boolean hasQuiz1 = comps.stream().anyMatch(c -> c.name().equalsIgnoreCase("Quiz 1") || c.name().equalsIgnoreCase("Quiz1"));
                boolean hasQuiz2 = comps.stream().anyMatch(c -> c.name().equalsIgnoreCase("Quiz 2") || c.name().equalsIgnoreCase("Quiz2"));
                boolean hasMid = comps.stream().anyMatch(c -> c.name().toLowerCase().contains("mid"));
                boolean hasEnd = comps.stream().anyMatch(c -> c.name().toLowerCase().contains("end"));
                if (!hasQuiz1) assessmentDao.create(s.id(), "Quiz 1", 20.0);
                if (!hasQuiz2) assessmentDao.create(s.id(), "Quiz 2", 15.0);
                if (!hasMid) assessmentDao.create(s.id(), "Midsem", 30.0);
                if (!hasEnd) assessmentDao.create(s.id(), "Endsem", 35.0);
            }
        } catch (Exception ignored) {
        }
        log.info("Application initialized with storage mode: {}", storageMode);
    }

    private AuthUserDao createAuthUserDao(String mode) {
        if ("jdbc".equals(mode)) {
            DataSource authDs = DataSourceFactory.fromProperties(config, "auth");
            return new JdbcAuthUserDao(authDs);
        }
        return new InMemoryAuthUserDao();
    }

    private MaintenanceDao createMaintenanceDao(String mode) {
        if ("jdbc".equals(mode)) {
            DataSource erpDs = DataSourceFactory.fromProperties(config, "erp");
            return new JdbcMaintenanceDao(erpDs);
        }
        return new InMemoryMaintenanceDao();
    }

    private SectionDao createSectionDao(String mode) {
        if ("jdbc".equals(mode)) {
            DataSource erpDs = DataSourceFactory.fromProperties(config, "erp");
            return new JdbcSectionDao(erpDs);
        }
        return new InMemorySectionDao();
    }

    private CourseDao createCourseDao(String mode) {
        if ("jdbc".equals(mode)) {
            DataSource erpDs = DataSourceFactory.fromProperties(config, "erp");
            return new JdbcCourseDao(erpDs);
        }
        return new InMemoryCourseDao();
    }

    private InstructorDao createInstructorDao(String mode) {
        if ("jdbc".equals(mode)) {
            DataSource erpDs = DataSourceFactory.fromProperties(config, "erp");
            return new JdbcInstructorDao(erpDs);
        }
        return new InMemoryInstructorDao();
    }

    private StudentDao createStudentDao(String mode) {
        if ("jdbc".equals(mode)) {
            DataSource erpDs = DataSourceFactory.fromProperties(config, "erp");
            return new JdbcStudentDao(erpDs);
        }
        return new InMemoryStudentDao();
    }

    private EnrollmentDao createEnrollmentDao(String mode) {
        if ("jdbc".equals(mode)) {
            DataSource erpDs = DataSourceFactory.fromProperties(config, "erp");
            return new JdbcEnrollmentDao(erpDs);
        }
        return new InMemoryEnrollmentDao();
    }

    private GradeDao createGradeDao(String mode) {
        if ("jdbc".equals(mode)) {
            DataSource erpDs = DataSourceFactory.fromProperties(config, "erp");
            return new JdbcGradeDao(erpDs);
        }
        return new InMemoryGradeDao();
    }

    private AssessmentDao createAssessmentDao(String mode) {
        if ("jdbc".equals(mode)) {
            DataSource erpDs = DataSourceFactory.fromProperties(config, "erp");
            return new JdbcAssessmentDao(erpDs);
        }
        return new InMemoryAssessmentDao();
    }

    private void seedUsers(InMemoryAuthUserDao dao, PasswordHasher hasher) {

        if (dao.findByUsername("admin1").isEmpty()) {
            dao.addUser("admin1", UserRole.ADMIN, hasher.hash("admin123".toCharArray()), true);
        }
        if (dao.findByUsername("admin2").isEmpty()) {
            dao.addUser("admin2", UserRole.ADMIN, hasher.hash("admin123".toCharArray()), true);
        }

        if (dao.findByUsername("inst1").isEmpty()) {
            dao.addUser("inst1", UserRole.INSTRUCTOR, hasher.hash("inst123".toCharArray()), true);
        }
        if (dao.findByUsername("inst2").isEmpty()) {
            dao.addUser("inst2", UserRole.INSTRUCTOR, hasher.hash("inst123".toCharArray()), true);
        }
        if (dao.findByUsername("inst3").isEmpty()) {
            dao.addUser("inst3", UserRole.INSTRUCTOR, hasher.hash("inst123".toCharArray()), true);
        }

        if (dao.findByUsername("stu1").isEmpty()) {
            dao.addUser("stu1", UserRole.STUDENT, hasher.hash("stu123".toCharArray()), true);
        }
        if (dao.findByUsername("stu2").isEmpty()) {
            dao.addUser("stu2", UserRole.STUDENT, hasher.hash("stu123".toCharArray()), true);
        }
        if (dao.findByUsername("stu3").isEmpty()) {
            dao.addUser("stu3", UserRole.STUDENT, hasher.hash("stu123".toCharArray()), true);
        }
        if (dao.findByUsername("stu4").isEmpty()) {
            dao.addUser("stu4", UserRole.STUDENT, hasher.hash("stu123".toCharArray()), true);
        }
        if (dao.findByUsername("stu5").isEmpty()) {
            dao.addUser("stu5", UserRole.STUDENT, hasher.hash("stu123".toCharArray()), true);
        }
    }

    public AuthService authService() {
        return authService;
    }

    public edu.univ.erp.data.auth.AuthUserDao authUserDao() {
        return authUserDao;
    }

    public MaintenanceService maintenanceService() {
        return maintenanceService;
    }

    public MaintenanceModeGuard maintenanceGuard() {
        return maintenanceGuard;
    }

    public AccessControlService accessControlService() {
        return accessControlService;
    }

    public edu.univ.erp.service.student.StudentService studentService() {
        return studentService;
    }

    public edu.univ.erp.service.instructor.InstructorService instructorService() {
        return instructorService;
    }

    public edu.univ.erp.service.admin.AdminService adminService() {
        return adminService;
    }
}

