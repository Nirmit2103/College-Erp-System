package edu.univ.erp.ui.dashboard;

import edu.univ.erp.access.MaintenanceModeGuard;
import edu.univ.erp.auth.AuthenticatedUser;
import edu.univ.erp.api.common.ApiResponse;
import edu.univ.erp.domain.AssessmentComponent;
import edu.univ.erp.domain.Section;
import edu.univ.erp.service.instructor.InstructorService;
import edu.univ.erp.service.instructor.dto.GradebookRow;
import edu.univ.erp.service.instructor.dto.SectionStats;
import edu.univ.erp.ui.common.MaintenanceBanner;
import edu.univ.erp.ui.common.PrimaryButton;
import edu.univ.erp.util.MessageDialogs;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;

public class InstructorDashboard extends JFrame {

    private final AuthenticatedUser user;
    private final MaintenanceModeGuard maintenanceGuard;
    private final InstructorService instructorService;
    private final edu.univ.erp.AppBootstrap bootstrap;

    private MaintenanceBanner maintenanceBanner;
    private JComboBox<Section> sectionComboBox;
    private final GradebookTableModel gradebookTableModel = new GradebookTableModel();
    private JTable gradebookTable;
    private boolean suppressComboEvents = false;

    public InstructorDashboard(AuthenticatedUser user,
                               MaintenanceModeGuard maintenanceGuard,
                               InstructorService instructorService,
                               edu.univ.erp.AppBootstrap bootstrap) {
        this.user = user;
        this.maintenanceGuard = maintenanceGuard;
        this.instructorService = instructorService;
        this.bootstrap = bootstrap;
        setTitle("Instructor Dashboard");
        setSize(1024, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        buildUi();
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                if (maintenanceBanner != null) {
                    maintenanceBanner.detach();
                }
            }
        });
        loadSections();
    }

    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout(0, 0));

        JPanel header = buildHeader();
        root.add(header, BorderLayout.NORTH);

        JPanel mainContent = new JPanel(new BorderLayout(0, 20));
        mainContent.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainContent.setBackground(new java.awt.Color(0xF5F5F5));

        maintenanceBanner = new MaintenanceBanner(maintenanceGuard);
        mainContent.add(maintenanceBanner, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 12));
        centerPanel.setOpaque(false);
        centerPanel.add(buildSectionSelector(), BorderLayout.NORTH);
        centerPanel.add(buildGradebookTable(), BorderLayout.CENTER);
        mainContent.add(centerPanel, BorderLayout.CENTER);
        mainContent.add(buildActionBar(), BorderLayout.SOUTH);

        root.add(mainContent, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new java.awt.Color(0x3F51B5));
        header.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel leftPanel = new JPanel(new BorderLayout(0, 5));
        leftPanel.setOpaque(false);

        JLabel welcomeLabel = new JLabel("Instructor Dashboard - " + user.username());
        welcomeLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 18));
        welcomeLabel.setForeground(java.awt.Color.WHITE);
        leftPanel.add(welcomeLabel, BorderLayout.NORTH);

        JLabel infoLabel = new JLabel("Manage your sections and student grades");
        infoLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
        infoLabel.setForeground(new java.awt.Color(255, 255, 255, 200));
        leftPanel.add(infoLabel, BorderLayout.SOUTH);

        header.add(leftPanel, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);

        edu.univ.erp.ui.common.PrimaryButton changePasswordBtn = new edu.univ.erp.ui.common.PrimaryButton("Change Password");
        changePasswordBtn.addActionListener(e -> openChangePasswordDialog());
        rightPanel.add(changePasswordBtn);

        edu.univ.erp.ui.common.PrimaryButton logoutButton = new edu.univ.erp.ui.common.PrimaryButton("Logout");
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

    private Component buildSectionSelector() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.add(new JLabel("My Sections"), BorderLayout.WEST);
        sectionComboBox = new JComboBox<>();
        sectionComboBox.setRenderer(new SectionRenderer());
        sectionComboBox.addActionListener(e -> {
            if (!suppressComboEvents) {
                refreshGradebook();
            }
        });
        panel.add(sectionComboBox, BorderLayout.CENTER);
        return panel;
    }

    private Component buildGradebookTable() {
        gradebookTable = new JTable(gradebookTableModel);
        gradebookTable.setAutoCreateRowSorter(true);
        gradebookTable.setFillsViewportHeight(true);
        gradebookTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        gradebookTable.setRowHeight(28);

        gradebookTable.setSurrendersFocusOnKeystroke(true);
        gradebookTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        gradebookTableModel.setScoreUpdater((enrollmentId, componentId, score) -> {
            Section section = getSelectedSection();
            if (section == null) return;
            var response = instructorService.recordScore(user, section.id(), enrollmentId, componentId, score);
            if (response.success()) {
                SwingUtilities.invokeLater(this::refreshGradebook);
            } else {
                response.error().map(ApiResponse.ErrorInfo::message)
                        .ifPresent(msg -> SwingUtilities.invokeLater(() -> MessageDialogs.showError(this, msg)));
            }
        });
        return new JScrollPane(gradebookTable);
    }

    private Component buildActionBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));

        PrimaryButton refreshButton = new PrimaryButton("Refresh");
        refreshButton.addActionListener(e -> refreshGradebook());

        PrimaryButton saveStudentButton = new PrimaryButton("Save Selected Student Grades");
        saveStudentButton.addActionListener(e -> saveSelectedStudentGrades());

        PrimaryButton computeFinalsButton = new PrimaryButton("Compute Final Grades");
        computeFinalsButton.addActionListener(e -> computeFinalGrades());

        PrimaryButton statsButton = new PrimaryButton("View Stats");
        statsButton.addActionListener(e -> showStats());

        panel.add(refreshButton);
        panel.add(saveStudentButton);
        panel.add(computeFinalsButton);
        panel.add(statsButton);
        return panel;
    }

    private void saveSelectedStudentGrades() {

        if (gradebookTable.isEditing()) {
            var editor = gradebookTable.getCellEditor();
            if (editor != null) editor.stopCellEditing();
        }

        int viewRow = gradebookTable.getSelectedRow();
        if (viewRow < 0) {
            MessageDialogs.showInfo(this, "Select a student row to save grades.");
            return;
        }
        int modelRow = gradebookTable.convertRowIndexToModel(viewRow);
        GradebookRow row = gradebookTableModel.getRow(modelRow);
        Section section = getSelectedSection();
        if (section == null) {
            MessageDialogs.showInfo(this, "Select a section first.");
            return;
        }

        boolean anyFailed = false;
        String firstErrorMessage = null;

        for (int col = 1; col <= gradebookTableModel.getColumnCount() - 3; col++) {
            var component = gradebookTableModel.getComponentForColumn(col);
            if (component == null) continue;
            Object val = gradebookTableModel.getValueAt(modelRow, col);
            if (val == null) continue;
            Double score = null;
            if (val instanceof Number) score = ((Number) val).doubleValue();
            else {
                try { score = Double.parseDouble(String.valueOf(val)); } catch (NumberFormatException ignored) {}
            }
            if (score == null) continue;

            var response = instructorService.recordScore(user, section.id(), row.enrollmentId(), component.id(), score);
            if (!response.success()) {
                anyFailed = true;
                if (firstErrorMessage == null) {
                    firstErrorMessage = response.error().map(ApiResponse.ErrorInfo::message).orElse("Unknown error");
                }
            }
        }

        if (anyFailed) {
            MessageDialogs.showError(this, firstErrorMessage != null ? firstErrorMessage : "Some grades could not be saved.");
        } else {
            MessageDialogs.showInfo(this, "Grades saved.");
        }

        refreshGradebook();
    }

    private void loadSections() {
        try {
            List<Section> sections = instructorService.mySections(user);
            suppressComboEvents = true;
            sectionComboBox.setModel(new DefaultComboBoxModel<>(sections.toArray(new Section[0])));
            if (!sections.isEmpty()) {
                sectionComboBox.setSelectedIndex(0);
            }
        } catch (Exception ex) {
            MessageDialogs.showError(this, "Failed to load sections: " + ex.getMessage());
        } finally {
            suppressComboEvents = false;
            refreshGradebook();
        }
    }

    private void refreshGradebook() {
        Section section = (Section) sectionComboBox.getSelectedItem();
        if (section == null) {
            gradebookTableModel.setData(List.of(), List.of());
            return;
        }
        try {
            List<AssessmentComponent> components = instructorService.listAssessments(user, section.id());
            List<GradebookRow> rows = instructorService.gradebook(user, section.id());
            gradebookTableModel.setData(components, rows);
            adjustTableColumns();
        } catch (Exception ex) {
            MessageDialogs.showError(this, "Failed to load gradebook: " + ex.getMessage());
        }
    }

    private void adjustTableColumns() {
        for (int column = 0; column < gradebookTable.getColumnCount(); column++) {
            gradebookTable.getColumnModel().getColumn(column).setPreferredWidth(140);
        }
    }

    private void addAssessment() {
        Section section = getSelectedSection();
        if (section == null) {
            MessageDialogs.showInfo(this, "Select a section first.");
            return;
        }
        String name = JOptionPane.showInputDialog(this, "Assessment name:", "New Assessment", JOptionPane.PLAIN_MESSAGE);
        if (name == null) {
            return;
        }
        String weightStr = JOptionPane.showInputDialog(this, "Weight (%) (0-100):", "20");
        if (weightStr == null) {
            return;
        }
        try {
            double weight = Double.parseDouble(weightStr);
            var response = instructorService.defineAssessment(user, section.id(), name, weight);
            if (response.success()) {
                MessageDialogs.showInfo(this, "Assessment added.");
                refreshGradebook();
            } else {
                response.error().map(ApiResponse.ErrorInfo::message)
                        .ifPresent(msg -> MessageDialogs.showError(this, msg));
            }
        } catch (NumberFormatException ex) {
            MessageDialogs.showError(this, "Enter a valid numeric weight.");
        }
    }

    private void recordScore() {
        Section section = getSelectedSection();
        if (section == null) {
            MessageDialogs.showInfo(this, "Select a section first.");
            return;
        }
        int viewRow = gradebookTable.getSelectedRow();
        int viewColumn = gradebookTable.getSelectedColumn();
        if (viewRow < 0 || viewColumn < 0) {
            MessageDialogs.showInfo(this, "Select a student and assessment column.");
            return;
        }
        int modelRow = gradebookTable.convertRowIndexToModel(viewRow);
        int modelColumn = gradebookTable.convertColumnIndexToModel(viewColumn);

        AssessmentComponent component = gradebookTableModel.getComponentForColumn(modelColumn);
        if (component == null) {
            MessageDialogs.showInfo(this, "Select an assessment column to record score.");
            return;
        }
        GradebookRow row = gradebookTableModel.getRow(modelRow);
        String scoreStr = JOptionPane.showInputDialog(this, "Enter score (0-100):", "");
        if (scoreStr == null) {
            return;
        }
        try {
            double score = Double.parseDouble(scoreStr);
            var response = instructorService.recordScore(user, section.id(), row.enrollmentId(), component.id(), score);
            if (response.success()) {
                MessageDialogs.showInfo(this, "Score saved.");
                refreshGradebook();
            } else {
                response.error().map(ApiResponse.ErrorInfo::message)
                        .ifPresent(msg -> MessageDialogs.showError(this, msg));
            }
        } catch (NumberFormatException ex) {
            MessageDialogs.showError(this, "Enter a valid numeric score.");
        }
    }

    private void computeFinalGrades() {
        Section section = getSelectedSection();
        if (section == null) {
            MessageDialogs.showInfo(this, "Select a section first.");
            return;
        }
        var response = instructorService.computeFinalGrades(user, section.id());
        if (response.success()) {
            MessageDialogs.showInfo(this, "Final grades computed.");
            refreshGradebook();
        } else {
            response.error().map(ApiResponse.ErrorInfo::message)
                    .ifPresent(msg -> MessageDialogs.showError(this, msg));
        }
    }

    private void showStats() {
        Section section = getSelectedSection();
        if (section == null) {
            MessageDialogs.showInfo(this, "Select a section first.");
            return;
        }
        try {
            SectionStats stats = instructorService.stats(user, section.id());
            if (stats.enrollmentCount() == 0) {
                MessageDialogs.showInfo(this, "No enrollments available for statistics.");
                return;
            }
            if (stats.average() == null) {
                MessageDialogs.showInfo(this, "Compute final grades to view statistics.");
                return;
            }
            String message = """
                    Enrolled: %d
                    Average: %.2f
                    Highest: %.2f
                    Lowest: %.2f
                    """.formatted(
                    stats.enrollmentCount(),
                    stats.average(),
                    stats.highest(),
                    stats.lowest()
            );
            MessageDialogs.showInfo(this, message);
        } catch (Exception ex) {
            MessageDialogs.showError(this, "Failed to compute stats: " + ex.getMessage());
        }
    }

    private Section getSelectedSection() {
        return (Section) sectionComboBox.getSelectedItem();
    }

    private static class SectionRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Section section) {
                setText("Section " + section.id() + " • " + section.semester() + " " + section.year());
            }
            return this;
        }
    }
}
