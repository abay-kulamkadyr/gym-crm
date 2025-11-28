package com.epam.application.request;

import com.epam.application.Credentials;
import com.epam.application.request.types.AuthenticatedRequest;
import com.epam.application.request.types.UpdateProfileRequest;
import java.time.LocalDate;
import java.util.Optional;

public record UpdateTraineeProfileRequest(Credentials credentials, Optional<String> firstName,
		Optional<String> lastName, Optional<String> password, Optional<Boolean> active, Optional<LocalDate> dob,
		Optional<String> address) implements AuthenticatedRequest, UpdateProfileRequest {

}
