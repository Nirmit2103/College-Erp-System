package edu.univ.erp.data.erp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import edu.univ.erp.domain.AssessmentComponent;

public class InMemoryAssessmentDao implements AssessmentDao {

    private final Map<Long, List<AssessmentComponent>> assessmentsBySection = new ConcurrentHashMap<>();
    private final Map<Long, AssessmentComponent> assessmentsById = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(1);

    public InMemoryAssessmentDao() {
        seed();
    }

    private void seed() {

        create(1L, "Quiz 1", 20.0);
        create(1L, "Quiz 2", 15.0);
        create(1L, "Midsem", 30.0);
        create(1L, "Endsem", 35.0);

        create(2L, "Quiz 1", 20.0);
        create(2L, "Quiz 2", 15.0);
        create(2L, "Midsem", 30.0);
        create(2L, "Endsem", 35.0);
    }

    @Override
    public List<AssessmentComponent> listBySection(long sectionId) {
        return Collections.unmodifiableList(new ArrayList<>(assessmentsBySection
                .getOrDefault(sectionId, List.of())));
    }

    @Override
    public AssessmentComponent create(long sectionId, String name, double weight) {
        long id = idSequence.getAndIncrement();
        AssessmentComponent component = new AssessmentComponent(id, sectionId, name, weight);
        assessmentsBySection.computeIfAbsent(sectionId, key -> new ArrayList<>()).add(component);
        assessmentsById.put(id, component);
        return component;
    }

    @Override
    public void update(AssessmentComponent component) {
        assessmentsById.put(component.id(), component);
        assessmentsBySection.computeIfPresent(component.sectionId(), (sectionId, list) -> {
            List<AssessmentComponent> updated = new ArrayList<>(list.size());
            for (AssessmentComponent item : list) {
                if (item.id() == component.id()) {
                    updated.add(component);
                } else {
                    updated.add(item);
                }
            }
            return updated;
        });
    }

    @Override
    public void delete(long componentId) {
        AssessmentComponent removed = assessmentsById.remove(componentId);
        if (removed != null) {
            assessmentsBySection.computeIfPresent(removed.sectionId(), (sectionId, list) -> {
                List<AssessmentComponent> updated = new ArrayList<>(list);
                updated.removeIf(component -> component.id() == componentId);
                return updated;
            });
        }
    }

    @Override
    public Optional<AssessmentComponent> findById(long componentId) {
        return Optional.ofNullable(assessmentsById.get(componentId));
    }
}

