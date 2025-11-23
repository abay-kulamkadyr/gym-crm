package com.epam.interfaces.web.controller;

import com.epam.application.facade.GymFacade;
import com.epam.application.request.CreateTraineeProfileRequest;
import com.epam.domain.model.Trainee;
import com.epam.interfaces.web.dto.request.TraineeRegistrationRequest;
import com.epam.interfaces.web.dto.response.CredentialsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trainees")
public class TraineeController {

	private final GymFacade gymFacade;

	@Autowired
	public TraineeController(GymFacade gymFacade) {
		this.gymFacade = gymFacade;
	}

	@PostMapping
	public ResponseEntity<CredentialsResponse> register(@RequestBody TraineeRegistrationRequest request) {
		Trainee createdTrainee = gymFacade.createTraineeProfile(new CreateTraineeProfileRequest(request.firstName(),
				request.lastName(), true, request.dateOfBirth(), request.address()));
		CredentialsResponse credentials = new CredentialsResponse(createdTrainee.getUsername(),
				createdTrainee.getPassword());
		return ResponseEntity.ok(credentials);
	}

}
