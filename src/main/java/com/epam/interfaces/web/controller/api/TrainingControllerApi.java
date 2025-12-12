package com.epam.interfaces.web.controller.api;

import com.epam.interfaces.web.dto.request.AddTrainingRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;

@Tag(name = "Trainings", description = "Training management operations")
public interface TrainingControllerApi {

	@Operation(summary = "Add Training",
			description = "Create a new training session, training type is determined by the trainer's specialization")
	ResponseEntity<Void> addTraining(@Valid AddTrainingRequest request);

}
