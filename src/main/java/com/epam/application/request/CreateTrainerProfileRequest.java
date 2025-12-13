package com.epam.application.request;

import com.epam.application.request.types.CreateProfileRequest;
import com.epam.domain.model.TrainingTypeEnum;

public record CreateTrainerProfileRequest(String firstName, String lastName, Boolean active,
        TrainingTypeEnum specialization) implements CreateProfileRequest {

}
