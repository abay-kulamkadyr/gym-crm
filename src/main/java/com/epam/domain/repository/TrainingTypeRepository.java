package com.epam.domain.repository;

import com.epam.domain.model.TrainingType;
import com.epam.domain.model.TrainingTypeEnum;
import java.util.List;
import java.util.Optional;

public interface TrainingTypeRepository extends CrudRepository<TrainingType> {

	Optional<TrainingType> findByTrainingTypeName(TrainingTypeEnum trainingName);

	List<TrainingType> getTrainingTypes();

}
