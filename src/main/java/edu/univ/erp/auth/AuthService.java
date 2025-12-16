package edu.univ.erp.auth;

import java.util.Optional;

public interface AuthService {

    LoginResult login(LoginRequest request);

    void logout();

    Optional<AuthenticatedUser> currentUser();

    boolean changePassword(String currentPassword, String newPassword);
}
