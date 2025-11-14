package com.epam.application.request;

import com.epam.application.request.types.CreateProfileRequest;
import java.time.LocalDate;
import java.util.Optional;

public record CreateTraineeProfileRequest(String firstName, String lastName, Boolean active, Optional<LocalDate> dob,
		Optional<String> address) implements CreateProfileRequest {

}
