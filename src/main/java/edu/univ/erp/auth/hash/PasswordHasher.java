package edu.univ.erp.auth.hash;

public interface PasswordHasher {

    String hash(char[] password);

    boolean verify(char[] password, String hash);
}

