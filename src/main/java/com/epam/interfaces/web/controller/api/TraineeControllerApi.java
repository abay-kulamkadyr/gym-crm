package com.epam.interfaces.web.controller.api;

import java.time.LocalDateTime;
import java.util.List;

import com.epam.domain.model.TrainingTypeEnum;
import com.epam.interfaces.web.dto.request.TraineeRegistrationRequest;
import com.epam.interfaces.web.dto.request.UpdateTraineeRequest;
import com.epam.interfaces.web.dto.request.UpdateTraineeTrainersRequest;
import com.epam.interfaces.web.dto.response.CredentialsResponse;
import com.epam.interfaces.web.dto.response.EmbeddedTraineeTrainingResponse;
import com.epam.interfaces.web.dto.response.EmbeddedTrainerResponse;
import com.epam.interfaces.web.dto.response.TraineeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;

@Tag(name = "Trainees", description = "Trainee management operations")
public interface TraineeControllerApi {

    @Operation(summary = "Register Trainee", description = "Create a new trainee profile")
    ResponseEntity<CredentialsResponse> register(@Valid TraineeRegistrationRequest request);

    @Operation(summary = "Get Trainee Profile", description = "Retrieve trainee profile by username")
    ResponseEntity<TraineeResponse> getProfile(
            @Parameter(description = "Trainee username", required = true) String username);

    @Operation(summary = "Update Trainee Profile", description = "Update trainee profile information")
    ResponseEntity<TraineeResponse> updateTraineeProfile(
            @Parameter(description = "Trainee username", required = true) String username,
            @Valid UpdateTraineeRequest request);

    @Operation(summary = "Delete Trainee Profile", description = "Delete trainee profile and associated trainings")
    ResponseEntity<Void> deleteTraineeProfile(
            @Parameter(description = "Trainee username", required = true) String username);

    @Operation(summary = "Get Available Trainers", description = "Get active trainers not assigned to this trainee")
    ResponseEntity<List<EmbeddedTrainerResponse>> getAvailableTrainers(
            @Parameter(description = "Trainee username", required = true) String username);

    @Operation(summary = "Update Trainee's Trainers", description = "Update the list of trainers assigned to trainee")
    ResponseEntity<List<EmbeddedTrainerResponse>> updateTrainers(
            @Parameter(description = "Trainee username", required = true) String username,
            @Valid UpdateTraineeTrainersRequest request);

    @Operation(summary = "Get Trainee Trainings", description = "Retrieve trainee's training history with filters")
    ResponseEntity<List<EmbeddedTraineeTrainingResponse>> getTrainings(
            @Parameter(description = "Trainee username", required = true) String username,
            @Parameter(description = "Period start date") LocalDateTime periodFrom,
            @Parameter(description = "Period end date") LocalDateTime periodTo,
            @Parameter(description = "Trainer name filter") String trainerName,
            @Parameter(description = "Training type filter") TrainingTypeEnum trainingType);

    @Operation(summary = "Activate/Deactivate Trainee", description = "Change trainee active status")
    ResponseEntity<Void> toggleActivation(
            @Parameter(description = "Trainee username", required = true) String username);

}
