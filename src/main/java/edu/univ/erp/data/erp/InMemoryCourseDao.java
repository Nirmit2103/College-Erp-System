package edu.univ.erp.data.erp;

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

import edu.univ.erp.domain.Course;

public class InMemoryCourseDao implements CourseDao {

    private final Map<Long, Course> courses = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(101);
    private final Path storageFile = Paths.get("data", "courses.txt");

    public InMemoryCourseDao() {
        if (!loadFromFile()) {
            seed();
        }
    }

    private void seed() {
        courses.put(101L, new Course(101, "CS101", "Introduction to Programming", 4, null));
        courses.put(102L, new Course(102, "MA102", "Linear Algebra", 3, null));
        courses.put(103L, new Course(103, "HS201", "Modern History", 2, null));
        saveToFile();
    }

    @Override
    public Optional<Course> findById(long id) {
        return Optional.ofNullable(courses.get(id));
    }

    @Override
    public List<Course> listAll() {
        return new ArrayList<>(courses.values());
    }

    @Override
    public Course create(String code, String title, int credits) {
        long id = idSequence.getAndIncrement();
        Course course = new Course(id, code, title, credits, null);
        courses.put(id, course);
        saveToFile();
        return course;
    }

    @Override
    public void deleteById(long courseId) {
        courses.remove(courseId);
        saveToFile();
    }

    private boolean loadFromFile() {
        try {
            if (!Files.exists(storageFile)) {
                return false;
            }
            try (BufferedReader br = Files.newBufferedReader(storageFile)) {
                String line;
                long maxId = 0;
                while ((line = br.readLine()) != null) {

                    String[] parts = line.split("\\|\\|", -1);
                    if (parts.length < 4) continue;
                    long id = Long.parseLong(parts[0].trim());
                    String code = parts[1].trim();
                    String title = parts[2].trim();
                    int credits = Integer.parseInt(parts[3].trim());
                    String prerequisiteCode = parts.length > 4 && !parts[4].trim().isEmpty() ? parts[4].trim() : null;
                    Course c = new Course(id, code, title, credits, prerequisiteCode);
                    courses.put(id, c);
                    if (id > maxId) maxId = id;
                }
                idSequence.set(Math.max(101, maxId + 1));
            }
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    private void saveToFile() {
        try {
            if (!Files.exists(storageFile.getParent())) {
                Files.createDirectories(storageFile.getParent());
            }
            try (BufferedWriter bw = Files.newBufferedWriter(storageFile)) {
                for (Course c : courses.values()) {
                    String prereq = c.prerequisiteCode() != null ? c.prerequisiteCode() : "";
                    bw.write(String.format("%d||%s||%s||%d||%s", c.id(), c.code(), c.title(), c.credits(), prereq));
                    bw.newLine();
                }
            }
        } catch (IOException ex) {

        }
    }
}

