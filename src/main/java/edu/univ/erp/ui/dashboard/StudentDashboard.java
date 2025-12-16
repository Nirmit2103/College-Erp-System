package edu.univ.erp.ui.dashboard;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;

import edu.univ.erp.access.MaintenanceModeGuard;
import edu.univ.erp.api.common.ApiResponse;
import edu.univ.erp.api.types.SectionRow;
import edu.univ.erp.auth.AuthenticatedUser;
import edu.univ.erp.domain.GradeView;
import edu.univ.erp.domain.TimetableEntry;
import edu.univ.erp.service.student.StudentService;
import edu.univ.erp.ui.common.MaintenanceBanner;
import edu.univ.erp.ui.common.PrimaryButton;
import edu.univ.erp.util.MessageDialogs;

public class StudentDashboard extends JFrame {

    private final AuthenticatedUser user;
    private MaintenanceBanner maintenanceBanner;
    private final StudentService studentService;
    private final edu.univ.erp.AppBootstrap bootstrap;
    private final SectionTableModel catalogModel = new SectionTableModel();
    private final SectionTableModel registrationModel = new SectionTableModel();
    private final TimetableTableModel timetableModel = new TimetableTableModel();
    private final GradeTableModel gradeTableModel = new GradeTableModel();

    private final java.util.function.LongConsumer gradeUpdateListener = enrollmentId -> javax.swing.SwingUtilities.invokeLater(this::refreshGrades);

    private JTable catalogTable;
    private JTable registrationTable;

