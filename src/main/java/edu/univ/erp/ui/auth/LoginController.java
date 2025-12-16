package edu.univ.erp.ui.auth;

import edu.univ.erp.AppBootstrap;
import edu.univ.erp.auth.AuthenticatedUser;
import edu.univ.erp.auth.LoginRequest;
import edu.univ.erp.auth.LoginResult;
import edu.univ.erp.ui.dashboard.AdminDashboard;
import edu.univ.erp.ui.dashboard.InstructorDashboard;
import edu.univ.erp.ui.dashboard.StudentDashboard;
import edu.univ.erp.util.MessageDialogs;

import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class LoginController {

    private final AppBootstrap bootstrap;
    private final LoginFrame frame;

    public LoginController(AppBootstrap bootstrap, LoginFrame frame) {
        this.bootstrap = bootstrap;
        this.frame = frame;
        init();
    }

    private void init() {
        frame.setOnLogin(e -> doLogin());

        frame.getUsernameField().getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                checkFailedAttempts();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                checkFailedAttempts();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                checkFailedAttempts();
            }
        });

        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                frame.initializeWarningCheck();
            }
        });
    }

    private void checkFailedAttempts() {
        String username = frame.getUsername();
        if (username.isEmpty()) {
            frame.hideFailedAttempts();
            return;
        }

        int attemptCount = bootstrap.authUserDao().getFailedAttemptCount(username);
        frame.showFailedAttempts(attemptCount);
    }

    private void doLogin() {
        String username = frame.getUsername();
        char[] password = frame.getPassword();
        try {
            LoginResult result = bootstrap.authService().login(new LoginRequest(username, password));
            if (result.success()) {
                frame.clearPassword();
                frame.dispose();
                result.user().ifPresent(this::openDashboard);
            } else {
                MessageDialogs.showError(frame, result.message());
            }
        } finally {
            java.util.Arrays.fill(password, '\0');
        }
    }

    private void openDashboard(AuthenticatedUser user) {
        SwingUtilities.invokeLater(() -> {
            switch (user.role()) {
                case STUDENT -> new StudentDashboard(user, bootstrap.maintenanceGuard(), bootstrap.studentService(), bootstrap).setVisible(true);
                case INSTRUCTOR -> new InstructorDashboard(user, bootstrap.maintenanceGuard(), bootstrap.instructorService(), bootstrap).setVisible(true);
                case ADMIN -> new AdminDashboard(user, bootstrap.maintenanceService(), bootstrap.maintenanceGuard(), bootstrap.adminService(), bootstrap).setVisible(true);
                default -> MessageDialogs.showError(frame, "Unknown role: " + user.role());
            }
        });
    }
}

