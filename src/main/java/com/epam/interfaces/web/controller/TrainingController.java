package com.epam.interfaces.web.controller;

import com.epam.application.facade.GymFacade;
import com.epam.application.request.CreateTrainingRequest;
import com.epam.interfaces.web.dto.request.AddTrainingRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trainings")
@Tag(name = "Trainings", description = "Training management operations")
public class TrainingController {

	private final GymFacade gymFacade;

	@Autowired
	public TrainingController(GymFacade gymFacade) {
		this.gymFacade = gymFacade;
	}

	@PostMapping
	@Operation(summary = "Add Training",
			description = "Create a new training session, training type is determined by the trainer's specialization")
	public ResponseEntity<Void> addTraining(@Valid @RequestBody AddTrainingRequest request) {

		CreateTrainingRequest createTrainingRequest = new CreateTrainingRequest(request.trainingName(),
				request.trainingDate(), request.trainingDurationMin(), Optional.empty(), request.traineeUsername(),
				request.trainerUsername());

		gymFacade.createTraining(createTrainingRequest);
		return ResponseEntity.ok().build();
	}

}
