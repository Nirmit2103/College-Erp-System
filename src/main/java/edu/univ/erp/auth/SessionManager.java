package edu.univ.erp.auth;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class SessionManager {

    private final AtomicReference<AuthenticatedUser> currentUser = new AtomicReference<>();

    public void setCurrentUser(AuthenticatedUser user) {
        currentUser.set(user);
    }

    public void clear() {
        currentUser.set(null);
    }

    public Optional<AuthenticatedUser> getCurrentUser() {
        return Optional.ofNullable(currentUser.get());
    }
}

