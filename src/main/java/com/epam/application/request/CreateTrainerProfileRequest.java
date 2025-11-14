package com.epam.application.request;

import com.epam.application.request.types.CreateProfileRequest;
import com.epam.domain.model.TrainingType;

public record CreateTrainerProfileRequest(String firstName, String lastName, Boolean active,
		TrainingType specialization) implements CreateProfileRequest {

}
