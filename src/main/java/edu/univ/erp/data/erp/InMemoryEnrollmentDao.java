package edu.univ.erp.data.erp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import edu.univ.erp.domain.Enrollment;
import edu.univ.erp.domain.EnrollmentStatus;

public class InMemoryEnrollmentDao implements EnrollmentDao {

    private final CopyOnWriteArrayList<Enrollment> enrollments = new CopyOnWriteArrayList<>();
    private final AtomicLong idSequence = new AtomicLong(1);
    private final Path enrollmentsFile = Paths.get("data", "enrollments.txt");

    public InMemoryEnrollmentDao() {
        if (!loadFromFile()) {
            seed();
            try { writeAllToFile(); } catch (IOException ignored) {}
        }
    }

    private void seed() {

        enrollments.add(new Enrollment(idSequence.getAndIncrement(), 3, 1, EnrollmentStatus.REGISTERED));
        enrollments.add(new Enrollment(idSequence.getAndIncrement(), 4, 2, EnrollmentStatus.REGISTERED));
    }

    private boolean loadFromFile() {
        try {
            if (!Files.exists(enrollmentsFile)) return false;
            List<String> lines = Files.readAllLines(enrollmentsFile, StandardCharsets.UTF_8);
            long maxId = 0;
            for (String line : lines) {
                String[] parts = line.split("\\|\\|");
                if (parts.length >= 4) {
                    try {
                        long id = Long.parseLong(parts[0]);
                        long studentId = Long.parseLong(parts[1]);
                        long sectionId = Long.parseLong(parts[2]);
                        EnrollmentStatus status = EnrollmentStatus.valueOf(parts[3]);
                        enrollments.add(new Enrollment(id, studentId, sectionId, status));
                        if (id > maxId) maxId = id;
                    } catch (Exception ignored) {}
                }
            }
            idSequence.set(maxId + 1);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private void writeAllToFile() throws IOException {
        if (enrollmentsFile.getParent() != null) Files.createDirectories(enrollmentsFile.getParent());
        List<String> lines = enrollments.stream()
                .map(e -> String.join("||",
                        Long.toString(e.id()),
                        Long.toString(e.studentId()),
                        Long.toString(e.sectionId()),
                        e.status().name()))
                .collect(Collectors.toList());
        Files.write(enrollmentsFile, lines, StandardCharsets.UTF_8);
    }

    @Override
    public Optional<Enrollment> findByStudentAndSection(long studentUserId, long sectionId) {
        return enrollments.stream()
                .filter(e -> e.studentId() == studentUserId && e.sectionId() == sectionId)
                .findFirst();
    }

    @Override
    public long countActiveEnrollments(long sectionId) {
        return enrollments.stream()
                .filter(e -> e.sectionId() == sectionId && e.status() == EnrollmentStatus.REGISTERED)
                .count();
    }

    @Override
    public Enrollment save(Enrollment enrollment) {
        Enrollment saved = new Enrollment(idSequence.getAndIncrement(),
            enrollment.studentId(),
            enrollment.sectionId(),
            enrollment.status());
        enrollments.add(saved);
        try { writeAllToFile(); } catch (IOException ignored) {}
        return saved;
    }

    @Override
    public void updateStatus(long enrollmentId, EnrollmentStatus status) {
        enrollments.replaceAll(e -> e.id() == enrollmentId
                ? new Enrollment(e.id(), e.studentId(), e.sectionId(), status)
                : e);
        try { writeAllToFile(); } catch (IOException ignored) {}
    }

    @Override
    public List<Enrollment> listByStudent(long studentUserId) {
        return enrollments.stream()
                .filter(e -> e.studentId() == studentUserId && e.status() == EnrollmentStatus.REGISTERED)
                .collect(Collectors.toList());
    }

    @Override
    public List<Enrollment> listBySection(long sectionId) {
        return enrollments.stream()
                .filter(e -> e.sectionId() == sectionId && e.status() == EnrollmentStatus.REGISTERED)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByStudentId(long studentUserId) {
        enrollments.removeIf(e -> e.studentId() == studentUserId);
        try { writeAllToFile(); } catch (IOException ignored) {}
    }

    @Override
    public void deleteById(long enrollmentId) {
        enrollments.removeIf(e -> e.id() == enrollmentId);
        try { writeAllToFile(); } catch (IOException ignored) {}
    }
}

