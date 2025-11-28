package com.epam.interfaces.web.controller;

import com.epam.application.Credentials;
import com.epam.application.facade.GymFacade;
import com.epam.application.request.CreateTraineeProfileRequest;
import com.epam.application.request.UpdateTraineeProfileRequest;
import com.epam.domain.TrainingFilter;
import com.epam.domain.model.Trainee;
import com.epam.domain.model.TrainingTypeEnum;
import com.epam.interfaces.web.dto.request.TraineeRegistrationRequest;
import com.epam.interfaces.web.dto.request.UpdateTraineeRequest;
import com.epam.interfaces.web.dto.request.UpdateTraineeTrainersRequest;
import com.epam.interfaces.web.dto.response.CredentialsResponse;
import com.epam.interfaces.web.dto.response.EmbeddedTrainerResponse;
import com.epam.interfaces.web.dto.response.EmbeddedTraineeTrainingResponse;
import com.epam.interfaces.web.dto.response.TraineeResponse;
import com.epam.interfaces.web.util.AuthenticationHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trainees")
@Tag(name = "Trainees", description = "Trainee management operations")
public class TraineeController {

	private final GymFacade gymFacade;

	private final AuthenticationHelper authenticationHelper;

	private final String AUTHORIZATION_HEADER = "TemporaryAuthentication";

	@Autowired
	public TraineeController(GymFacade gymFacade, AuthenticationHelper authenticationHelper) {
		this.gymFacade = gymFacade;
		this.authenticationHelper = authenticationHelper;
	}

