package com.epam.application.request;

import java.time.LocalDate;
import java.util.Optional;

import com.epam.application.request.types.CreateProfileRequest;

public record CreateTraineeProfileRequest(String firstName, String lastName, Boolean active, Optional<LocalDate> dob,
        Optional<String> address) implements CreateProfileRequest {

}
