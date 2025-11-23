package com.epam.interfaces.web.dto.request;

import java.time.LocalDate;
import java.util.Optional;

public record TraineeRegistrationRequest(String firstName,

		String lastName,

		Optional<LocalDate> dateOfBirth,

		Optional<String> address) {

}
