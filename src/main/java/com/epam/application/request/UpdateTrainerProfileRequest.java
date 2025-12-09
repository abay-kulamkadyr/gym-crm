package com.epam.application.request;

import com.epam.application.Credentials;
import com.epam.application.request.types.AuthenticatedRequest;
import com.epam.application.request.types.UpdateProfileRequest;
import com.epam.domain.model.TrainingTypeEnum;

import java.util.Optional;

public record UpdateTrainerProfileRequest(String username, Optional<String> firstName, Optional<String> lastName,
		Optional<String> password, Optional<Boolean> active,
		Optional<TrainingTypeEnum> specialization) implements UpdateProfileRequest {

}
