package edu.univ.erp.data.auth;

import java.util.List;
import java.util.Optional;

import edu.univ.erp.domain.UserRole;

public interface AuthUserDao {

    Optional<AuthUserRecord> findByUsername(String username);

    void updateLastLogin(long userId);

    void recordFailedAttempt(String username);

    void resetFailedAttempts(long userId);

    long createUser(String username, UserRole role, String passwordHash, boolean active);

    List<AuthUserRecord> listAllUsers();

    void deleteUser(long userId);

    void updatePassword(long userId, String newPasswordHash);

    int getFailedAttemptCount(String username);
}
