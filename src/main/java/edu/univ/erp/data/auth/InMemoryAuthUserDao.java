package edu.univ.erp.data.auth;

import edu.univ.erp.domain.UserRole;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

public class InMemoryAuthUserDao implements AuthUserDao {

    private final Map<String, AuthUserRecord> usersByUsername = new ConcurrentHashMap<>();
    private final Map<Long, String> usersById = new ConcurrentHashMap<>();
    private final Map<String, Integer> failedAttempts = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(1);
    private final Path storageFile = Paths.get("data", "auth_users.txt");

    public InMemoryAuthUserDao() {
        loadFromFile();
    }

    public long addUser(String username, UserRole role, String passwordHash, boolean active) {
        return createUser(username, role, passwordHash, active);
    }

    @Override
    public long createUser(String username, UserRole role, String passwordHash, boolean active) {
        if (usersByUsername.containsKey(username.toLowerCase())) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }
        long id = idSequence.getAndIncrement();
        String hashStr = passwordHash;
        AuthUserRecord record = new AuthUserRecord(id, username, role, hashStr, active, null);
        usersByUsername.put(username.toLowerCase(), record);
        usersById.put(id, username.toLowerCase());
        saveToFile();
        return id;
    }

    @Override
    public Optional<AuthUserRecord> findByUsername(String username) {
        return Optional.ofNullable(usersByUsername.get(username.toLowerCase()));
    }

    @Override
    public void updateLastLogin(long userId) {
        usersByUsername.replaceAll((key, value) -> value.userId() == userId
                ? new AuthUserRecord(value.userId(), value.username(), value.role(), value.passwordHash(), value.active(), OffsetDateTime.now())
                : value);
        saveToFile();
    }

    @Override
    public void recordFailedAttempt(String username) {
        failedAttempts.merge(username.toLowerCase(), 1, Integer::sum);
    }

    @Override
    public void resetFailedAttempts(long userId) {
        usersByUsername.values().stream()
                .filter(record -> record.userId() == userId)
                .findFirst()
                .ifPresent(record -> failedAttempts.remove(record.username().toLowerCase()));
    }

    @Override
    public int getFailedAttemptCount(String username) {
        return failedAttempts.getOrDefault(username.toLowerCase(), 0);
    }

    @Override
    public List<AuthUserRecord> listAllUsers() {
        return new ArrayList<>(usersByUsername.values());
    }

    @Override
    public void deleteUser(long userId) {
        String username = usersById.remove(userId);
        if (username != null) {
            usersByUsername.remove(username);
            failedAttempts.remove(username);
            saveToFile();
        }
    }

    @Override
    public void updatePassword(long userId, String newPasswordHash) {
        usersByUsername.replaceAll((key, value) -> value.userId() == userId
                ? new AuthUserRecord(value.userId(), value.username(), value.role(), newPasswordHash, value.active(), value.lastLogin())
                : value);
        saveToFile();
    }

    private void loadFromFile() {
        try {
            if (!Files.exists(storageFile)) {

                return;
            }
            try (BufferedReader br = Files.newBufferedReader(storageFile)) {
                String line;
                long maxId = 0;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split("\\|\\|", -1);
                    if (parts.length < 6) continue;
                    long id = Long.parseLong(parts[0].trim());
                    String username = parts[1].trim();
                    UserRole role = UserRole.valueOf(parts[2].trim());
                    String passwordHash = parts[3].trim();
                    boolean active = Boolean.parseBoolean(parts[4].trim());
                    OffsetDateTime lastLogin = null;
                    if (!parts[5].isEmpty()) {
                        try {
                            lastLogin = OffsetDateTime.parse(parts[5]);
                        } catch (DateTimeParseException ex) {
                            lastLogin = null;
                        }
                    }
                    AuthUserRecord record = new AuthUserRecord(id, username, role, passwordHash, active, lastLogin);
                    usersByUsername.put(username.toLowerCase(), record);
                    usersById.put(id, username.toLowerCase());
                    if (id > maxId) maxId = id;
                }
                idSequence.set(maxId + 1);
            }
        } catch (IOException e) {

        }
    }

    private void saveToFile() {
        try {
            if (!Files.exists(storageFile.getParent())) {
                Files.createDirectories(storageFile.getParent());
            }
            try (BufferedWriter bw = Files.newBufferedWriter(storageFile)) {
                for (AuthUserRecord r : usersByUsername.values()) {
                    String last = r.lastLogin() == null ? "" : r.lastLogin().toString();
                    bw.write(String.format("%d||%s||%s||%s||%b||%s", r.userId(), r.username(), r.role().name(), r.passwordHash(), r.active(), last));
                    bw.newLine();
                }
            }
        } catch (IOException e) {

        }
    }
}

