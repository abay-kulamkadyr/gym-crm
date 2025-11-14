package com.epam.domain.repository;

import com.epam.domain.model.TrainingType;
import java.util.List;
import java.util.Optional;

public interface TrainingTypeRepository extends CrudRepository<TrainingType> {

	Optional<TrainingType> findByTrainingTypeName(String trainingName);

	List<TrainingType> getTrainingTypes();

}
