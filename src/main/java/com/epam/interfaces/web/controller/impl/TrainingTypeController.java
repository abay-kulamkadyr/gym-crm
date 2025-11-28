package com.epam.interfaces.web.controller.impl;

import com.epam.application.facade.GymFacade;
import com.epam.interfaces.web.controller.api.TrainingTypeControllerApi;
import com.epam.interfaces.web.dto.response.TrainingTypeResponse;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/training-types")
public class TrainingTypeController implements TrainingTypeControllerApi {

	private final GymFacade gymFacade;

	@Autowired
	public TrainingTypeController(GymFacade gymFacade) {
		this.gymFacade = gymFacade;
	}

	@GetMapping
	@Override
	public ResponseEntity<List<TrainingTypeResponse>> getTrainingTypes() {
		List<TrainingTypeResponse> response = gymFacade.getTrainingTypes()
			.stream()
			.map(type -> new TrainingTypeResponse(type.getTrainingTypeId(), type.getTrainingTypeName()))
			.toList();
		return ResponseEntity.ok(response);
	}

}