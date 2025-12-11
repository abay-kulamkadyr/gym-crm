package com.epam.domain.port;

import java.util.List;

import com.epam.domain.TrainingFilter;
import com.epam.domain.model.Training;

public interface TrainingRepository extends CrudRepository<Training> {

	List<Training> getTraineeTrainings(String traineeUsername, TrainingFilter filter);

	List<Training> getTrainerTrainings(String trainerUsername, TrainingFilter filter);

}
