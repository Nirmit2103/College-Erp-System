package edu.univ.erp.service.admin;

import java.util.List;

import edu.univ.erp.api.common.ApiResponse;
import edu.univ.erp.auth.AuthenticatedUser;
import edu.univ.erp.data.auth.AuthUserRecord;
import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.Instructor;
import edu.univ.erp.domain.Section;

public interface AdminService {

    ApiResponse<Void> createUser(AuthenticatedUser admin, String username, String role, String tempPassword);

    ApiResponse<edu.univ.erp.domain.Instructor> createInstructorProfile(AuthenticatedUser admin, long userId, String firstName, String lastName, String department);

    ApiResponse<Course> createCourse(AuthenticatedUser admin, Course course);

    ApiResponse<Section> createSection(AuthenticatedUser admin, Section section);

    ApiResponse<Void> assignInstructor(AuthenticatedUser admin, long sectionId, long instructorUserId);

    List<Course> listCourses(AuthenticatedUser admin);

    List<Section> listSections(AuthenticatedUser admin);

    List<Instructor> listInstructors(AuthenticatedUser admin);

    List<AuthUserRecord> listUsers(AuthenticatedUser admin);

    ApiResponse<Void> deleteUser(AuthenticatedUser admin, long userId);

    ApiResponse<Void> deleteCourse(AuthenticatedUser admin, long courseId);

    ApiResponse<Void> deleteSection(AuthenticatedUser admin, long sectionId);

    ApiResponse<String> reconcileInstructorProfiles(AuthenticatedUser admin);

    ApiResponse<String> createBackup(AuthenticatedUser admin);

    ApiResponse<Void> restoreBackup(AuthenticatedUser admin, String backupName);

    List<String> listBackups(AuthenticatedUser admin);

    ApiResponse<Void> deleteBackup(AuthenticatedUser admin, String backupName);
}

