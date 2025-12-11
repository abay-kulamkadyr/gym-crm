package com.epam.interfaces.web.dto.response;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_EMPTY)
public record TraineeResponse(Optional<String> username, String firstName, String lastName, LocalDate dob,
		String address, Boolean active, List<EmbeddedTrainerResponse> trainers) {

}
