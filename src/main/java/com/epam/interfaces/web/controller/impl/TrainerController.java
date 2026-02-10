package com.epam.interfaces.web.controller.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.epam.application.facade.GymFacade;
import com.epam.application.request.CreateTrainerProfileRequest;
import com.epam.application.request.UpdateTrainerProfileRequest;
import com.epam.domain.TrainingFilter;
import com.epam.domain.model.Trainer;
import com.epam.interfaces.web.controller.api.TrainerControllerApi;
import com.epam.interfaces.web.dto.request.TrainerRegistrationRequest;
import com.epam.interfaces.web.dto.request.UpdateTrainerRequest;
import com.epam.interfaces.web.dto.response.CredentialsResponse;
import com.epam.interfaces.web.dto.response.EmbeddedTraineeResponse;
import com.epam.interfaces.web.dto.response.EmbeddedTrainerTrainingResponse;
import com.epam.interfaces.web.dto.response.TrainerResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api/trainers")
public class TrainerController implements TrainerControllerApi {

    private final GymFacade gymFacade;

    @Autowired
    public TrainerController(GymFacade gymFacade) {
        this.gymFacade = gymFacade;
    }

    @Override
    @PostMapping
    public ResponseEntity<CredentialsResponse> register(@Valid @RequestBody TrainerRegistrationRequest request) {
        CreateTrainerProfileRequest createRequest = new CreateTrainerProfileRequest(
                request.firstName(), request.lastName(), true, request.specialization());

        Trainer trainer = gymFacade.createTrainerProfile(createRequest);

        CredentialsResponse response = new CredentialsResponse(trainer.getUsername(), trainer.getPassword());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    @GetMapping("/{username}")
    @PreAuthorize("#username == authentication.name")
    public ResponseEntity<TrainerResponse> getProfile(@PathVariable String username) {
        Trainer trainer = gymFacade.getTrainerByUsername(username);

        List<EmbeddedTraineeResponse> trainees = gymFacade.getTrainerTrainees(username).stream()
                .map(EmbeddedTraineeResponse::toEmbeddedTrainee)
                .toList();

        TrainerResponse trainerResponse = new TrainerResponse(
                Optional.empty(),
                trainer.getFirstName(),
                trainer.getLastName(),
                trainer.getSpecialization().getTrainingTypeName(),
                trainer.getActive(),
                trainees);
        return ResponseEntity.ok(trainerResponse);
    }

    @Override
    @PutMapping("/{username}")
    @PreAuthorize("#username == authentication.name")
    public ResponseEntity<TrainerResponse> updateProfile(
            @PathVariable String username, @Valid @RequestBody UpdateTrainerRequest request) {

        UpdateTrainerProfileRequest updateProfileRequest = new UpdateTrainerProfileRequest(
                username,
                Optional.of(request.firstName()),
                Optional.of(request.lastName()),
                Optional.empty(),
                Optional.of(request.active()),
                Optional.of(request.specialization()));

        Trainer trainer = gymFacade.updateTrainerProfile(updateProfileRequest);

        List<EmbeddedTraineeResponse> trainees = gymFacade.getTrainerTrainees(username).stream()
                .map(EmbeddedTraineeResponse::toEmbeddedTrainee)
                .toList();

        TrainerResponse response = new TrainerResponse(
                Optional.of(trainer.getUsername()),
                trainer.getFirstName(),
                trainer.getLastName(),
                trainer.getSpecialization().getTrainingTypeName(),
                trainer.getActive(),
                trainees);

        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/{username}/trainings")
    @PreAuthorize("#username == authentication.name")
    public ResponseEntity<List<EmbeddedTrainerTrainingResponse>> getTrainings(
            @PathVariable String username,
            @RequestParam(required = false) LocalDateTime periodFrom,
            @RequestParam(required = false) LocalDateTime periodTo,
            @RequestParam(required = false) String traineeName) {
        TrainingFilter filter = TrainingFilter.forTrainer(
                Optional.ofNullable(periodFrom), Optional.ofNullable(periodTo), Optional.ofNullable(traineeName));

        List<EmbeddedTrainerTrainingResponse> response = gymFacade.getTrainerTrainings(username, filter).stream()
                .map(EmbeddedTrainerTrainingResponse::toEmbeddedTraining)
                .toList();

        return ResponseEntity.ok(response);
    }

    @Override
    @PatchMapping("/{username}/activation")
    @PreAuthorize("#username == authentication.name")
    public ResponseEntity<Void> toggleActivation(@PathVariable String username) {
        gymFacade.toggleTrainerActiveStatus(username);
        return ResponseEntity.ok().build();
    }
}
