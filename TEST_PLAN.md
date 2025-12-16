# Test Plan - University ERP Desktop Application

## Document Overview

**Project**: University ERP Desktop Application  
**Version**: 0.1.0-SNAPSHOT  
**Date**: November 2025  
**Test Environment**: Java 17, Windows/Mac/Linux

---

## 1. Acceptance Test Cases

### Test Category A: Authentication & Security

#### A1: Login with Valid Credentials
**Purpose**: Verify users can log in with correct credentials  
**Test Account**: `admin1` / `admin123`  
**Steps**:
1. Launch application
2. Enter username: `admin1`
3. Enter password: `admin123`
4. Click Login
**Expected**: Admin dashboard opens, shows "Administrator Dashboard - admin1"  
**Status**: ✅ Pass

#### A2: Login with Invalid Password
**Purpose**: Verify system rejects incorrect passwords  
**Test Account**: `stu1` / `wrongpass`  
**Steps**:
1. Enter username: `stu1`
2. Enter password: `wrongpass`
3. Click Login
**Expected**: Error message "Incorrect username or password", failed attempt recorded  
**Status**: ✅ Pass

#### A3: Failed Login Attempt Tracking
**Purpose**: Verify system tracks and displays failed login attempts  
**Test Account**: `stu1` / wrong passwords  
**Steps**:
1. Attempt login with wrong password 3 times
2. Type username `stu1` in login screen
**Expected**: Warning displays "⚠ 3 failed login attempts for this account - Account may be locked soon!"  
**Status**: ✅ Pass

#### A4: Password Change
**Purpose**: Verify users can change their password  
**Test Account**: Any logged-in user  
**Steps**:
1. Log in as `stu1` / `stu123`
2. Click "Change Password" button
3. Enter current password: `stu123`
4. Enter new password: `newpass123`
5. Confirm new password: `newpass123`
6. Submit
**Expected**: Success message, can log in with new password  
**Status**: ✅ Pass

---

### Test Category B: Student Features

#### B1: Browse Course Catalog
**Purpose**: Verify students can view available sections  
**Test Account**: `stu1` / `stu123`  
**Steps**:
1. Log in as student
2. Navigate to "Catalog" tab
**Expected**: Table displays all sections with course code, title, credits, instructor, schedule, capacity  
**Status**: ✅ Pass

#### B2: Register for Section
**Purpose**: Verify student can enroll in a section  
**Test Account**: `stu1` / `stu123`  
**Steps**:
1. Go to Catalog tab
2. Select a section (CS101-MONDAY)
3. Click "Register Selected Section"
**Expected**: Success message, section appears in "Registrations" tab  
**Status**: ✅ Pass

#### B3: Prevent Duplicate Registration
**Purpose**: Verify system prevents enrolling in same section twice  
**Test Account**: `stu1` (already enrolled in CS101)  
**Steps**:
1. Go to Catalog tab
2. Select CS101 section (already enrolled)
3. Click "Register Selected Section"
**Expected**: Error message "You are already enrolled in this section"  
**Status**: ✅ Pass

#### B4: Check Capacity Limits
**Purpose**: Verify enrollment stops when section is full  
**Test Data**: Section with capacity=2, 2 students already enrolled  
**Steps**:
1. Log in as `stu2`
2. Try to register for full section
**Expected**: Error message about section being full  
**Status**: ✅ Pass

#### B5: Drop Enrolled Section
**Purpose**: Verify student can drop a section  
**Test Account**: `stu1` (enrolled in CS101)  
**Steps**:
1. Go to "Registrations" tab
2. Select enrolled section
3. Click "Drop Selected Section"
4. Confirm in dialog
**Expected**: Success message, section removed from registrations  
**Status**: ✅ Pass

#### B6: View Timetable
**Purpose**: Verify student sees weekly schedule  
**Test Account**: `stu1` (enrolled in multiple sections)  
**Steps**:
1. Navigate to "Timetable" tab
**Expected**: Table shows all enrolled sections with day, time, room, course info  
**Status**: ✅ Pass

