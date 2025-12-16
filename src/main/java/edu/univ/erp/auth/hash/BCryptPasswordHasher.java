package edu.univ.erp.auth.hash;

import org.mindrot.jbcrypt.BCrypt;

public class BCryptPasswordHasher implements PasswordHasher {

    private final int logRounds;

    public BCryptPasswordHasher() {
        this(12);
    }

    public BCryptPasswordHasher(int logRounds) {
        if (logRounds < 4 || logRounds > 31) {
            throw new IllegalArgumentException("logRounds must be between 4 and 31");
        }
        this.logRounds = logRounds;
    }

    @Override
    public String hash(char[] password) {
        String salt = BCrypt.gensalt(logRounds);
        return BCrypt.hashpw(new String(password), salt);
    }

    @Override
    public boolean verify(char[] password, String hash) {
        if (hash == null || hash.isBlank()) {
            return false;
        }
        return BCrypt.checkpw(new String(password), hash);
    }
}

