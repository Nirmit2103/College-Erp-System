package edu.univ.erp.auth;

import java.util.Optional;

public final class LoginResult {

    private final boolean success;
    private final AuthenticatedUser user;
    private final String message;

    private LoginResult(boolean success, AuthenticatedUser user, String message) {
        this.success = success;
        this.user = user;
        this.message = message;
    }

    public static LoginResult success(AuthenticatedUser user) {
        return new LoginResult(true, user, "Login successful");
    }

    public static LoginResult failure(String message) {
        return new LoginResult(false, null, message);
    }

    public boolean success() {
        return success;
    }

    public Optional<AuthenticatedUser> user() {
        return Optional.ofNullable(user);
    }

    public String message() {
        return message;
    }
}

