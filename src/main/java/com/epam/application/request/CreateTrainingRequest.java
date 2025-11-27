package com.epam.application.request;

import com.epam.domain.model.TrainingTypeEnum;
import java.time.LocalDateTime;
import java.util.Optional;

public record CreateTrainingRequest(String trainingName, LocalDateTime trainingDate, Integer trainingDurationMin,
		Optional<TrainingTypeEnum> trainingType, String traineeUsername, String trainerUsername) {

}