    public StudentDashboard(AuthenticatedUser user,
                            MaintenanceModeGuard maintenanceGuard,
                            StudentService studentService,
                            edu.univ.erp.AppBootstrap bootstrap) {
        this.user = user;
        this.studentService = studentService;
        this.bootstrap = bootstrap;
        setTitle("Student Dashboard");
        setSize(1024, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        buildUi(maintenanceGuard);

        edu.univ.erp.util.GradeUpdateNotifier.addListener(gradeUpdateListener);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                if (maintenanceBanner != null) {
                    maintenanceBanner.detach();
                }

                edu.univ.erp.util.GradeUpdateNotifier.removeListener(gradeUpdateListener);
            }
        });
        refreshAll();
    }

    private void buildUi(MaintenanceModeGuard maintenanceGuard) {
        JPanel panel = new JPanel(new BorderLayout(0, 0));

        JPanel header = buildHeader();
        panel.add(header, BorderLayout.NORTH);

        JPanel mainContent = new JPanel(new BorderLayout(0, 20));
        mainContent.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainContent.setBackground(edu.univ.erp.ui.common.UIColors.BACKGROUND);

        maintenanceBanner = new MaintenanceBanner(maintenanceGuard);
        mainContent.add(maintenanceBanner, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        tabs.addTab("Dashboard", buildDashboardTab());
        tabs.addTab("Course Catalog", buildCatalogTab());
        tabs.addTab("My Registrations", buildRegistrationsTab());
        tabs.addTab("Timetable", buildTimetableTab());
        tabs.addTab("Grades & Transcript", buildGradesTab());

        mainContent.add(tabs, BorderLayout.CENTER);
        panel.add(mainContent, BorderLayout.CENTER);
        setContentPane(panel);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(edu.univ.erp.ui.common.UIColors.PRIMARY);
        header.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel leftPanel = new JPanel(new BorderLayout(0, 5));
        leftPanel.setOpaque(false);

        JLabel welcomeLabel = new JLabel("Good Morning, " + user.username() + "! Welcome to IIIT-Delhi.");
        welcomeLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 18));
        welcomeLabel.setForeground(java.awt.Color.WHITE);
        leftPanel.add(welcomeLabel, BorderLayout.NORTH);

        JLabel lastLoginLabel = new JLabel("Your last login was on " + new java.text.SimpleDateFormat("MMM dd, hh:mm a").format(new java.util.Date()));
        lastLoginLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
        lastLoginLabel.setForeground(new java.awt.Color(255, 255, 255, 200));
        leftPanel.add(lastLoginLabel, BorderLayout.SOUTH);

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

        int result = javax.swing.JOptionPane.showConfirmDialog(this, panel, "Change Password", javax.swing.JOptionPane.OK_CANCEL_OPTION);
        if (result == javax.swing.JOptionPane.OK_OPTION) {
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

    private JPanel buildDashboardTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setBackground(edu.univ.erp.ui.common.UIColors.BACKGROUND);
        panel.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel cardsPanel = new JPanel(new java.awt.GridLayout(2, 3, 20, 20));
        cardsPanel.setOpaque(false);

        edu.univ.erp.ui.common.DashboardCard catalogCard = new edu.univ.erp.ui.common.DashboardCard(
            "", "Course Catalog", "Browse Courses", edu.univ.erp.ui.common.UIColors.CARD_BLUE);
        catalogCard.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                ((JTabbedPane)panel.getParent()).setSelectedIndex(1);
            }
        });

        edu.univ.erp.ui.common.DashboardCard registrationCard = new edu.univ.erp.ui.common.DashboardCard(
            "", "My Registrations", "View Enrolled", edu.univ.erp.ui.common.UIColors.CARD_GREEN);
        registrationCard.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                ((JTabbedPane)panel.getParent()).setSelectedIndex(2);
            }
        });

        edu.univ.erp.ui.common.DashboardCard timetableCard = new edu.univ.erp.ui.common.DashboardCard(
            "", "My Timetable", "Class Schedule", edu.univ.erp.ui.common.UIColors.CARD_PURPLE);
        timetableCard.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                ((JTabbedPane)panel.getParent()).setSelectedIndex(3);
            }
        });

        edu.univ.erp.ui.common.DashboardCard gradesCard = new edu.univ.erp.ui.common.DashboardCard(
            "", "Grades", "View Performance", edu.univ.erp.ui.common.UIColors.CARD_ORANGE);
        gradesCard.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                ((JTabbedPane)panel.getParent()).setSelectedIndex(4);
            }
        });

        edu.univ.erp.ui.common.DashboardCard transcriptCard = new edu.univ.erp.ui.common.DashboardCard(
            "", "Transcript", "Download Record", edu.univ.erp.ui.common.UIColors.CARD_TEAL);
        transcriptCard.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                downloadTranscript();
            }
        });

        cardsPanel.add(catalogCard);
        cardsPanel.add(registrationCard);
        cardsPanel.add(timetableCard);
        cardsPanel.add(gradesCard);
        cardsPanel.add(transcriptCard);

        panel.add(cardsPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildCatalogTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        catalogTable = new JTable(catalogModel);
        catalogTable.setAutoCreateRowSorter(true);
        catalogTable.setFillsViewportHeight(true);
        panel.add(new JScrollPane(catalogTable), BorderLayout.CENTER);

        PrimaryButton registerButton = new PrimaryButton("Register Selected Section");
        registerButton.addActionListener(e -> registerSelectedSection());
        panel.add(registerButton, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildRegistrationsTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        registrationTable = new JTable(registrationModel);
        registrationTable.setAutoCreateRowSorter(true);
        registrationTable.setFillsViewportHeight(true);
        panel.add(new JScrollPane(registrationTable), BorderLayout.CENTER);

        PrimaryButton dropButton = new PrimaryButton("Drop Selected Section");
        dropButton.addActionListener(e -> dropSelectedSection());
        panel.add(dropButton, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildTimetableTab() {
        JPanel panel = new JPanel(new BorderLayout());
        JTable timetableTable = new JTable(timetableModel);
        timetableTable.setAutoCreateRowSorter(true);
        timetableTable.setFillsViewportHeight(true);
        panel.add(new JScrollPane(timetableTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildGradesTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        JTable gradesTable = new JTable(gradeTableModel);
        gradesTable.setAutoCreateRowSorter(true);
        gradesTable.setFillsViewportHeight(true);
        panel.add(new JScrollPane(gradesTable), BorderLayout.CENTER);

        PrimaryButton downloadTranscriptButton = new PrimaryButton("Download Transcript (CSV)");
        downloadTranscriptButton.addActionListener(e -> downloadTranscript());
        panel.add(downloadTranscriptButton, BorderLayout.SOUTH);
        return panel;
    }

    private void refreshAll() {
        refreshCatalog();
        refreshRegistrations();
        refreshTimetable();
        refreshGrades();
    }

    private void refreshCatalog() {
        try {
            var result = studentService.browseCatalog(user, 0, 200);
            catalogModel.setRows(result.items());
        } catch (Exception ex) {
            MessageDialogs.showError(this, "Failed to load catalog: " + ex.getMessage());
        }
    }

    private void refreshRegistrations() {
        try {
            List<SectionRow> registrations = studentService.myRegistrations(user);
            registrationModel.setRows(registrations);
        } catch (Exception ex) {
            MessageDialogs.showError(this, "Failed to load registrations: " + ex.getMessage());
        }
    }

    private void refreshTimetable() {
        try {
            List<TimetableEntry> entries = studentService.timetable(user);
            timetableModel.setEntries(entries);
        } catch (Exception ex) {
            MessageDialogs.showError(this, "Failed to load timetable: " + ex.getMessage());
        }
    }

    private void refreshGrades() {
        try {
            List<GradeView> grades = studentService.gradeReport(user);
            gradeTableModel.setRows(grades);
        } catch (Exception ex) {
            MessageDialogs.showError(this, "Failed to load grades: " + ex.getMessage());
        }
    }

    private void registerSelectedSection() {
        if (catalogTable.getSelectedRow() < 0) {
            MessageDialogs.showInfo(this, "Select a section to register.");
            return;
        }
        int modelRow = catalogTable.convertRowIndexToModel(catalogTable.getSelectedRow());
        SectionRow row = catalogModel.getRow(modelRow);
        try {
            ApiResponse<Void> response = studentService.register(user, row.sectionId());
            if (response.success()) {
                MessageDialogs.showInfo(this, "Registration successful.");
                refreshAll();
            } else {
                response.error().map(ApiResponse.ErrorInfo::message)
                        .ifPresent(msg -> MessageDialogs.showError(this, msg));
            }
        } catch (Exception ex) {
            MessageDialogs.showError(this, "Registration failed: " + ex.getMessage());
        }
    }

    private void dropSelectedSection() {
        if (registrationTable.getSelectedRow() < 0) {
            MessageDialogs.showInfo(this, "Select a section to drop.");
            return;
        }
        int modelRow = registrationTable.convertRowIndexToModel(registrationTable.getSelectedRow());
        SectionRow row = registrationModel.getRow(modelRow);
        try {
            ApiResponse<Void> response = studentService.drop(user, row.sectionId());
            if (response.success()) {
                MessageDialogs.showInfo(this, "Section dropped.");
                refreshAll();
            } else {
                response.error().map(ApiResponse.ErrorInfo::message)
                        .ifPresent(msg -> MessageDialogs.showError(this, msg));
            }
        } catch (Exception ex) {
            MessageDialogs.showError(this, "Drop failed: " + ex.getMessage());
        }
    }

    private void downloadTranscript() {

        String[] options = {"CSV", "PDF"};
        int choice = javax.swing.JOptionPane.showOptionDialog(this,
                "Choose transcript format:",
                "Download Transcript",
                javax.swing.JOptionPane.DEFAULT_OPTION,
                javax.swing.JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[1]);

        if (choice == -1) return;

        try {
            byte[] data;
            String extension;

            if (choice == 0) {

                data = studentService.exportTranscriptCsv(user);
                extension = ".csv";
            } else {

                data = studentService.exportTranscriptPdf(user);
                extension = ".pdf";
            }

            if (data.length == 0) {
                MessageDialogs.showInfo(this, "No completed courses available for transcript.");
                return;
            }

            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new java.io.File("transcript_" + user.username() + extension));
            int result = chooser.showSaveDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                Path path = chooser.getSelectedFile().toPath();
                Files.write(path, data);
                MessageDialogs.showInfo(this, "Transcript saved to " + path);
            }
        } catch (IOException ex) {
            MessageDialogs.showError(this, "Failed to save transcript: " + ex.getMessage());
        } catch (Exception ex) {
            MessageDialogs.showError(this, "Could not generate transcript: " + ex.getMessage());
        }
    }
}
