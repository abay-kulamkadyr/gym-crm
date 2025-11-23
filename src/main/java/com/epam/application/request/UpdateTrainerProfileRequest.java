package com.epam.application.request;

import com.epam.application.Credentials;
import com.epam.application.request.types.AuthenticatedRequest;
import com.epam.application.request.types.UpdateProfileRequest;
import com.epam.domain.model.TrainingTypeEnum;
import java.util.Optional;

public record UpdateTrainerProfileRequest(Credentials credentials, Optional<String> firstName,
		Optional<String> lastName, Optional<String> username, Optional<String> password, Optional<Boolean> active,
		Optional<TrainingTypeEnum> specialization) implements AuthenticatedRequest, UpdateProfileRequest {

}
