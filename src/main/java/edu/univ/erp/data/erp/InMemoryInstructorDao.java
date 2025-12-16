package edu.univ.erp.data.erp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import edu.univ.erp.domain.Instructor;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class InMemoryInstructorDao implements InstructorDao {

    private final Map<Long, Instructor> instructors = new ConcurrentHashMap<>();

    public InMemoryInstructorDao() {
        seed();
    }

    private void seed() {

        Path dataFile = Paths.get("data", "instructors.txt");
        if (Files.exists(dataFile)) {
            try {
                List<String> lines = Files.readAllLines(dataFile, StandardCharsets.UTF_8);
                for (String line : lines) {
                    String[] parts = line.split("\\|\\|");
                    if (parts.length >= 4) {
                        try {
                            long userId = Long.parseLong(parts[0]);
                            String fullName = parts[1];
                            String department = parts[2];
                            String title = parts[3];
                            instructors.put(userId, new Instructor(userId, fullName, department, title));
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
                return;
            } catch (IOException e) {

            }
        }

        instructors.put(2L, new Instructor(2, "Dr. Alice Kapoor", "Computer Science", "Associate Professor"));

        try {
            writeAllToFile(dataFile);
        } catch (IOException ignored) {
        }
    }

    @Override
    public Optional<Instructor> findByUserId(long userId) {
        return Optional.ofNullable(instructors.get(userId));
    }

    @Override
    public List<Instructor> listAll() {
        return new ArrayList<>(instructors.values());
    }

    @Override
    public Instructor create(long userId, String firstName, String lastName, String department) {
        if (instructors.containsKey(userId)) {
            throw new IllegalArgumentException("Instructor profile already exists for user: " + userId);
        }
        String fullName = firstName + " " + lastName;
        Instructor instructor = new Instructor(userId, fullName, department, "Assistant Professor");
        instructors.put(userId, instructor);

        try {
            writeAllToFile(Paths.get("data", "instructors.txt"));
        } catch (IOException ignored) {
        }
        return instructor;
    }

    @Override
    public boolean deleteByUserId(long userId) {
        boolean removed = instructors.remove(userId) != null;
        if (removed) {
            try {
                writeAllToFile(Paths.get("data", "instructors.txt"));
            } catch (IOException ignored) {
            }
        }
        return removed;
    }

    private void writeAllToFile(Path dataFile) throws IOException {

        if (dataFile.getParent() != null) {
            Files.createDirectories(dataFile.getParent());
        }
        List<String> lines = instructors.values().stream()
            .map(i -> String.join("||",
                Long.toString(i.userId()),
                i.fullName(),
                i.department(),
                i.designation() == null ? "" : i.designation()))
            .collect(Collectors.toList());
        Files.write(dataFile, lines, StandardCharsets.UTF_8);
    }
}

