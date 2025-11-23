package com.epam.interfaces.web.controller;

import com.epam.application.Credentials;
import com.epam.application.facade.GymFacade;
import com.epam.application.request.CreateTrainerProfileRequest;
import com.epam.domain.model.Trainer;
import com.epam.interfaces.web.dto.request.TrainerRegistrationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trainers")
public class TrainerController {

	private final GymFacade gymFacade;

	@Autowired
	public TrainerController(GymFacade gymFacade) {
		this.gymFacade = gymFacade;
	}

	// @PostMapping
	// public ResponseEntity<Credentials> register(@RequestBody TrainerRegistrationRequest
	// request) {
	// Trainer trainer = gymFacade.createTrainerProfile(new
	// CreateTrainerProfileRequest(request.firstName(),
	// request.firstName(), true, request.specialization()));
	// }

}
