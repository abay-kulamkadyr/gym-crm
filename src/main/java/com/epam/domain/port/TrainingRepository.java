package com.epam.domain.port;

import com.epam.domain.TrainingFilter;
import com.epam.domain.model.Training;

import java.util.List;

public interface TrainingRepository extends CrudRepository<Training> {

	List<Training> getTraineeTrainings(String traineeUsername, TrainingFilter filter);

	List<Training> getTrainerTrainings(String trainerUsername, TrainingFilter filter);

}
