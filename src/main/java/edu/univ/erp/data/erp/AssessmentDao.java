package edu.univ.erp.data.erp;

import java.util.List;
import java.util.Optional;

import edu.univ.erp.domain.AssessmentComponent;

public interface AssessmentDao {

    List<AssessmentComponent> listBySection(long sectionId);

    AssessmentComponent create(long sectionId, String name, double weight);

    void update(AssessmentComponent component);

    void delete(long componentId);

    Optional<AssessmentComponent> findById(long componentId);
}