	@PostMapping
	@Operation(summary = "Register Trainee", description = "Create a new trainee profile")
	public ResponseEntity<CredentialsResponse> register(@Valid @RequestBody TraineeRegistrationRequest request) {
		CreateTraineeProfileRequest createTraineeProfileRequest = new CreateTraineeProfileRequest(request.firstName(),
				request.lastName(), true, request.dateOfBirth(), request.address());

		Trainee createdTrainee = gymFacade.createTraineeProfile(createTraineeProfileRequest);

		CredentialsResponse response = new CredentialsResponse(createdTrainee.getUsername(),
				createdTrainee.getPassword());
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/{username}")
	@Operation(summary = "Get Trainee Profile", description = "Retrieve trainee profile by username")
	public ResponseEntity<TraineeResponse> getProfile(
			@Parameter(description = "Trainee username", required = true) @PathVariable String username,
			@Parameter(description = "Credentials in the format: username:password", required = true,
					example = "john.doe:password123") @RequestHeader(value = AUTHORIZATION_HEADER) String auth) {

		Credentials credentials = authenticationHelper.extractAndValidateCredentials(auth, username);

		Trainee trainee = gymFacade.findTraineeByUsername(credentials)
			.orElseThrow(() -> new EntityNotFoundException("Trainee not found with username: " + username));

		List<EmbeddedTrainerResponse> trainers = gymFacade.getTraineeTrainers(credentials)
			.stream()
			.map(EmbeddedTrainerResponse::toEmbeddedTrainer)
			.toList();

		TraineeResponse response = new TraineeResponse(Optional.empty(), trainee.getFirstName(), trainee.getLastName(),
				trainee.getDob(), trainee.getAddress(), trainee.getActive(), trainers);
		return ResponseEntity.ok(response);
	}

	@PutMapping("/{username}")
	@Operation(summary = "Update Trainee Profile", description = "Update trainee profile information")
	public ResponseEntity<TraineeResponse> updateTraineeProfile(
			@Parameter(description = "Trainee username", required = true) @PathVariable String username,
			@Valid @RequestBody UpdateTraineeRequest request,
			@Parameter(description = "Credentials in the format: username:password", required = true,
					example = "john.doe:password123") @RequestHeader(value = AUTHORIZATION_HEADER) String auth) {

		Credentials credentials = authenticationHelper.extractAndValidateCredentials(auth, username);

		UpdateTraineeProfileRequest updateProfileRequest = new UpdateTraineeProfileRequest(credentials,
				Optional.of(request.firstName()), Optional.of(request.lastName()), Optional.empty(),
				Optional.of(request.active()), Optional.ofNullable(request.dateOfBirth()),
				Optional.ofNullable(request.address()));

		Trainee trainee = gymFacade.updateTraineeProfile(updateProfileRequest);

		List<EmbeddedTrainerResponse> trainers = gymFacade.getTraineeTrainers(credentials)
			.stream()
			.map(EmbeddedTrainerResponse::toEmbeddedTrainer)
			.toList();

		TraineeResponse response = new TraineeResponse(Optional.of(trainee.getUsername()), trainee.getFirstName(),
				trainee.getLastName(), trainee.getDob(), trainee.getAddress(), trainee.getActive(), trainers);

		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/{username}")
	@Operation(summary = "Delete Trainee Profile", description = "Delete trainee profile and associated trainings")
	public ResponseEntity<Void> deleteTraineeProfile(
			@Parameter(description = "Trainee username", required = true) @PathVariable String username,
			@Parameter(description = "Credentials in the format: username:password", required = true,
					example = "john.doe:password123") @RequestHeader(value = AUTHORIZATION_HEADER) String auth) {

		Credentials credentials = authenticationHelper.extractAndValidateCredentials(auth, username);
		gymFacade.deleteTraineeProfile(credentials);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/{username}/available-trainers")
	@Operation(summary = "Get Available Trainers", description = "Get active trainers not assigned to this trainee")
	public ResponseEntity<List<EmbeddedTrainerResponse>> getAvailableTrainers(
			@Parameter(description = "Trainee username", required = true) @PathVariable String username,
			@Parameter(description = "Credentials in the format: username:password", required = true,
					example = "john.doe:password123") @RequestHeader(value = AUTHORIZATION_HEADER) String auth) {

		Credentials credentials = authenticationHelper.extractAndValidateCredentials(auth, username);

		List<EmbeddedTrainerResponse> response = gymFacade.getTraineeUnassignedTrainers(credentials)
			.stream()
			.map(EmbeddedTrainerResponse::toEmbeddedTrainer)
			.toList();

		return ResponseEntity.ok(response);
	}

	@PutMapping("/{username}/trainers")
	@Operation(summary = "Update Trainee's Trainers", description = "Update the list of trainers assigned to trainee")
	public ResponseEntity<List<EmbeddedTrainerResponse>> updateTrainers(
			@Parameter(description = "Trainee username", required = true) @PathVariable String username,
			@Valid @RequestBody UpdateTraineeTrainersRequest request,
			@Parameter(description = "Credentials in the format: username:password", required = true,
					example = "john.doe:password123") @RequestHeader(value = AUTHORIZATION_HEADER) String auth) {

		Credentials credentials = authenticationHelper.extractAndValidateCredentials(auth, username);

		gymFacade.updateTraineeTrainersList(credentials, request.trainerUsernames());
		List<EmbeddedTrainerResponse> response = gymFacade.getTraineeTrainers(credentials)
			.stream()
			.map(EmbeddedTrainerResponse::toEmbeddedTrainer)
			.toList();

		return ResponseEntity.ok(response);
	}

	@GetMapping("/{username}/trainings")
	public ResponseEntity<List<EmbeddedTraineeTrainingResponse>> getTrainings(
			@Parameter(description = "Trainee username", required = true) @PathVariable String username,
			@Parameter(description = "Period start date") @RequestParam(required = false) LocalDateTime periodFrom,
			@Parameter(description = "Period end date") @RequestParam(required = false) LocalDateTime periodTo,
			@Parameter(description = "Trainer name filter") @RequestParam(required = false) String trainerName,
			@Parameter(description = "Training type filter") @RequestParam(
					required = false) TrainingTypeEnum trainingType,
			@Parameter(description = "Credentials in the format: username:password", required = true,
					example = "john.doe:password123") @RequestHeader(value = AUTHORIZATION_HEADER) String auth) {

		Credentials credentials = authenticationHelper.extractAndValidateCredentials(auth, username);

		TrainingFilter filter = TrainingFilter.forTrainee(Optional.ofNullable(periodFrom),
				Optional.ofNullable(periodTo), Optional.ofNullable(trainerName), Optional.ofNullable(trainingType));

		List<EmbeddedTraineeTrainingResponse> response = gymFacade.getTraineeTrainings(credentials, filter)
			.stream()
			.map(EmbeddedTraineeTrainingResponse::toEmbeddedTraining)
			.toList();

		return ResponseEntity.ok(response);
	}

	@PatchMapping("/{username}/activation")
	@Operation(summary = "Activate/Deactivate Trainee", description = "Change trainee active status")
	public ResponseEntity<Void> toggleActivation(
			@Parameter(description = "Trainee username", required = true) @PathVariable String username,
			@Parameter(description = "Credentials in the format: username:password", required = true,
					example = "john.doe:password123") @RequestHeader(value = AUTHORIZATION_HEADER) String auth) {
		Credentials credentials = authenticationHelper.extractAndValidateCredentials(auth, username);

		gymFacade.toggleTraineeActiveStatus(credentials);

		return ResponseEntity.ok().build();
	}

}