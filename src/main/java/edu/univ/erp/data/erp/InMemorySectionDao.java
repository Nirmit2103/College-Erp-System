package edu.univ.erp.data.erp;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import edu.univ.erp.domain.Section;

public class InMemorySectionDao implements SectionDao {

    private final CopyOnWriteArrayList<Section> sections = new CopyOnWriteArrayList<>();
    private final AtomicLong idSequence = new AtomicLong(1);

    public InMemorySectionDao() {
        seed();
    }

    private void seed() {

        Path dataFile = Paths.get("data", "sections.txt");
        if (Files.exists(dataFile)) {
            try {
                List<String> lines = Files.readAllLines(dataFile, StandardCharsets.UTF_8);
                long maxId = 0;
                for (String line : lines) {
                    String[] parts = line.split("\\|\\|");
                    if (parts.length >= 10) {
                        try {
                            long id = Long.parseLong(parts[0]);
                            long courseId = Long.parseLong(parts[1]);
                            long instructorId = Long.parseLong(parts[2]);
                            DayOfWeek day = DayOfWeek.valueOf(parts[3]);
                            LocalTime start = LocalTime.parse(parts[4]);
                            LocalTime end = LocalTime.parse(parts[5]);
                            String room = parts[6];
                            int capacity = Integer.parseInt(parts[7]);
                            String semester = parts[8];

                            if (semester.equalsIgnoreCase("odd")) {
                                semester = "1";
                            } else if (semester.equalsIgnoreCase("even")) {
                                semester = "2";
                            }
                            int year = Integer.parseInt(parts[9]);
                            sections.add(new Section(id, courseId, instructorId, day, start, end, room, capacity, semester, year));
                            if (id > maxId) {
                                maxId = id;
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }
                idSequence.set(maxId + 1);
                return;
            } catch (IOException e) {

            }
        }

        add(101, 6, DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(10, 30),
            "Room A101", 40, "1", 2025);
        add(101, 6, DayOfWeek.WEDNESDAY, LocalTime.of(9, 0), LocalTime.of(10, 30),
            "Room A102", 40, "1", 2025);
        add(102, 6, DayOfWeek.TUESDAY, LocalTime.of(11, 0), LocalTime.of(12, 30),
            "Room B201", 35, "1", 2025);
        add(103, 6, DayOfWeek.THURSDAY, LocalTime.of(14, 0), LocalTime.of(15, 30),
            "Room C301", 50, "1", 2025);

        try {
            writeAllToFile(dataFile);
        } catch (IOException ignored) {
        }
    }

    private void add(long courseId,
                     long instructorId,
                     DayOfWeek day,
                     LocalTime start,
                     LocalTime end,
                     String room,
                     int capacity,
                     String semester,
                     int year) {
        sections.add(new Section(
                idSequence.getAndIncrement(),
                courseId,
                instructorId,
                day,
                start,
                end,
                room,
                capacity,
                semester,
                year
        ));
    }

    @Override
    public List<Section> listAll() {
        return Collections.unmodifiableList(new ArrayList<>(sections));
    }

    @Override
    public Optional<Section> findById(long id) {
        return sections.stream().filter(section -> section.id() == id).findFirst();
    }

    @Override
    public Section create(long courseId, long instructorId, DayOfWeek dayOfWeek,
                          LocalTime startTime, LocalTime endTime, String room,
                          int capacity, String semester, int year) {
        Section section = new Section(
                idSequence.getAndIncrement(),
                courseId,
                instructorId,
                dayOfWeek,
                startTime,
                endTime,
                room,
                capacity,
                semester,
                year
        );
        sections.add(section);

        try {
            writeAllToFile(Paths.get("data", "sections.txt"));
        } catch (IOException ignored) {
        }
        return section;
    }

    @Override
    public void updateInstructor(long sectionId, long instructorId) {
        for (int i = 0; i < sections.size(); i++) {
            Section section = sections.get(i);
            if (section.id() == sectionId) {
                Section updated = new Section(
                        section.id(),
                        section.courseId(),
                        instructorId,
                        section.dayOfWeek(),
                        section.startTime(),
                        section.endTime(),
                        section.room(),
                        section.capacity(),
                        section.semester(),
                        section.year()
                );
                sections.set(i, updated);

                try {
                    writeAllToFile(Paths.get("data", "sections.txt"));
                } catch (IOException ignored) {
                }
                return;
            }
        }
        throw new IllegalArgumentException("Section not found: " + sectionId);
    }

    @Override
    public void deleteById(long sectionId) {
        sections.removeIf(s -> s.id() == sectionId);
        try { writeAllToFile(Paths.get("data", "sections.txt")); } catch (IOException ignored) {}
    }

    private void writeAllToFile(Path dataFile) throws IOException {
        if (dataFile.getParent() != null) {
            Files.createDirectories(dataFile.getParent());
        }
        List<String> lines = sections.stream().map(s -> String.join("||",
                Long.toString(s.id()),
                Long.toString(s.courseId()),
                Long.toString(s.instructorId()),
                s.dayOfWeek().name(),
                s.startTime().toString(),
                s.endTime().toString(),
                s.room(),
                Integer.toString(s.capacity()),
                s.semester(),
                Integer.toString(s.year())
        )).collect(Collectors.toList());
        Files.write(dataFile, lines, StandardCharsets.UTF_8);
    }
}
