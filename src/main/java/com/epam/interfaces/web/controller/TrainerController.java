package com.epam.interfaces.web.controller;

import com.epam.application.Credentials;
import com.epam.application.facade.GymFacade;
import com.epam.application.request.CreateTrainerProfileRequest;
import com.epam.application.request.UpdateTrainerProfileRequest;
import com.epam.domain.TrainingFilter;
import com.epam.domain.model.Trainer;
import com.epam.interfaces.web.dto.request.TrainerRegistrationRequest;
import com.epam.interfaces.web.dto.request.UpdateTrainerRequest;
import com.epam.interfaces.web.dto.response.CredentialsResponse;
import com.epam.interfaces.web.dto.response.EmbeddedTraineeResponse;
import com.epam.interfaces.web.dto.response.EmbeddedTrainerTrainingResponse;
import com.epam.interfaces.web.dto.response.TrainerResponse;
import com.epam.interfaces.web.util.AuthenticationHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/trainers")
@Tag(name = "Trainers", description = "Trainer management operations")
public class TrainerController {

	private final GymFacade gymFacade;

	private final AuthenticationHelper authenticationHelper;

	private final String AUTHORIZATION_HEADER = "TemporaryAuthentication";

	@Autowired
	public TrainerController(GymFacade gymFacade, AuthenticationHelper authenticationHelper) {
		this.gymFacade = gymFacade;
		this.authenticationHelper = authenticationHelper;
	}

	@PostMapping
	@Operation(summary = "Register Trainer", description = "Create a new trainer profile")
	public ResponseEntity<CredentialsResponse> register(@Valid @RequestBody TrainerRegistrationRequest request) {

		CreateTrainerProfileRequest createRequest = new CreateTrainerProfileRequest(request.firstName(),
				request.lastName(), true, request.specialization());

		Trainer trainer = gymFacade.createTrainerProfile(createRequest);

		CredentialsResponse response = new CredentialsResponse(trainer.getUsername(), trainer.getPassword());

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/{username}")
	@Operation(summary = "Get Trainer Profile", description = "Retrieve trainer profile by username")
	public ResponseEntity<TrainerResponse> getProfile(
			@Parameter(description = "Trainer username", required = true) @PathVariable String username,
			@Parameter(description = "Credentials in the format: username:password", required = true,
					example = "john.doe:password123") @RequestHeader(value = AUTHORIZATION_HEADER) String auth) {

		Credentials credentials = authenticationHelper.extractAndValidateCredentials(auth, username);

		Trainer trainer = gymFacade.findTrainerByUsername(credentials)
			.orElseThrow(() -> new EntityNotFoundException("Trainer not found with username: " + username));

		List<EmbeddedTraineeResponse> trainees = gymFacade.getTrainerTrainees(credentials)
			.stream()
			.map(EmbeddedTraineeResponse::toEmbeddedTrainee)
			.toList();

		TrainerResponse trainerResponse = new TrainerResponse(Optional.empty(), trainer.getFirstName(),
				trainer.getLastName(), trainer.getSpecialization().getTrainingTypeName(), trainer.getActive(),
				trainees);
		return ResponseEntity.ok(trainerResponse);
	}

	@PutMapping("/{username}")
	@Operation(summary = "Update Trainer Profile", description = "Update trainer profile information")
	public ResponseEntity<TrainerResponse> updateProfile(
			@Parameter(description = "Trainer username", required = true) @PathVariable String username,
			@Valid @RequestBody UpdateTrainerRequest request,
			@Parameter(description = "Credentials in the format: username:password", required = true,
					example = "john.doe:password123") @RequestHeader(value = AUTHORIZATION_HEADER) String auth) {

		Credentials credentials = authenticationHelper.extractAndValidateCredentials(auth, username);

		UpdateTrainerProfileRequest updateProfileRequest = new UpdateTrainerProfileRequest(credentials,
				Optional.of(request.firstName()), Optional.of(request.lastName()), Optional.empty(), Optional.empty(),
				Optional.of(request.active()), Optional.of(request.specialization()));

		Trainer trainer = gymFacade.updateTrainerProfile(updateProfileRequest);

		List<EmbeddedTraineeResponse> trainees = gymFacade.getTrainerTrainees(credentials)
			.stream()
			.map(EmbeddedTraineeResponse::toEmbeddedTrainee)
			.toList();

		TrainerResponse response = new TrainerResponse(Optional.of(trainer.getUsername()), trainer.getFirstName(),
				trainer.getLastName(), trainer.getSpecialization().getTrainingTypeName(), trainer.getActive(),
				trainees);

		return ResponseEntity.ok(response);
	}

	@GetMapping("/{username}/trainings")
	@Operation(summary = "Get Trainer Trainings",
			description = "Retrieve trainer's training list with optional filters")
	public ResponseEntity<List<EmbeddedTrainerTrainingResponse>> getTrainings(
			@Parameter(description = "Trainer username", required = true) @PathVariable String username,
			@Parameter(description = "Period start date") @RequestParam(required = false) LocalDateTime periodFrom,
			@Parameter(description = "Period end date") @RequestParam(required = false) LocalDateTime periodTo,
			@Parameter(description = "Trainee name filter") @RequestParam(required = false) String traineeName,
			@Parameter(description = "Credentials in the format: username:password", required = true,
					example = "john.doe:password123") @RequestHeader(value = AUTHORIZATION_HEADER) String auth) {

		Credentials credentials = authenticationHelper.extractAndValidateCredentials(auth, username);

		TrainingFilter filter = TrainingFilter.forTrainer(Optional.ofNullable(periodFrom),
				Optional.ofNullable(periodTo), Optional.ofNullable(traineeName));

		List<EmbeddedTrainerTrainingResponse> response = gymFacade.getTrainerTrainings(credentials, filter)
			.stream()
			.map(EmbeddedTrainerTrainingResponse::toEmbeddedTraining)
			.toList();

		return ResponseEntity.ok(response);
	}

	@PatchMapping("/{username}/activation")
	@Operation(summary = "Activate/Deactivate Trainer", description = "Change trainer active status")
	public ResponseEntity<Void> toggleActivation(
			@Parameter(description = "Trainer username", required = true) @PathVariable String username,
			@Parameter(description = "Credentials in the format: username:password", required = true,
					example = "john.doe:password123") @RequestHeader(value = AUTHORIZATION_HEADER) String auth) {
		Credentials credentials = authenticationHelper.extractAndValidateCredentials(auth, username);
		gymFacade.toggleTrainerActiveStatus(credentials);
		return ResponseEntity.ok().build();
	}

}
