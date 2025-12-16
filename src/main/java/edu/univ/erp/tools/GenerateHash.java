package edu.univ.erp.tools;

import edu.univ.erp.auth.hash.BCryptPasswordHasher;
import edu.univ.erp.auth.hash.PasswordHasher;

public class GenerateHash {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java GenerateHash <password>");
            System.exit(1);
        }

        PasswordHasher hasher = new BCryptPasswordHasher();
        String password = args[0];
        String hash = hasher.hash(password.toCharArray());

        System.out.println("Password: " + password);
        System.out.println("Hash: " + hash);
    }
}
