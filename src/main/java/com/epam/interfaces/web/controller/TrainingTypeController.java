package com.epam.interfaces.web.controller;

import com.epam.application.facade.GymFacade;
import com.epam.interfaces.web.dto.response.TrainingTypeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/training-types")
@Tag(name = "Training Types", description = "Training type operations")
public class TrainingTypeController {

	private final GymFacade gymFacade;

	@Autowired
	public TrainingTypeController(GymFacade gymFacade) {
		this.gymFacade = gymFacade;
	}

	@GetMapping
	@Operation(summary = "Get Training Types", description = "Retrieve all available training types")
	public ResponseEntity<List<TrainingTypeResponse>> getTrainingTypes() {

		List<TrainingTypeResponse> response = gymFacade.getTrainingTypes()
			.stream()
			.map(type -> new TrainingTypeResponse(type.getTrainingTypeId(), type.getTrainingTypeName()))
			.toList();

		return ResponseEntity.ok(response);
	}

}
