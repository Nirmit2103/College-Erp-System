package edu.univ.erp.data.erp;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import edu.univ.erp.domain.Student;

public class InMemoryStudentDao implements StudentDao {

    private final Map<Long, Student> students = new ConcurrentHashMap<>();

    public InMemoryStudentDao() {
        seed();
    }

    private void seed() {

        students.put(6L, new Student(6, "CS2024001", "B.Tech Computer Science", 2));
        students.put(7L, new Student(7, "CS2024002", "B.Tech Computer Science", 2));
        students.put(8L, new Student(8, "CS2024003", "B.Tech Computer Science", 3));
        students.put(9L, new Student(9, "CS2024004", "B.Tech Computer Science", 3));
        students.put(10L, new Student(10, "CS2024005", "B.Tech Computer Science", 4));
    }

    @Override
    public Optional<Student> findByUserId(long userId) {
        return Optional.ofNullable(students.get(userId));
    }

    @Override
    public Student create(long userId, String rollNo, String program, int year) {
        if (students.containsKey(userId)) {
            throw new IllegalArgumentException("Student profile already exists for user: " + userId);
        }
        Student student = new Student(userId, rollNo, program, year);
        students.put(userId, student);
        return student;
    }

    @Override
    public void deleteByUserId(long userId) {
        students.remove(userId);
    }
}

