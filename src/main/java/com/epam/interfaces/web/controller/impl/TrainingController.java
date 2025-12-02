package com.epam.interfaces.web.controller.impl;

import com.epam.application.facade.GymFacade;
import com.epam.application.request.CreateTrainingRequest;
import com.epam.interfaces.web.controller.api.TrainingControllerApi;
import com.epam.interfaces.web.dto.request.AddTrainingRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/trainings")
public class TrainingController implements TrainingControllerApi {

	private final GymFacade gymFacade;

	@Autowired
	public TrainingController(GymFacade gymFacade) {
		this.gymFacade = gymFacade;
	}

	@PostMapping
	@Override
	public ResponseEntity<Void> addTraining(@Valid @RequestBody AddTrainingRequest request) {
		CreateTrainingRequest createTrainingRequest = new CreateTrainingRequest(request.trainingName(),
				request.trainingDate(), request.trainingDurationMin(), Optional.empty(), request.traineeUsername(),
				request.trainerUsername());
		gymFacade.createTraining(createTrainingRequest);
		return ResponseEntity.ok().build();
	}

}