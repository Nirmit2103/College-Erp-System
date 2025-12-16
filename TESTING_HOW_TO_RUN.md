# How to Run - University ERP Testing Guide

## System Requirements

- **Java Version**: JDK 17 or higher
- **Operating System**: Windows 10+, macOS 10.14+, or Linux
- **Memory**: Minimum 512MB RAM
- **Database** (optional): MySQL 8.0+ or MariaDB 10.5+

## Database Setup

### Two Separate Databases Required

1. **Auth Database** (`univ_auth_db`) - Stores user authentication information
2. **ERP Database** (`univ_erp_db`) - Stores academic data (courses, sections, grades, etc.)

### Database Connection Settings

**Auth Database:**
```
URL: jdbc:mysql://localhost:3306/univ_auth_db
Username: erp_auth
Password: changeme
```

**ERP Database:**
```
URL: jdbc:mysql://localhost:3306/univ_erp_db
Username: erp_app
Password: changeme
```

### Setup Instructions

**Option 1: In-Memory Mode (Default - No Database Required)**
```powershell
# Just run the application - no setup needed
.\run.ps1
```

**Option 2: Database Mode (Persistent Storage)**

1. Create databases and load seed scripts:
```bash
mysql -u root -p < database/auth/schema.sql
mysql -u root -p < database/auth/seed.sql
mysql -u root -p < database/erp/schema.sql
mysql -u root -p < database/erp/seed.sql
```

2. Create database users (optional):
```sql
CREATE USER 'erp_auth'@'localhost' IDENTIFIED BY 'changeme';
GRANT ALL PRIVILEGES ON univ_auth_db.* TO 'erp_auth'@'localhost';

CREATE USER 'erp_app'@'localhost' IDENTIFIED BY 'changeme';
GRANT ALL PRIVILEGES ON univ_erp_db.* TO 'erp_app'@'localhost';

FLUSH PRIVILEGES;
```

3. Edit `src/main/resources/application.properties`:
```properties
storage.mode=jdbc
```

## Default Test Accounts

### Administrator Account
- **Username**: `admin1`
- **Password**: `admin123`
- **Purpose**: Full system access - user/course/section management, maintenance mode

### Instructor Account
- **Username**: `inst1`
- **Password**: `inst123`
- **Name**: Dr. Alice Kapoor
- **Department**: Computer Science
- **Purpose**: Grade management, assessment creation, section management

### Student Accounts

**Student 1:**
- **Username**: `stu1`
- **Password**: `stu123`
- **Roll No**: 2024-CS-001
- **Program**: Computer Science
- **Year**: 1
- **Purpose**: Testing registration, grade viewing, timetable

**Student 2:**
- **Username**: `stu2`
- **Password**: `stu123`
- **Roll No**: 2024-CS-002
- **Program**: Computer Science
- **Year**: 1
- **Purpose**: Testing conflicts, capacity limits, multiple enrollments

## Running the Application

### Windows (PowerShell)
```powershell
# Set Java home (adjust path to your installation)
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"

# Run using the convenience script
.\run.ps1

# OR build and run JAR
mvn clean package -DskipTests
java -jar target\erp-desktop-0.1.0-SNAPSHOT.jar
```

### Mac/Linux
```bash
# Set Java home
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk

# Build and run
./mvnw clean package -DskipTests
java -jar target/erp-desktop-0.1.0-SNAPSHOT.jar
```

## Quick Verification

After starting the application:

1. **Login Screen appears** - Application started successfully
2. **Login as admin1** - Tests authentication system
3. **Check maintenance banner** - Should show "✓ System Online"
4. **Navigate through tabs** - Verify UI loads correctly

## Troubleshooting

**Problem**: Cannot connect to database
- **Solution**: Verify MySQL is running, check credentials in `application.properties`

**Problem**: "JAVA_HOME not found"
- **Solution**: Set JAVA_HOME environment variable to JDK installation path

**Problem**: Build fails
- **Solution**: Run `mvn clean` then try again

**Problem**: Login fails with correct credentials
- **Solution**: Ensure seed data was loaded correctly (check database or restart in memory mode)

---

**For detailed testing procedures, see TEST_PLAN.md**
