package com.epam.interfaces.web.dto.response;

import com.epam.domain.model.Training;
import com.epam.domain.model.TrainingTypeEnum;
import java.time.LocalDateTime;

public record EmbeddedTrainerTrainingResponse(String trainingName, LocalDateTime trainingDate,
		TrainingTypeEnum trainingType, Integer durationMin, String traineeName) {

	public static EmbeddedTrainerTrainingResponse toEmbeddedTraining(Training training) {

		return new EmbeddedTrainerTrainingResponse(training.getTrainingName(), training.getTrainingDate(),
				training.getTrainingType().getTrainingTypeName(), training.getTrainingDurationMin(),
				training.getTrainee().getUsername());
	}
}
