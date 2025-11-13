package com.epam.application.request;

import com.epam.application.Credentials;
import com.epam.application.request.types.AuthenticatedRequest;
import com.epam.application.request.types.UpdateProfileRequest;
import java.util.Optional;

public record UpdateTrainerProfileRequest(Credentials credentials, Optional<String> firstName,
		Optional<String> lastName, Optional<String> username, Optional<String> password, Optional<Boolean> active,
		Optional<String> specialization) implements AuthenticatedRequest, UpdateProfileRequest {

}
