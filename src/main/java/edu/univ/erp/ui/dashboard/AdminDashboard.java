package edu.univ.erp.ui.dashboard;

import edu.univ.erp.access.MaintenanceModeGuard;
import edu.univ.erp.api.common.ApiResponse;
import edu.univ.erp.auth.AuthenticatedUser;
import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.Instructor;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.UserRole;
import edu.univ.erp.service.admin.AdminService;
import edu.univ.erp.service.maintenance.MaintenanceService;
import edu.univ.erp.ui.common.MaintenanceBanner;
import edu.univ.erp.ui.common.PrimaryButton;
import edu.univ.erp.ui.common.SecondaryButton;
import edu.univ.erp.util.MessageDialogs;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public class AdminDashboard extends JFrame {

    private final AuthenticatedUser user;
    private final MaintenanceService maintenanceService;
    private final AdminService adminService;
    private final edu.univ.erp.AppBootstrap bootstrap;
    private MaintenanceBanner maintenanceBanner;

    private DefaultTableModel userTableModel;
    private JTable userTable;
    private JTextField usernameField;
    private JComboBox<String> roleComboBox;
    private JTextField passwordField;

    private DefaultTableModel courseTableModel;
    private JTextField courseCodeField;
    private JTextField courseTitleField;
    private JTextField courseCreditsField;

    private DefaultTableModel sectionTableModel;
    private JTable sectionTable;

    public AdminDashboard(AuthenticatedUser user,
                          MaintenanceService maintenanceService,
                          MaintenanceModeGuard maintenanceGuard,
                          AdminService adminService,
                          edu.univ.erp.AppBootstrap bootstrap) {
        this.user = user;
        this.maintenanceService = maintenanceService;
        this.adminService = adminService;
        this.bootstrap = bootstrap;
        setTitle("Admin Dashboard - " + user.username());
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        buildUi(maintenanceGuard);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                if (maintenanceBanner != null) {
                    maintenanceBanner.detach();
                }
            }
        });
    }

    private void buildUi(MaintenanceModeGuard maintenanceGuard) {
        JPanel panel = new JPanel(new BorderLayout(0, 0));

        JPanel header = buildHeader();
        panel.add(header, BorderLayout.NORTH);

        JPanel mainContent = new JPanel(new BorderLayout(0, 20));
        mainContent.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainContent.setBackground(new java.awt.Color(0xF5F5F5));

        maintenanceBanner = new MaintenanceBanner(maintenanceGuard);
        mainContent.add(maintenanceBanner, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        tabs.addTab("Users", buildUserManagementTab());
        tabs.addTab("Courses", buildCourseManagementTab());
        tabs.addTab("Sections", buildSectionManagementTab());
        tabs.addTab("Maintenance", buildMaintenanceTab());
        tabs.addTab("Backup/Restore", buildBackupRestoreTab());

        mainContent.add(tabs, BorderLayout.CENTER);
        panel.add(mainContent, BorderLayout.CENTER);
        setContentPane(panel);

        refreshAll();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new java.awt.Color(0x3F51B5));
        header.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel leftPanel = new JPanel(new BorderLayout(0, 5));
        leftPanel.setOpaque(false);

        JLabel welcomeLabel = new JLabel("Administrator Dashboard - " + user.username());
        welcomeLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 18));
        welcomeLabel.setForeground(java.awt.Color.WHITE);
        leftPanel.add(welcomeLabel, BorderLayout.NORTH);

        JLabel infoLabel = new JLabel("Manage users, courses, sections and system settings");
        infoLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
        infoLabel.setForeground(new java.awt.Color(255, 255, 255, 200));
        leftPanel.add(infoLabel, BorderLayout.SOUTH);

        header.add(leftPanel, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);

        PrimaryButton changePasswordBtn = new PrimaryButton("Change Password");
        changePasswordBtn.addActionListener(e -> openChangePasswordDialog());
        rightPanel.add(changePasswordBtn);

        PrimaryButton logoutButton = new PrimaryButton("Logout");
        logoutButton.setBackground(new java.awt.Color(0xF44336));
        logoutButton.addActionListener(e -> logout());
        rightPanel.add(logoutButton);

        header.add(rightPanel, BorderLayout.EAST);

        return header;
    }

    private void logout() {
        dispose();
        javax.swing.SwingUtilities.invokeLater(() -> {
            edu.univ.erp.ui.auth.LoginFrame loginFrame = new edu.univ.erp.ui.auth.LoginFrame();
            new edu.univ.erp.ui.auth.LoginController(bootstrap, loginFrame);
            loginFrame.setVisible(true);
        });
    }

    private void openChangePasswordDialog() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new java.awt.Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Current Password:"), gbc);
        gbc.gridx = 1;
        JPasswordField currentPwdField = new JPasswordField(20);
        panel.add(currentPwdField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("New Password:"), gbc);
        gbc.gridx = 1;
        JPasswordField newPwdField = new JPasswordField(20);
        panel.add(newPwdField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Confirm Password:"), gbc);
        gbc.gridx = 1;
        JPasswordField confirmPwdField = new JPasswordField(20);
        panel.add(confirmPwdField, gbc);

        int result = JOptionPane.showConfirmDialog(this, panel, "Change Password", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String currentPwd = new String(currentPwdField.getPassword());
            String newPwd = new String(newPwdField.getPassword());
            String confirmPwd = new String(confirmPwdField.getPassword());

            if (currentPwd.isEmpty() || newPwd.isEmpty()) {
                MessageDialogs.showError(this, "Please fill all fields");
                return;
            }

            if (!newPwd.equals(confirmPwd)) {
                MessageDialogs.showError(this, "New passwords do not match");
                return;
            }

            if (newPwd.length() < 6) {
                MessageDialogs.showError(this, "Password must be at least 6 characters");
                return;
            }

            boolean success = bootstrap.authService().changePassword(currentPwd, newPwd);
            if (success) {
                MessageDialogs.showInfo(this, "Password changed successfully");
            } else {
                MessageDialogs.showError(this, "Current password is incorrect");
            }
        }
    }

    private JPanel buildUserManagementTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new java.awt.Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        form.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        usernameField = new JTextField(20);
        form.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        form.add(new JLabel("Role:"), gbc);
        gbc.gridx = 1;
        roleComboBox = new JComboBox<>(new String[]{"STUDENT", "INSTRUCTOR", "ADMIN"});
        form.add(roleComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        form.add(new JLabel("Temporary Password:"), gbc);
        gbc.gridx = 1;
        passwordField = new JTextField(20);
        form.add(passwordField, gbc);

        gbc.gridx = 1; gbc.gridy = 3;
        PrimaryButton createUserBtn = new PrimaryButton("Create User");
        createUserBtn.addActionListener(e -> createUser());
        form.add(createUserBtn, gbc);

        userTableModel = new DefaultTableModel(new Object[]{"User ID", "Username", "Role"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(userTableModel);
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        this.userTable = table;

        JPanel top = new JPanel(new BorderLayout());
        top.add(form, BorderLayout.NORTH);
        PrimaryButton refreshBtn = new PrimaryButton("Refresh");
        refreshBtn.addActionListener(e -> refreshUsers());

        PrimaryButton reconcileBtn = new PrimaryButton("Reconcile Instructors");
        reconcileBtn.addActionListener(e -> {
            var response = adminService.reconcileInstructorProfiles(user);
            if (response.success()) {
                MessageDialogs.showInfo(this, response.payload().orElse("Reconcile complete."));
                refreshUsers();
                refreshSections();
            } else {
                response.error().map(ApiResponse.ErrorInfo::message).ifPresent(msg -> MessageDialogs.showError(this, msg));
            }
        });
        javax.swing.JPanel southPanel = new javax.swing.JPanel(new java.awt.FlowLayout());
        southPanel.add(refreshBtn);
        southPanel.add(reconcileBtn);
        top.add(southPanel, BorderLayout.SOUTH);

        panel.add(top, BorderLayout.NORTH);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);

        PrimaryButton deleteBtn = new PrimaryButton("Delete Selected User");
        deleteBtn.addActionListener(e -> deleteSelectedUser());
        tablePanel.add(deleteBtn, BorderLayout.SOUTH);

        panel.add(tablePanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildCourseManagementTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new java.awt.Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        form.add(new JLabel("Course Code:"), gbc);
        gbc.gridx = 1;
        courseCodeField = new JTextField(20);
        form.add(courseCodeField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        form.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1;
        courseTitleField = new JTextField(20);
        form.add(courseTitleField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        form.add(new JLabel("Credits:"), gbc);
        gbc.gridx = 1;
        courseCreditsField = new JTextField(20);
        form.add(courseCreditsField, gbc);

        gbc.gridx = 1; gbc.gridy = 3;
        PrimaryButton createCourseBtn = new PrimaryButton("Create Course");
        createCourseBtn.addActionListener(e -> createCourse());
        form.add(createCourseBtn, gbc);

        courseTableModel = new DefaultTableModel(new Object[]{"Course ID", "Code", "Title", "Credits"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(courseTableModel);
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);

        JPanel top = new JPanel(new BorderLayout());
        top.add(form, BorderLayout.NORTH);
        PrimaryButton refreshBtn = new PrimaryButton("Refresh");
        refreshBtn.addActionListener(e -> refreshCourses());
        javax.swing.JPanel south = new javax.swing.JPanel(new java.awt.FlowLayout());
        south.add(refreshBtn);
        PrimaryButton deleteCourseBtn = new PrimaryButton("Delete Selected Course");
        deleteCourseBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow < 0) {
                MessageDialogs.showError(this, "Select a course first");
                return;
            }
            Long courseId = (Long) courseTableModel.getValueAt(selectedRow, 0);
            String code = (String) courseTableModel.getValueAt(selectedRow, 1);
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Delete course " + code + "? This will remove its sections, enrollments and grades.",
                    "Confirm Delete Course",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                var response = adminService.deleteCourse(user, courseId);
                if (response.success()) {
                    MessageDialogs.showInfo(this, "Course deleted successfully");
                    refreshCourses();
                    refreshSections();
                } else {
                    response.error().map(ApiResponse.ErrorInfo::message).ifPresent(msg -> MessageDialogs.showError(this, msg));
                }
            }
        });
        south.add(deleteCourseBtn);
        top.add(south, BorderLayout.SOUTH);

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildSectionManagementTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));

        sectionTableModel = new DefaultTableModel(new Object[]{"Section ID", "Course Code", "Instructor", "Day", "Time", "Room", "Capacity", "Semester", "Year"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        sectionTable = new JTable(sectionTableModel);
        sectionTable.setAutoCreateRowSorter(true);
        sectionTable.setFillsViewportHeight(true);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));

        PrimaryButton createSectionBtn = new PrimaryButton("Create Section");
        createSectionBtn.addActionListener(e -> openCreateSectionDialog());
        buttonPanel.add(createSectionBtn);

        PrimaryButton deleteSectionBtn = new PrimaryButton("Delete Section");
        deleteSectionBtn.addActionListener(e -> deleteSelectedSection());
        buttonPanel.add(deleteSectionBtn);

        PrimaryButton assignBtn = new PrimaryButton("Assign Instructor");
        assignBtn.addActionListener(e -> assignInstructor());
        buttonPanel.add(assignBtn);

        PrimaryButton refreshBtn = new PrimaryButton("Refresh");
        refreshBtn.addActionListener(e -> refreshSections());
        buttonPanel.add(refreshBtn);

        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(sectionTable), BorderLayout.CENTER);

        return panel;
    }

    private void openCreateSectionDialog() {
        JDialog dialog = new JDialog(this, "Create New Section", true);
        dialog.setSize(500, 550);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new java.awt.Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        form.add(new JLabel("Course:"), gbc);
        gbc.gridx = 1;
        JComboBox<Course> courseCombo = new JComboBox<>();
        courseCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(javax.swing.JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Course course) {
                    setText(course.code() + " - " + course.title());
                }
                return this;
            }
        });

        List<Course> courses = adminService.listCourses(user);
        courses.forEach(courseCombo::addItem);
        form.add(courseCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        form.add(new JLabel("Instructor:"), gbc);
        gbc.gridx = 1;
        JComboBox<Instructor> instructorCombo = new JComboBox<>();
        instructorCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(javax.swing.JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Instructor instructor) {
                    setText(instructor.fullName() + " (" + instructor.department() + ")");
                }
                return this;
            }
        });

        List<Instructor> instructors = adminService.listInstructors(user);
        instructors.forEach(instructorCombo::addItem);
        form.add(instructorCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        form.add(new JLabel("Day:"), gbc);
        gbc.gridx = 1;
        JComboBox<DayOfWeek> dayCombo = new JComboBox<>(DayOfWeek.values());
        form.add(dayCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        form.add(new JLabel("Start Time (HH:MM):"), gbc);
        gbc.gridx = 1;
        JTextField startTimeField = new JTextField(10);
        startTimeField.setToolTipText("Format: HH:MM (e.g., 09:00)");
        form.add(startTimeField, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        form.add(new JLabel("End Time (HH:MM):"), gbc);
        gbc.gridx = 1;
        JTextField endTimeField = new JTextField(10);
        endTimeField.setToolTipText("Format: HH:MM (e.g., 10:30)");
        form.add(endTimeField, gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        form.add(new JLabel("Room:"), gbc);
        gbc.gridx = 1;
        JTextField roomField = new JTextField(20);
        form.add(roomField, gbc);

        gbc.gridx = 0; gbc.gridy = 6;
        form.add(new JLabel("Capacity:"), gbc);
        gbc.gridx = 1;
        JTextField capacityField = new JTextField(10);
        form.add(capacityField, gbc);

        gbc.gridx = 0; gbc.gridy = 7;
        form.add(new JLabel("Semester:"), gbc);
        gbc.gridx = 1;
        JTextField semesterField = new JTextField(10);
        semesterField.setToolTipText("e.g., Spring, Fall");
        form.add(semesterField, gbc);

        gbc.gridx = 0; gbc.gridy = 8;
        form.add(new JLabel("Year:"), gbc);
        gbc.gridx = 1;
        JTextField yearField = new JTextField(10);
        form.add(yearField, gbc);

        gbc.gridx = 0; gbc.gridy = 9; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel buttonPanelDialog = new JPanel(new FlowLayout());

        PrimaryButton createBtn = new PrimaryButton("Create");
        createBtn.addActionListener(e -> {

            Course selectedCourse = (Course) courseCombo.getSelectedItem();
            Instructor selectedInstructor = (Instructor) instructorCombo.getSelectedItem();

            if (selectedCourse == null || selectedInstructor == null) {
                MessageDialogs.showError(dialog, "Please select both course and instructor");
                return;
            }

            try {
                DayOfWeek day = (DayOfWeek) dayCombo.getSelectedItem();
                String startTime = startTimeField.getText().trim();
                String endTime = endTimeField.getText().trim();
                String room = roomField.getText().trim();
                int capacity = Integer.parseInt(capacityField.getText().trim());
                String semester = semesterField.getText().trim();
                int year = Integer.parseInt(yearField.getText().trim());

                if (startTime.isEmpty() || endTime.isEmpty() || room.isEmpty() || semester.isEmpty()) {
                    MessageDialogs.showError(dialog, "Please fill in all fields");
                    return;
                }

                LocalTime startLocalTime = LocalTime.parse(startTime);
                LocalTime endLocalTime = LocalTime.parse(endTime);

                Section section = new Section(0, selectedCourse.id(), 
                    selectedInstructor.userId(), day, startLocalTime, endLocalTime, 
                    room, capacity, semester, year);

                var response = adminService.createSection(user, section);

                if (response.success()) {
                    MessageDialogs.showInfo(dialog, "Section created successfully");
                    refreshSections();
                    dialog.dispose();
                } else {
                    response.error().map(ApiResponse.ErrorInfo::message)
                        .ifPresent(msg -> MessageDialogs.showError(dialog, msg));
                }
            } catch (NumberFormatException ex) {
                MessageDialogs.showError(dialog, "Invalid number format for capacity or year");
            }
        });
        buttonPanelDialog.add(createBtn);

        SecondaryButton cancelBtn = new SecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());
        buttonPanelDialog.add(cancelBtn);

        form.add(buttonPanelDialog, gbc);

        dialog.add(new JScrollPane(form));
        dialog.setVisible(true);
    }

    private void deleteSelectedSection() {
        int selectedRow = sectionTable.getSelectedRow();
        if (selectedRow < 0) {
            MessageDialogs.showError(this, "Please select a section to delete");
            return;
        }

        Long sectionId = (Long) sectionTableModel.getValueAt(selectedRow, 0);
        String courseCode = (String) sectionTableModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete section " + sectionId + " for course " + courseCode + "?\nThis will remove all enrollments and grades.",
                "Confirm Delete Section",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            var response = adminService.deleteSection(user, sectionId);
            if (response.success()) {
                MessageDialogs.showInfo(this, "Section deleted successfully");
                refreshSections();
            } else {
                response.error().map(ApiResponse.ErrorInfo::message)
                    .ifPresent(msg -> MessageDialogs.showError(this, msg));
            }
        }
    }

    private JPanel buildMaintenanceTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel center = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new java.awt.Insets(20, 20, 20, 20);

        gbc.gridx = 0; gbc.gridy = 0;
        center.add(new JLabel("Maintenance Mode Control"), gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        PrimaryButton toggleButton = new PrimaryButton("Toggle Maintenance Mode");
        toggleButton.addActionListener(e -> toggleMaintenance());
        center.add(toggleButton, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        center.add(new JLabel("<html><p style='width:400px;text-align:center;'>When maintenance mode is ON,<br>students and instructors can view but cannot make changes.</p></html>"), gbc);

        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildBackupRestoreTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));

        PrimaryButton createBackupBtn = new PrimaryButton("Create Backup");
        createBackupBtn.addActionListener(e -> createBackup());
        topPanel.add(createBackupBtn);

        PrimaryButton restoreBackupBtn = new PrimaryButton("Restore Selected Backup");
        restoreBackupBtn.addActionListener(e -> restoreSelectedBackup());
        topPanel.add(restoreBackupBtn);

        PrimaryButton deleteBackupBtn = new PrimaryButton("Delete Selected Backup");
        deleteBackupBtn.addActionListener(e -> deleteSelectedBackup());
        topPanel.add(deleteBackupBtn);

        PrimaryButton refreshBtn = new PrimaryButton("Refresh List");
        refreshBtn.addActionListener(e -> refreshBackupList());
        topPanel.add(refreshBtn);

        panel.add(topPanel, BorderLayout.NORTH);

        DefaultTableModel backupTableModel = new DefaultTableModel(new Object[]{"Backup Name", "Created"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable backupTable = new JTable(backupTableModel);
        backupTable.setAutoCreateRowSorter(true);
        backupTable.setFillsViewportHeight(true);
        panel.add(new JScrollPane(backupTable), BorderLayout.CENTER);

        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 0, 0, 0));
        JLabel infoLabel = new JLabel("<html><b>Backup/Restore Information:</b><br>" +
                "• Backups save all data files (courses, sections, enrollments, grades, users)<br>" +
                "• Restore will overwrite current data with backup data<br>" +
                "• Create backups before major data changes<br>" +
                "• Backups are stored in the 'backups/' folder</html>");
        infoLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
        infoPanel.add(infoLabel, BorderLayout.CENTER);
        panel.add(infoPanel, BorderLayout.SOUTH);

        panel.putClientProperty("backupTable", backupTable);
        panel.putClientProperty("backupTableModel", backupTableModel);

        refreshBackupList(backupTable, backupTableModel);

        return panel;
    }

    private void createBackup() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Create a backup of all current data?",
                "Confirm Backup",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        ApiResponse<String> response = adminService.createBackup(user);
        if (response.success()) {
            String backupName = response.payload().orElse("unknown");
            MessageDialogs.showInfo(this, "Backup created successfully: " + backupName);
            refreshBackupList();
        } else {
            String errorMsg = response.error().map(e -> e.message()).orElse("Failed to create backup");
            MessageDialogs.showError(this, errorMsg);
        }
    }

    private void restoreSelectedBackup() {
        JPanel backupTab = findBackupRestoreTab();
        if (backupTab == null) return;

        JTable backupTable = (JTable) backupTab.getClientProperty("backupTable");
        int selectedRow = backupTable.getSelectedRow();

        if (selectedRow < 0) {
            MessageDialogs.showError(this, "Please select a backup to restore");
            return;
        }

        int modelRow = backupTable.convertRowIndexToModel(selectedRow);
        DefaultTableModel model = (DefaultTableModel) backupTable.getModel();
        String backupName = (String) model.getValueAt(modelRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Restore backup: " + backupName + "?\n\nWARNING: This will overwrite all current data!",
                "Confirm Restore",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        ApiResponse<Void> response = adminService.restoreBackup(user, backupName);
        if (response.success()) {
            MessageDialogs.showInfo(this, "Backup restored successfully. Please restart the application for changes to take effect.");
        } else {
            String errorMsg = response.error().map(e -> e.message()).orElse("Failed to restore backup");
            MessageDialogs.showError(this, errorMsg);
        }
    }

    private void deleteSelectedBackup() {
        JPanel backupTab = findBackupRestoreTab();
        if (backupTab == null) return;

        JTable backupTable = (JTable) backupTab.getClientProperty("backupTable");
        int selectedRow = backupTable.getSelectedRow();

        if (selectedRow < 0) {
            MessageDialogs.showError(this, "Please select a backup to delete");
            return;
        }

        int modelRow = backupTable.convertRowIndexToModel(selectedRow);
        DefaultTableModel model = (DefaultTableModel) backupTable.getModel();
        String backupName = (String) model.getValueAt(modelRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete backup: " + backupName + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        ApiResponse<Void> response = adminService.deleteBackup(user, backupName);
        if (response.success()) {
            MessageDialogs.showInfo(this, "Backup deleted successfully");
            refreshBackupList();
        } else {
            String errorMsg = response.error().map(e -> e.message()).orElse("Failed to delete backup");
            MessageDialogs.showError(this, errorMsg);
        }
    }

    private void refreshBackupList() {
        JPanel backupTab = findBackupRestoreTab();
        if (backupTab == null) return;

        JTable backupTable = (JTable) backupTab.getClientProperty("backupTable");
        DefaultTableModel backupTableModel = (DefaultTableModel) backupTab.getClientProperty("backupTableModel");
        refreshBackupList(backupTable, backupTableModel);
    }

    private void refreshBackupList(JTable backupTable, DefaultTableModel backupTableModel) {
        backupTableModel.setRowCount(0);
        List<String> backups = adminService.listBackups(user);

        for (String backupName : backups) {

            String timestamp = backupName.replace("backup_", "").replace("_", " ");
            backupTableModel.addRow(new Object[]{backupName, timestamp});
        }
    }

    private JPanel findBackupRestoreTab() {
        java.awt.Container contentPane = getContentPane();
        if (contentPane instanceof JPanel mainPanel) {
            for (java.awt.Component comp : mainPanel.getComponents()) {
                if (comp instanceof JPanel borderLayoutPanel) {
                    for (java.awt.Component child : borderLayoutPanel.getComponents()) {
                        if (child instanceof JTabbedPane tabs) {
                            int tabCount = tabs.getTabCount();
                            for (int i = 0; i < tabCount; i++) {
                                if ("Backup/Restore".equals(tabs.getTitleAt(i))) {
                                    return (JPanel) tabs.getComponentAt(i);
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private void createUser() {
        String username = usernameField.getText().trim();
        String role = (String) roleComboBox.getSelectedItem();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            MessageDialogs.showError(this, "Please fill all fields");
            return;
        }

        ApiResponse<Void> response = adminService.createUser(user, username, role, password);
        if (response.success()) {
            MessageDialogs.showInfo(this, "User created successfully. Username: " + username);
            usernameField.setText("");
            passwordField.setText("");
            refreshUsers();

            if ("INSTRUCTOR".equals(role)) {

                Long createdUserId = adminService.listUsers(user).stream()
                        .filter(r -> r.username().equalsIgnoreCase(username))
                        .map(r -> r.userId())
                        .findFirst().orElse(null);

                if (createdUserId != null) {
                    String firstName = javax.swing.JOptionPane.showInputDialog(this, "Instructor First Name:", "First Name", javax.swing.JOptionPane.QUESTION_MESSAGE);
                    if (firstName == null || firstName.trim().isEmpty()) {
                        MessageDialogs.showInfo(this, "Instructor profile not created: first name missing.");
                    } else {
                        String lastName = javax.swing.JOptionPane.showInputDialog(this, "Instructor Last Name:", "Last Name", javax.swing.JOptionPane.QUESTION_MESSAGE);
                        if (lastName == null || lastName.trim().isEmpty()) {
                            MessageDialogs.showInfo(this, "Instructor profile not created: last name missing.");
                        } else {
                            String department = javax.swing.JOptionPane.showInputDialog(this, "Department:", "Department", javax.swing.JOptionPane.QUESTION_MESSAGE);
                            if (department == null || department.trim().isEmpty()) {
                                department = "General";
                            }
                            ApiResponse<edu.univ.erp.domain.Instructor> ip = adminService.createInstructorProfile(user, createdUserId, firstName.trim(), lastName.trim(), department.trim());
                            if (ip.success()) {
                                String name = ip.payload().map(i -> i.fullName()).orElse("(unknown)");
                                MessageDialogs.showInfo(this, "Instructor profile created: " + name);
                            } else {
                                String err = ip.error().map(e -> e.message()).orElse("Failed to create instructor profile");
                                MessageDialogs.showError(this, err);
                            }
                        }
                    }
                }
                refreshSections();
            }
        } else {
            String errorMsg = response.error().map(e -> e.message()).orElse("Failed to create user");
            MessageDialogs.showError(this, errorMsg);
        }
    }

    private void createCourse() {
        String code = courseCodeField.getText().trim();
        String title = courseTitleField.getText().trim();
        String creditsStr = courseCreditsField.getText().trim();

        if (code.isEmpty() || title.isEmpty() || creditsStr.isEmpty()) {
            MessageDialogs.showError(this, "Please fill all fields");
            return;
        }

        try {
            int credits = Integer.parseInt(creditsStr);
            Course course = new Course(0, code, title, credits, null);
            ApiResponse<Course> response = adminService.createCourse(user, course);
            if (response.success()) {
                MessageDialogs.showInfo(this, "Course created successfully");
                courseCodeField.setText("");
                courseTitleField.setText("");
                courseCreditsField.setText("");

                response.payload().ifPresent(created -> {
                    courseTableModel.addRow(new Object[]{created.id(), created.code(), created.title(), created.credits()});
                });

                refreshSections();
            } else {
                String errorMsg = response.error().map(e -> e.message()).orElse("Failed to create course");
                MessageDialogs.showError(this, errorMsg);
            }
        } catch (NumberFormatException e) {
            MessageDialogs.showError(this, "Credits must be a number");
        }
    }

    private void assignInstructor() {
        int selectedRow = sectionTable.getSelectedRow();
        if (selectedRow < 0) {
            MessageDialogs.showError(this, "Please select a section from the table");
            return;
        }

        Long sectionId = (Long) sectionTableModel.getValueAt(selectedRow, 0);
        Instructor instructor = (Instructor) JOptionPane.showInputDialog(
                this,
                "Select instructor:",
                "Assign Instructor",
                JOptionPane.QUESTION_MESSAGE,
                null,
                adminService.listInstructors(user).toArray(),
                null);

        if (instructor != null) {
            ApiResponse<Void> response = adminService.assignInstructor(user, sectionId, instructor.userId());
            if (response.success()) {
                MessageDialogs.showInfo(this, "Instructor assigned successfully");
                refreshSections();
            } else {
                String errorMsg = response.error().map(e -> e.message()).orElse("Failed to assign instructor");
                MessageDialogs.showError(this, errorMsg);
            }
        }
    }

    private void toggleMaintenance() {
        try {
            boolean newStatus = maintenanceService.toggle(user).maintenanceOn();
            MessageDialogs.showInfo(this,
                    "Maintenance mode is now " + (newStatus ? "ON" : "OFF") + ".");
        } catch (Exception ex) {
            MessageDialogs.showError(this, ex.getMessage());
        }
    }

    private void refreshAll() {
        refreshUsers();
        refreshCourses();
        refreshSections();
    }

    private void refreshUsers() {
        userTableModel.setRowCount(0);
        List<edu.univ.erp.data.auth.AuthUserRecord> users = adminService.listUsers(user);
        for (edu.univ.erp.data.auth.AuthUserRecord record : users) {
            String status = record.active() ? "Active" : "Inactive";
            userTableModel.addRow(new Object[]{record.userId(), record.username(), record.role(), status});
        }
    }

    private void deleteSelectedUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow < 0) {
            MessageDialogs.showError(this, "Please select a user to delete");
            return;
        }

        Long userId = (Long) userTableModel.getValueAt(selectedRow, 0);
        String username = (String) userTableModel.getValueAt(selectedRow, 1);

        int confirm = javax.swing.JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete user '" + username + "'?",
                "Confirm Delete",
                javax.swing.JOptionPane.YES_NO_OPTION);

        if (confirm == javax.swing.JOptionPane.YES_OPTION) {
            ApiResponse<Void> response = adminService.deleteUser(user, userId);
            if (response.success()) {
                MessageDialogs.showInfo(this, "User deleted successfully");
                refreshUsers();

                refreshSections();
            } else {
                String errorMsg = response.error().map(e -> e.message()).orElse("Failed to delete user");
                MessageDialogs.showError(this, errorMsg);
            }
        }
    }

    private void refreshCourses() {
        courseTableModel.setRowCount(0);
        List<Course> courses = adminService.listCourses(user);
        for (Course course : courses) {
            courseTableModel.addRow(new Object[]{course.id(), course.code(), course.title(), course.credits()});
        }
    }

    private void refreshSections() {
        sectionTableModel.setRowCount(0);
        List<Section> sections = adminService.listSections(user);
        List<Course> courses = adminService.listCourses(user);
        List<Instructor> instructors = adminService.listInstructors(user);

        for (Section section : sections) {
            Course course = courses.stream().filter(c -> c.id() == section.courseId()).findFirst().orElse(null);
            Instructor instructor = instructors.stream().filter(i -> i.userId() == section.instructorId()).findFirst().orElse(null);
            String courseName = course != null ? course.code() : "Unknown";
            String instructorName = instructor != null ? instructor.fullName() : "TBA";
            String time = section.startTime() + " - " + section.endTime();

            sectionTableModel.addRow(new Object[]{
                    section.id(),
                    courseName,
                    instructorName,
                    section.dayOfWeek(),
                    time,
                    section.room(),
                    section.capacity(),
                    section.semester(),
                    section.year()
            });
        }
    }
}
