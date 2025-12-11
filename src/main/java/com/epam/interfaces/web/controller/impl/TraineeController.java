package com.epam.interfaces.web.controller.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
import com.epam.interfaces.web.dto.response.EmbeddedTraineeTrainingResponse;
import com.epam.interfaces.web.dto.response.EmbeddedTrainerResponse;
import com.epam.interfaces.web.dto.response.TraineeResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trainees")
public class TraineeController implements TraineeControllerApi {

    private final GymFacade gymFacade;

    @Autowired
    public TraineeController(GymFacade gymFacade) {
        this.gymFacade = gymFacade;
    }

    @Override
    @PostMapping
    public ResponseEntity<CredentialsResponse> register(@Valid @RequestBody TraineeRegistrationRequest request) {
        CreateTraineeProfileRequest createTraineeProfileRequest = new CreateTraineeProfileRequest(request
                .firstName(), request.lastName(), true, request.dateOfBirth(), request.address());
        Trainee createdTrainee = gymFacade.createTraineeProfile(createTraineeProfileRequest);
        CredentialsResponse response =
                new CredentialsResponse(createdTrainee.getUsername(), createdTrainee.getPassword());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    @GetMapping("/{username}")
    @PreAuthorize("#username == authentication.name")
    public ResponseEntity<TraineeResponse> getProfile(@PathVariable String username) {
        Trainee trainee = gymFacade.getTraineeByUsername(username);
        List<EmbeddedTrainerResponse> trainers = gymFacade
                .getTraineeTrainers(username)
                .stream()
                .map(EmbeddedTrainerResponse::toEmbeddedTrainer)
                .toList();
        TraineeResponse response = new TraineeResponse(Optional.empty(),
                trainee.getFirstName(),
                trainee.getLastName(),
                trainee.getDob(),
                trainee.getAddress(),
                trainee.getActive(),
                trainers);
        return ResponseEntity.ok(response);
    }

    @Override
    @PutMapping("/{username}")
    @PreAuthorize("#username == authentication.name")
    public ResponseEntity<TraineeResponse> updateTraineeProfile(
            @PathVariable String username,
            @Valid @RequestBody UpdateTraineeRequest request) {
        UpdateTraineeProfileRequest updateProfileRequest = new UpdateTraineeProfileRequest(username,
                Optional.of(request.firstName()),
                Optional.of(request.lastName()),
                Optional.empty(),
                Optional.of(request.active()),
                Optional.ofNullable(request.dateOfBirth()),
                Optional.ofNullable(request.address()));
        Trainee trainee = gymFacade.updateTraineeProfile(updateProfileRequest);

        List<EmbeddedTrainerResponse> trainers = gymFacade
                .getTraineeTrainers(username)
                .stream()
                .map(EmbeddedTrainerResponse::toEmbeddedTrainer)
                .toList();

        TraineeResponse response = new TraineeResponse(Optional.of(trainee.getUsername()),
                trainee.getFirstName(),
                trainee.getLastName(),
                trainee.getDob(),
                trainee.getAddress(),
                trainee.getActive(),
                trainers);

        return ResponseEntity.ok(response);
    }

    @Override
    @DeleteMapping("/{username}")
    @PreAuthorize("#username == authentication.name")
    public ResponseEntity<Void> deleteTraineeProfile(@PathVariable String username) {
        gymFacade.deleteTraineeProfile(username);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/{username}/available-trainers")
    @PreAuthorize("#username == authentication.name")
    public ResponseEntity<List<EmbeddedTrainerResponse>> getAvailableTrainers(@PathVariable String username) {
        List<EmbeddedTrainerResponse> response = gymFacade
                .getTraineeUnassignedTrainers(username)
                .stream()
                .map(EmbeddedTrainerResponse::toEmbeddedTrainer)
                .toList();
        return ResponseEntity.ok(response);
    }

    @Override
    @PutMapping("/{username}/trainers")
    @PreAuthorize("#username == authentication.name")
    public ResponseEntity<List<EmbeddedTrainerResponse>> updateTrainers(
            @PathVariable String username,
            @Valid @RequestBody UpdateTraineeTrainersRequest request) {
        gymFacade.updateTraineeTrainersList(username, request.trainerUsernames());
        List<EmbeddedTrainerResponse> response = gymFacade
                .getTraineeTrainers(username)
                .stream()
                .map(EmbeddedTrainerResponse::toEmbeddedTrainer)
                .toList();
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/{username}/trainings")
    @PreAuthorize("#username == authentication.name")
    public ResponseEntity<List<EmbeddedTraineeTrainingResponse>> getTrainings(
            @PathVariable String username,
            @RequestParam(required = false) LocalDateTime periodFrom,
            @RequestParam(required = false) LocalDateTime periodTo,
            @RequestParam(required = false) String trainerName,
            @RequestParam(required = false) TrainingTypeEnum trainingType) {
        TrainingFilter filter = TrainingFilter
                .forTrainee(
                    Optional.ofNullable(periodFrom),
                    Optional.ofNullable(periodTo),
                    Optional.ofNullable(trainerName),
                    Optional.ofNullable(trainingType));
        List<EmbeddedTraineeTrainingResponse> response = gymFacade
                .getTraineeTrainings(username, filter)
                .stream()
                .map(EmbeddedTraineeTrainingResponse::toEmbeddedTraining)
                .toList();
        return ResponseEntity.ok(response);
    }

    @Override
    @PatchMapping("/{username}/activation")
    @PreAuthorize("#username == authentication.name")
    public ResponseEntity<Void> toggleActivation(@PathVariable String username) {
        gymFacade.toggleTraineeActiveStatus(username);
        return ResponseEntity.ok().build();
    }

}
