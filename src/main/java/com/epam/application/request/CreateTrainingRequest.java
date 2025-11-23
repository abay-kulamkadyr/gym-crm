package com.epam.application.request;

import com.epam.application.Credentials;
import com.epam.application.request.types.AuthenticatedRequest;
import com.epam.domain.model.TrainingTypeEnum;
import java.time.LocalDateTime;

public record CreateTrainingRequest(Credentials credentials, String trainingName, LocalDateTime trainingDate,
		Integer trainingDurationMin, TrainingTypeEnum trainingType, String traineeUsername,
		String trainerUsername) implements AuthenticatedRequest {

}
