package edu.univ.erp.data.erp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import edu.univ.erp.domain.FinalGrade;
import edu.univ.erp.domain.GradeEntry;
import edu.univ.erp.util.GradeUpdateNotifier;

public class InMemoryGradeDao implements GradeDao {

    private final Map<Long, Map<Long, GradeEntry>> gradeEntries = new ConcurrentHashMap<>();
    private final Map<Long, FinalGrade> finalGrades = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(1);

    private final Path gradesFile = Paths.get("data", "grades.txt");
    private final Path finalsFile = Paths.get("data", "final_grades.txt");

    public InMemoryGradeDao() {
        if (!loadFromFiles()) {
            seed();

            try { writeAllToFiles(); } catch (IOException ignored) {}
        }
    }

    private boolean loadFromFiles() {
        try {
            if (Files.exists(gradesFile)) {
                List<String> lines = Files.readAllLines(gradesFile, StandardCharsets.UTF_8);
                long maxId = 0;
                for (String line : lines) {

                    String[] parts = line.split("\\|\\|");
                    if (parts.length >= 4) {
                        try {
                            long entryId = Long.parseLong(parts[0]);
                            long enrollmentId = Long.parseLong(parts[1]);
                            long componentId = Long.parseLong(parts[2]);
                            double score = Double.parseDouble(parts[3]);
                            gradeEntries.computeIfAbsent(enrollmentId, id -> new ConcurrentHashMap<>());
                            gradeEntries.get(enrollmentId).put(componentId, new GradeEntry(entryId, enrollmentId, componentId, score));
                            if (entryId > maxId) maxId = entryId;
                        } catch (NumberFormatException ignored) {}
                    }
                }
                idSequence.set(maxId + 1);
            }
            if (Files.exists(finalsFile)) {
                List<String> lines = Files.readAllLines(finalsFile, StandardCharsets.UTF_8);
                for (String line : lines) {

                    String[] parts = line.split("\\|\\|");
                    if (parts.length >= 3) {
                        try {
                            long enrollmentId = Long.parseLong(parts[0]);
                            double percentage = Double.parseDouble(parts[1]);
                            String letter = parts[2];
                            finalGrades.put(enrollmentId, new FinalGrade(enrollmentId, percentage, letter));
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }
            return Files.exists(gradesFile) || Files.exists(finalsFile);
        } catch (IOException e) {
            return false;
        }
    }

    private void seed() {

        upsertScore(1L, 1L, 92.5);
        upsertScore(1L, 2L, 83.3333333);
        upsertScore(1L, 3L, 90.0);

        upsertScore(2L, 1L, 80.0);
        upsertScore(2L, 2L, 66.6666667);
        upsertScore(2L, 3L, 80.0);
    }

    @Override
    public List<GradeEntry> listByEnrollment(long enrollmentId) {
        return List.copyOf(gradeEntries.getOrDefault(enrollmentId, Map.of()).values());
    }

    @Override
    public FinalGrade findFinalGrade(long enrollmentId) {
        return finalGrades.get(enrollmentId);
    }

    @Override
    public void upsertScore(long enrollmentId, long componentId, double score) {
        gradeEntries.computeIfAbsent(enrollmentId, id -> new ConcurrentHashMap<>());
        gradeEntries.compute(enrollmentId, (id, map) -> {
            Map<Long, GradeEntry> entries = map == null ? new ConcurrentHashMap<>() : new ConcurrentHashMap<>(map);
            GradeEntry existing = entries.get(componentId);
            if (existing != null) {
                entries.put(componentId, new GradeEntry(existing.id(), enrollmentId, componentId, score));
            } else {
                entries.put(componentId, new GradeEntry(idSequence.getAndIncrement(), enrollmentId, componentId, score));
            }
            return entries;
        });

        try { writeAllToFiles(); } catch (IOException ignored) {}

        GradeUpdateNotifier.notifyGradeUpdated(enrollmentId);
    }

    @Override
    public void deleteScoresForComponent(long sectionId, long componentId) {
        gradeEntries.replaceAll((enrollmentId, entries) -> {
            Map<Long, GradeEntry> updated = new ConcurrentHashMap<>(entries);
            updated.remove(componentId);
            return updated;
        });
        try { writeAllToFiles(); } catch (IOException ignored) {}
    }

    @Override
    public void upsertFinalGrade(FinalGrade finalGrade) {
        finalGrades.put(finalGrade.enrollmentId(), finalGrade);
        try { writeAllToFiles(); } catch (IOException ignored) {}
        GradeUpdateNotifier.notifyGradeUpdated(finalGrade.enrollmentId());
    }

    @Override
    public void deleteByEnrollmentId(long enrollmentId) {
        gradeEntries.remove(enrollmentId);
        finalGrades.remove(enrollmentId);
        try { writeAllToFiles(); } catch (IOException ignored) {}
    }

    private void writeAllToFiles() throws IOException {
        if (gradesFile.getParent() != null) Files.createDirectories(gradesFile.getParent());
        List<String> gradeLines = new ArrayList<>();
        for (Map.Entry<Long, Map<Long, GradeEntry>> e : gradeEntries.entrySet()) {
            for (GradeEntry entry : e.getValue().values()) {
                gradeLines.add(String.join("||",
                        Long.toString(entry.id()),
                        Long.toString(entry.enrollmentId()),
                        Long.toString(entry.componentId()),
                        Double.toString(entry.score())));
            }
        }
        Files.write(gradesFile, gradeLines, StandardCharsets.UTF_8);

        List<String> finalLines = finalGrades.values().stream()
                .map(f -> String.join("||",
                        Long.toString(f.enrollmentId()),
                        Double.toString(f.percentage()),
                        f.letterGrade() == null ? "" : f.letterGrade()))
                .collect(Collectors.toList());
        Files.write(finalsFile, finalLines, StandardCharsets.UTF_8);
    }
}

