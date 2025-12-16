-- ERP Database Seed Data
-- Sample academic data

USE univ_erp_db;

-- Clear existing data (optional, for clean seed)
-- DELETE FROM final_grades;
-- DELETE FROM grade_entries;
-- DELETE FROM assessments;
-- DELETE FROM enrollments;
-- DELETE FROM sections;
-- DELETE FROM courses;
-- DELETE FROM instructors;
-- DELETE FROM students;

-- Students (user_id matches auth_users)
INSERT INTO students (user_id, roll_no, program, year) VALUES
(3, 'STU101', 'B.Tech CSE', 2),
(4, 'STU102', 'B.Tech CSE', 2);

-- Instructors (user_id matches auth_users)
INSERT INTO instructors (user_id, first_name, last_name, department, title) VALUES
(2, 'Dr. Alice', 'Kapoor', 'Computer Science', 'Associate Professor');

-- Courses
INSERT INTO courses (code, title, credits) VALUES
('CS101', 'Introduction to Programming', 4),
('MA102', 'Linear Algebra', 3),
('HS201', 'Modern History', 2);

-- Sections (course_id references courses, instructor_id references instructors.user_id)
INSERT INTO sections (course_id, instructor_id, day_of_week, start_time, end_time, room, capacity, semester, year) VALUES
(1, 2, 'MONDAY', '09:00:00', '10:30:00', 'Room A101', 40, 'Odd', 2025),
(1, 2, 'WEDNESDAY', '09:00:00', '10:30:00', 'Room A102', 40, 'Odd', 2025),
(2, 2, 'TUESDAY', '11:00:00', '12:30:00', 'Room B201', 35, 'Odd', 2025),
(3, 2, 'THURSDAY', '14:00:00', '15:30:00', 'Room C301', 50, 'Odd', 2025);

-- Enrollments (student_id references students.user_id, section_id references sections)
INSERT INTO enrollments (student_id, section_id, status) VALUES
(3, 1, 'ACTIVE'),
(3, 2, 'ACTIVE'),
(4, 3, 'ACTIVE');

-- Assessment components for section 1
INSERT INTO assessments (section_id, name, weight) VALUES
(1, 'Quiz', 20.00),
(1, 'Midterm', 30.00),
(1, 'End-Sem', 50.00);

-- Assessment components for section 2
INSERT INTO assessments (section_id, name, weight) VALUES
(2, 'Quiz', 20.00),
(2, 'Midterm', 30.00),
(2, 'End-Sem', 50.00);

-- Assessment components for section 3
INSERT INTO assessments (section_id, name, weight) VALUES
(3, 'Quiz', 20.00),
(3, 'Midterm', 30.00),
(3, 'End-Sem', 50.00);

-- Grade entries for enrollment 1 (student 3 in section 1)
INSERT INTO grade_entries (enrollment_id, assessment_id, score) VALUES
(1, 1, 18.5),
(1, 2, 25.0),
(1, 3, 45.0);

-- Grade entries for enrollment 2 (student 3 in section 2)
INSERT INTO grade_entries (enrollment_id, assessment_id, score) VALUES
(2, 4, 16.0),
(2, 5, 20.0),
(2, 6, 40.0);

-- Grade entries for enrollment 3 (student 4 in section 3)
INSERT INTO grade_entries (enrollment_id, assessment_id, score) VALUES
(3, 7, 17.0),
(3, 8, 22.0),
(3, 9, 41.0);

-- Final grades
INSERT INTO final_grades (enrollment_id, final_percentage, letter_grade) VALUES
(1, 88.5, 'A'),
(2, 76.0, 'B'),
(3, 80.0, 'B+');

-- Maintenance setting (default: OFF)
INSERT INTO maintenance_settings (setting_key, setting_value) 
VALUES ('maintenance_on', 'false')
ON DUPLICATE KEY UPDATE setting_value = 'false';