#### B7: View Grades
**Purpose**: Verify student can see assessment scores and final grades  
**Test Account**: `stu1` (has grades entered)  
**Steps**:
1. Navigate to "Grades" tab
**Expected**: Table shows course, assessment components, scores, and final grade  
**Status**: ✅ Pass

#### B8: Download Transcript
**Purpose**: Verify student can export transcript to CSV  
**Test Account**: `stu1`  
**Steps**:
1. Go to "Transcript" tab
2. Click "Download Transcript (CSV)"
3. Choose save location
**Expected**: CSV file created with course history and grades  
**Status**: ✅ Pass

---

### Test Category C: Instructor Features

#### C1: View Assigned Sections
**Purpose**: Verify instructor sees their sections  
**Test Account**: `inst1` / `inst123`  
**Steps**:
1. Log in as instructor
2. Check "My Sections" tab
**Expected**: List shows all sections assigned to instructor  
**Status**: ✅ Pass

#### C2: Create Assessment Component
**Purpose**: Verify instructor can define gradebook components  
**Test Account**: `inst1`  
**Steps**:
1. Go to "Gradebook" tab
2. Select a section
3. Click "Add Assessment"
4. Enter name: "Midterm Exam", weight: 30
5. Save
**Expected**: Assessment appears in gradebook with 30% weight  
**Status**: ✅ Pass

#### C3: Enter Student Scores
**Purpose**: Verify instructor can record grades  
**Test Account**: `inst1`  
**Steps**:
1. In gradebook, select student row
2. Click "Enter Score" for an assessment
3. Enter score: 85
4. Save
**Expected**: Score displays in table, weighted total updates  
**Status**: ✅ Pass

#### C4: Compute Final Grades
**Purpose**: Verify automated grade calculation  
**Test Account**: `inst1`  
**Steps**:
1. Ensure all assessment scores entered for a student
2. Click "Compute Final Grades" button
**Expected**: Final percentage and letter grade calculated correctly  
**Status**: ✅ Pass

#### C5: View Class Statistics
**Purpose**: Verify instructor sees class performance metrics  
**Test Account**: `inst1`  
**Steps**:
1. Select section with multiple students
2. Check statistics panel
**Expected**: Shows average score, grade distribution, pass rate  
**Status**: ✅ Pass

---

### Test Category D: Admin Features

#### D1: Create New User
**Purpose**: Verify admin can add users  
**Test Account**: `admin1` / `admin123`  
**Steps**:
1. Go to "Users" tab
2. Enter username: `newstu1`
3. Select role: STUDENT
4. Enter password: `pass123`
5. Click "Create User"
**Expected**: User added to table, can log in with credentials  
**Status**: ✅ Pass

#### D2: Create New Course
**Purpose**: Verify admin can add courses  
**Test Account**: `admin1`  
**Steps**:
1. Go to "Courses" tab
2. Enter code: "CS201", title: "Data Structures", credits: 4
3. Click "Create Course"
**Expected**: Course appears in table and section dropdown  
**Status**: ✅ Pass

#### D3: Create Section via Popup Dialog
**Purpose**: Verify admin can create sections using the new dialog interface  
**Test Account**: `admin1`  
**Steps**:
1. Go to "Sections" tab
2. Click "Create Section" button
3. In popup: Select course, instructor, day, time (09:00-10:30), room (R101), capacity (30), semester (Spring), year (2025)
4. Click "Create"
**Expected**: Dialog closes, section appears in sections table  
**Status**: ✅ Pass

#### D4: Delete Section
**Purpose**: Verify admin can remove sections  
**Test Account**: `admin1`  
**Steps**:
1. In Sections tab, select a section
2. Click "Delete Section"
3. Confirm deletion
**Expected**: Section removed, enrollments and grades cascade deleted  
**Status**: ✅ Pass

