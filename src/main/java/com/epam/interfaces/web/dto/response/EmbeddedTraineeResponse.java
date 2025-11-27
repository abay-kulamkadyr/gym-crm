package com.epam.interfaces.web.dto.response;

import com.epam.domain.model.Trainee;

public record EmbeddedTraineeResponse(String username, String firstName, String lastName) {

	public static EmbeddedTraineeResponse toEmbeddedTrainee(Trainee trainee) {
		return new EmbeddedTraineeResponse(trainee.getUsername(), trainee.getFirstName(), trainee.getLastName());
	}
}
