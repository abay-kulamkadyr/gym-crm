package com.epam.interfaces.web.controller.api;

import com.epam.interfaces.web.dto.request.TrainerRegistrationRequest;
import com.epam.interfaces.web.dto.request.UpdateTrainerRequest;
import com.epam.interfaces.web.dto.response.CredentialsResponse;
import com.epam.interfaces.web.dto.response.EmbeddedTrainerTrainingResponse;
import com.epam.interfaces.web.dto.response.TrainerResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Trainers", description = "Trainer management operations")
public interface TrainerControllerApi {

	@Operation(summary = "Register Trainer", description = "Create a new trainer profile")
	ResponseEntity<CredentialsResponse> register(@Valid TrainerRegistrationRequest request);

	@Operation(summary = "Get Trainer Profile", description = "Retrieve trainer profile by username")
	ResponseEntity<TrainerResponse> getProfile(
			@Parameter(description = "Trainer username", required = true) String username);

	@Operation(summary = "Update Trainer Profile", description = "Update trainer profile information")
	ResponseEntity<TrainerResponse> updateProfile(
			@Parameter(description = "Trainer username", required = true) String username,
			@Valid UpdateTrainerRequest request);

	@Operation(summary = "Get Trainer Trainings",
			description = "Retrieve trainer's training list with optional filters")
	ResponseEntity<List<EmbeddedTrainerTrainingResponse>> getTrainings(
			@Parameter(description = "Trainer username", required = true) String username,
			@Parameter(description = "Period start date") LocalDateTime periodFrom,
			@Parameter(description = "Period end date") LocalDateTime periodTo,
			@Parameter(description = "Trainee name filter") String traineeName);

	@Operation(summary = "Activate/Deactivate Trainer", description = "Change trainer active status")
	ResponseEntity<Void> toggleActivation(
			@Parameter(description = "Trainer username", required = true) String username);

}