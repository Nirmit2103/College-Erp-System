package edu.univ.erp.auth.impl;

import edu.univ.erp.auth.AuthService;
import edu.univ.erp.auth.AuthenticatedUser;
import edu.univ.erp.auth.LoginRequest;
import edu.univ.erp.auth.LoginResult;
import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.auth.hash.PasswordHasher;
import edu.univ.erp.data.auth.AuthUserDao;
import edu.univ.erp.data.auth.AuthUserRecord;
import edu.univ.erp.domain.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class DatabaseAuthService implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(DatabaseAuthService.class);

    private final AuthUserDao authUserDao;
    private final PasswordHasher passwordHasher;
    private final SessionManager sessionManager;
    private final MaintenanceFlagProvider maintenanceFlagProvider;

    public interface MaintenanceFlagProvider {
        boolean isMaintenanceOn();
    }

    public DatabaseAuthService(AuthUserDao authUserDao,
                               PasswordHasher passwordHasher,
                               SessionManager sessionManager,
                               MaintenanceFlagProvider maintenanceFlagProvider) {
        this.authUserDao = authUserDao;
        this.passwordHasher = passwordHasher;
        this.sessionManager = sessionManager;
        this.maintenanceFlagProvider = maintenanceFlagProvider;
    }

    @Override
    public LoginResult login(LoginRequest request) {
        Optional<AuthUserRecord> recordOpt = authUserDao.findByUsername(request.username());
        if (recordOpt.isEmpty()) {
            log.warn("Login failed for unknown user {}", request.username());
            return LoginResult.failure("Incorrect username or password.");
        }

        AuthUserRecord record = recordOpt.get();
        if (!record.active()) {
            return LoginResult.failure("Account is disabled. Contact administrator.");
        }

        boolean verified = passwordHasher.verify(request.password(), record.passwordHash());
        if (!verified) {
            authUserDao.recordFailedAttempt(request.username());
            return LoginResult.failure("Incorrect username or password.");
        }

        authUserDao.resetFailedAttempts(record.userId());
        authUserDao.updateLastLogin(record.userId());

        boolean maintenanceOn = maintenanceFlagProvider.isMaintenanceOn();
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                record.userId(),
                record.username(),
                record.role() == null ? UserRole.STUDENT : record.role(),
                maintenanceOn
        );
        sessionManager.setCurrentUser(authenticatedUser);
        log.info("User {} logged in successfully as {}", record.username(), record.role());
        return LoginResult.success(authenticatedUser);
    }

    @Override
    public void logout() {
        sessionManager.clear();
    }

    @Override
    public Optional<AuthenticatedUser> currentUser() {
        return sessionManager.getCurrentUser();
    }

    @Override
    public boolean changePassword(String currentPassword, String newPassword) {
        Optional<AuthenticatedUser> userOpt = currentUser();
        if (userOpt.isEmpty()) {
            return false;
        }

        AuthenticatedUser user = userOpt.get();
        Optional<AuthUserRecord> recordOpt = authUserDao.findByUsername(user.username());
        if (recordOpt.isEmpty()) {
            return false;
        }

        AuthUserRecord record = recordOpt.get();

        boolean verified = passwordHasher.verify(currentPassword.toCharArray(), record.passwordHash());
        if (!verified) {
            return false;
        }

        String newHash = passwordHasher.hash(newPassword.toCharArray());
        authUserDao.updatePassword(record.userId(), newHash);
        log.info("Password changed for user {}", user.username());
        return true;
    }
}

