package com.epam.interfaces.web.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record UpdateTraineeTrainersRequest(@NotEmpty(message = "Trainer list cannot be empty") @NotNull(
		message = "Trainer list is required") List<@NotNull(
				message = "Trainer username cannot be null") String> trainerUsernames) {
}