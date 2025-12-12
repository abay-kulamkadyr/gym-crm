package com.epam.application.request;

import java.util.Optional;

import com.epam.application.request.types.UpdateProfileRequest;
import com.epam.domain.model.TrainingTypeEnum;

public record UpdateTrainerProfileRequest(String username, Optional<String> firstName, Optional<String> lastName,
		Optional<String> password, Optional<Boolean> active,
		Optional<TrainingTypeEnum> specialization) implements UpdateProfileRequest {

}
