package com.epam.domain.port;

import java.util.List;
import java.util.Optional;

import com.epam.domain.model.TrainingType;
import com.epam.domain.model.TrainingTypeEnum;

public interface TrainingTypeRepository extends CrudRepository<TrainingType> {

    Optional<TrainingType> findByTrainingTypeName(TrainingTypeEnum trainingName);

    List<TrainingType> getTrainingTypes();

}
