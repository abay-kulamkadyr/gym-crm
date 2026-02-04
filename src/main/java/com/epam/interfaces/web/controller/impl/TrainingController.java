package com.epam.interfaces.web.controller.impl;

import java.util.Optional;

import com.epam.application.facade.GymFacade;
import com.epam.application.request.CreateTrainingRequest;
import com.epam.domain.model.Trainer;
import com.epam.interfaces.web.client.TrainerWorkloadInterface;
import com.epam.interfaces.web.client.request.ActionType;
import com.epam.interfaces.web.client.request.TrainerWorkloadWebRequest;
import com.epam.interfaces.web.controller.api.TrainingControllerApi;
import com.epam.interfaces.web.dto.request.AddTrainingRequest;
import com.epam.interfaces.web.dto.request.DeleteTrainingRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trainings")
public class TrainingController implements TrainingControllerApi {

    private final GymFacade gymFacade;
    private final TrainerWorkloadInterface trainerWorkload;

    @Autowired
    public TrainingController(GymFacade gymFacade, TrainerWorkloadInterface trainerWorkload) {
        this.gymFacade = gymFacade;
        this.trainerWorkload = trainerWorkload;
    }

    @Override
    @PostMapping
    @PreAuthorize("#request.traineeUsername == authentication.name or #request.trainerUsername == authentication.name")
    public ResponseEntity<Void> addTraining(@Valid @RequestBody AddTrainingRequest request) {
        CreateTrainingRequest createTrainingRequest = new CreateTrainingRequest(request.trainingName(),
                request.trainingDate(),
                request.trainingDurationMin(),
                Optional.empty(),
                request.traineeUsername(),
                request.trainerUsername());
        gymFacade.createTraining(createTrainingRequest);
        Trainer trainer = gymFacade.getTrainerByUsername(request.trainerUsername());
        trainerWorkload
                .processTrainerRequest(
                    new TrainerWorkloadWebRequest(request.trainerUsername(),
                            trainer.getFirstName(),
                            trainer.getLastName(),
                            trainer.getActive(),
                            request.trainingDate(),
                            request.trainingDurationMin(),
                            ActionType.ADD));
        return ResponseEntity.ok().build();
    }

    @Override
    @DeleteMapping
    @PreAuthorize("#request.traineeUsername == authentication.name or #request.trainerUsername == authentication.name")
    public ResponseEntity<Void> deleteTraining(@Valid @RequestBody DeleteTrainingRequest request) {
        gymFacade.deleteTraining(request.traineeUsername(), request.trainerUsername(), request.trainingDate());
        Trainer trainer = gymFacade.getTrainerByUsername(request.trainerUsername());
        trainerWorkload
                .processTrainerRequest(
                    new TrainerWorkloadWebRequest(request.trainerUsername(),
                            trainer.getFirstName(),
                            trainer.getLastName(),
                            trainer.getActive(),
                            request.trainingDate(),
                            request.trainingDurationMin(),
                            ActionType.DELETE));
        return ResponseEntity.ok().build();
    }
}
