package com.epam.application.request;

import java.time.LocalDate;
import java.util.Optional;

import com.epam.application.request.types.UpdateProfileRequest;

public record UpdateTraineeProfileRequest(String username, Optional<String> firstName, Optional<String> lastName,
        Optional<String> password, Optional<Boolean> active, Optional<LocalDate> dob, Optional<String> address)
        implements UpdateProfileRequest {

}
