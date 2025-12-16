package edu.univ.erp.service.instructor;

import edu.univ.erp.data.erp.InMemoryAssessmentDao;
import edu.univ.erp.data.erp.InMemoryEnrollmentDao;
import edu.univ.erp.data.erp.InMemoryGradeDao;
import edu.univ.erp.data.erp.InMemorySectionDao;
import edu.univ.erp.data.erp.InMemoryStudentDao;
import edu.univ.erp.service.AccessControlService;
import edu.univ.erp.service.impl.DefaultAccessControlService;
import edu.univ.erp.access.MaintenanceModeGuard;
import edu.univ.erp.auth.AuthenticatedUser;
import edu.univ.erp.domain.UserRole;
import edu.univ.erp.service.instructor.impl.InstructorServiceImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InstructorServiceImplTest {

    @Test
    void computeFinalGrades_calculatesExpectedFinalPercentages() {
        var sectionDao = new InMemorySectionDao();
        var enrollmentDao = new InMemoryEnrollmentDao();
        var assessmentDao = new InMemoryAssessmentDao();
        var gradeDao = new InMemoryGradeDao();
        var studentDao = new InMemoryStudentDao();
        AccessControlService access = new DefaultAccessControlService();
        MaintenanceModeGuard guard = new MaintenanceModeGuard();

        var svc = new InstructorServiceImpl(sectionDao, enrollmentDao, assessmentDao, gradeDao, studentDao, access, guard);
        AuthenticatedUser instructor = new AuthenticatedUser(6L, "instructor6", UserRole.INSTRUCTOR, false);

        var resp = svc.computeFinalGrades(instructor, 1L);
        assertTrue(resp.success(), "computeFinalGrades should succeed");

        var fg = gradeDao.findFinalGrade(1L);
        assertNotNull(fg, "Final grade should be present for enrollment 1");
        assertEquals(90.0, fg.percentage(), 0.0001);
        assertEquals("A", fg.letterGrade());
    }
}
