package com.epam.interfaces.web.dto.request;

import com.epam.domain.model.TrainingTypeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateTrainerRequest(
		@NotBlank(message = "First name is required") @Size(min = 2, max = 50,
				message = "First name must be between 2 and 50 characters") String firstName,

		@NotBlank(message = "Last name is required") @Size(min = 2, max = 50,
				message = "Last name must be between 2 and 50 characters") String lastName,

		@NotNull(message = "Specialization is required") TrainingTypeEnum specialization,

		@NotNull(message = "Active status is required") Boolean active) {
}