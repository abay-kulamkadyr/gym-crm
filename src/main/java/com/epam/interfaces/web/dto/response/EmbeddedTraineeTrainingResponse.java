package com.epam.interfaces.web.dto.response;

import com.epam.domain.model.Training;
import com.epam.domain.model.TrainingTypeEnum;

import java.time.LocalDateTime;

public record EmbeddedTraineeTrainingResponse(String trainingName, LocalDateTime trainingDate,
		TrainingTypeEnum trainingType, Integer durationMin, String trainerName) {

	public static EmbeddedTraineeTrainingResponse toEmbeddedTraining(Training training) {

		return new EmbeddedTraineeTrainingResponse(training.getTrainingName(), training.getTrainingDate(),
				training.getTrainingType().getTrainingTypeName(), training.getTrainingDurationMin(),
				training.getTrainer().getUsername());
	}
}