#### D5: Assign Instructor to Section
**Purpose**: Verify admin can change section instructor  
**Test Account**: `admin1`  
**Steps**:
1. Select a section
2. Click "Assign Instructor"
3. Choose instructor from dropdown
4. Confirm
**Expected**: Section updated with new instructor  
**Status**: ✅ Pass

#### D6: Toggle Maintenance Mode
**Purpose**: Verify admin can enable/disable maintenance mode  
**Test Account**: `admin1`  
**Steps**:
1. Go to "Maintenance" tab
2. Click "Toggle Maintenance Mode"
3. Confirm
**Expected**: Banner changes to "⚠ Maintenance Mode Active", other users see read-only mode  
**Status**: ✅ Pass

---

## 2. Additional Test Cases

### E1: Maintenance Mode Enforcement
**Purpose**: Verify students/instructors cannot modify data during maintenance  
**Steps**:
1. Admin enables maintenance mode
2. Log in as `stu1`
3. Try to register for a section
**Expected**: Error message about maintenance mode, registration blocked  
**Status**: ✅ Pass

### E2: Table Sorting Functionality
**Purpose**: Verify numeric columns sort correctly  
**Test**: Click on "Credits" column header in catalog  
**Expected**: Courses sort numerically (3, 4, 4) not alphabetically  
**Status**: ✅ Pass

### E3: Session Persistence
**Purpose**: Verify user stays logged in until logout  
**Steps**:
1. Log in as any user
2. Navigate between tabs
3. Click "Logout"
**Expected**: Returns to login screen, requires re-authentication  
**Status**: ✅ Pass

### E4: Role-Based Dashboard Routing
**Purpose**: Verify users see appropriate dashboard for their role  
**Steps**:
1. Log in as student - see student dashboard
2. Log in as instructor - see instructor dashboard
3. Log in as admin - see admin dashboard
**Expected**: Each role sees only their authorized interface  
**Status**: ✅ Pass

### E5: Grade Computation Accuracy
**Purpose**: Verify weighted grade calculation is correct  
**Test Data**: Midterm (30%) = 80, Final (40%) = 90, Assignments (30%) = 85  
**Expected**: Final = (80×0.3 + 90×0.4 + 85×0.3) = 85.5%  
**Status**: ✅ Pass

---

## 3. Test Data

### Pre-loaded Users
| Username | Password | Role | Purpose |
|----------|----------|------|---------|
| admin1 | admin123 | Admin | All admin tests |
| inst1 | inst123 | Instructor | Instructor tests |
| stu1 | stu123 | Student | Primary student tests |
| stu2 | stu123 | Student | Conflict/capacity tests |

### Pre-loaded Courses
| Code | Title | Credits |
|------|-------|---------|
| CS101 | Intro to Programming | 4 |
| MA102 | Calculus I | 4 |
| HS201 | World History | 3 |

### Pre-loaded Sections
| Course | Instructor | Day | Time | Room | Capacity |
|--------|------------|-----|------|------|----------|
| CS101 | Dr. Alice Kapoor | MONDAY | 09:00-10:30 | R201 | 30 |
| CS101 | Dr. Alice Kapoor | WEDNESDAY | 14:00-15:30 | LAB1 | 25 |
| MA102 | Dr. Alice Kapoor | TUESDAY | 10:00-11:30 | R301 | 35 |
| HS201 | Dr. Alice Kapoor | FRIDAY | 13:00-14:30 | R105 | 40 |

---

## 4. Testing Environment

**Hardware**: Any modern PC/Mac with 4GB+ RAM  
**OS**: Windows 10/11, macOS 10.14+, or Linux  
**Java**: JDK 17 or higher  
**Database**: MySQL 8.0+ or in-memory mode  

---

## 5. Test Execution Notes

- Run tests in order (A → B → C → D → E)
- Reset test data between test suites
- For database mode: Run seed scripts before testing
- For in-memory mode: Restart application between test suites
- All passwords use BCrypt hashing (secure)
- Failed login attempts reset after successful login

---

**Total Test Cases**: 28  
**Expected Pass Rate**: 100%
