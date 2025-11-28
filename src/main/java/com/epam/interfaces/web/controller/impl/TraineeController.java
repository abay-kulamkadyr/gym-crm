package com.epam.interfaces.web.controller.impl;

import com.epam.application.Credentials;
import com.epam.application.facade.GymFacade;
import com.epam.application.request.CreateTraineeProfileRequest;
import com.epam.application.request.UpdateTraineeProfileRequest;
import com.epam.domain.TrainingFilter;
import com.epam.domain.model.Trainee;
import com.epam.domain.model.TrainingTypeEnum;
import com.epam.interfaces.web.controller.api.TraineeControllerApi;
import com.epam.interfaces.web.dto.request.TraineeRegistrationRequest;
import com.epam.interfaces.web.dto.request.UpdateTraineeRequest;
import com.epam.interfaces.web.dto.request.UpdateTraineeTrainersRequest;
import com.epam.interfaces.web.dto.response.CredentialsResponse;
import com.epam.interfaces.web.dto.response.EmbeddedTrainerResponse;
import com.epam.interfaces.web.dto.response.EmbeddedTraineeTrainingResponse;
import com.epam.interfaces.web.dto.response.TraineeResponse;
import com.epam.interfaces.web.util.AuthenticationHelper;
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
public class TraineeController implements TraineeControllerApi {

	private final GymFacade gymFacade;

	private final AuthenticationHelper authenticationHelper;

	private final String AUTHORIZATION_HEADER = "TemporaryAuthentication";

	@Autowired
	public TraineeController(GymFacade gymFacade, AuthenticationHelper authenticationHelper) {
		this.gymFacade = gymFacade;
		this.authenticationHelper = authenticationHelper;
	}

	@PostMapping
	@Override
	public ResponseEntity<CredentialsResponse> register(@Valid @RequestBody TraineeRegistrationRequest request) {
		CreateTraineeProfileRequest createTraineeProfileRequest = new CreateTraineeProfileRequest(request.firstName(),
				request.lastName(), true, request.dateOfBirth(), request.address());

		Trainee createdTrainee = gymFacade.createTraineeProfile(createTraineeProfileRequest);

		CredentialsResponse response = new CredentialsResponse(createdTrainee.getUsername(),
				createdTrainee.getPassword());
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/{username}")
	@Override
	public ResponseEntity<TraineeResponse> getProfile(@PathVariable String username,
			@RequestHeader(value = AUTHORIZATION_HEADER) String auth) {

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
	@Override
	public ResponseEntity<TraineeResponse> updateTraineeProfile(@PathVariable String username,
			@Valid @RequestBody UpdateTraineeRequest request,
			@RequestHeader(value = AUTHORIZATION_HEADER) String auth) {

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
	@Override
	public ResponseEntity<Void> deleteTraineeProfile(@PathVariable String username,
			@RequestHeader(value = AUTHORIZATION_HEADER) String auth) {

		Credentials credentials = authenticationHelper.extractAndValidateCredentials(auth, username);
		gymFacade.deleteTraineeProfile(credentials);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/{username}/available-trainers")
	@Override
	public ResponseEntity<List<EmbeddedTrainerResponse>> getAvailableTrainers(@PathVariable String username,
			@RequestHeader(value = AUTHORIZATION_HEADER) String auth) {

		Credentials credentials = authenticationHelper.extractAndValidateCredentials(auth, username);

		List<EmbeddedTrainerResponse> response = gymFacade.getTraineeUnassignedTrainers(credentials)
			.stream()
			.map(EmbeddedTrainerResponse::toEmbeddedTrainer)
			.toList();

		return ResponseEntity.ok(response);
	}

	@PutMapping("/{username}/trainers")
	@Override
	public ResponseEntity<List<EmbeddedTrainerResponse>> updateTrainers(@PathVariable String username,
			@Valid @RequestBody UpdateTraineeTrainersRequest request,
			@RequestHeader(value = AUTHORIZATION_HEADER) String auth) {

		Credentials credentials = authenticationHelper.extractAndValidateCredentials(auth, username);

		gymFacade.updateTraineeTrainersList(credentials, request.trainerUsernames());
		List<EmbeddedTrainerResponse> response = gymFacade.getTraineeTrainers(credentials)
			.stream()
			.map(EmbeddedTrainerResponse::toEmbeddedTrainer)
			.toList();

		return ResponseEntity.ok(response);
	}

	@GetMapping("/{username}/trainings")
	@Override
	public ResponseEntity<List<EmbeddedTraineeTrainingResponse>> getTrainings(@PathVariable String username,
			@RequestParam(required = false) LocalDateTime periodFrom,
			@RequestParam(required = false) LocalDateTime periodTo, @RequestParam(required = false) String trainerName,
			@RequestParam(required = false) TrainingTypeEnum trainingType,
			@RequestHeader(value = AUTHORIZATION_HEADER) String auth) {

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
	@Override
	public ResponseEntity<Void> toggleActivation(@PathVariable String username,
			@RequestHeader(value = AUTHORIZATION_HEADER) String auth) {

		Credentials credentials = authenticationHelper.extractAndValidateCredentials(auth, username);
		gymFacade.toggleTraineeActiveStatus(credentials);
		return ResponseEntity.ok().build();
	}

}