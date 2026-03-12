package com.epam.integration.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.epam.application.facade.GymFacadeImpl;
import com.epam.application.request.CreateTraineeProfileRequest;
import com.epam.application.request.CreateTrainerProfileRequest;
import com.epam.application.request.CreateTrainingRequest;
import com.epam.domain.TrainingFilter;
import com.epam.domain.model.Trainee;
import com.epam.domain.model.Trainer;
import com.epam.domain.model.Training;
import com.epam.domain.model.TrainingTypeEnum;
import com.epam.integration.base.TransactionalTestBase;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class TrainingSessionIntegrationTest extends TransactionalTestBase {

    @Autowired
    private GymFacadeImpl gymFacade;

    @Autowired
    private EntityManager entityManager;

    @Test
    void createTraining_persistsSessionWithCorrectAssociations() {
        Trainer trainer = gymFacade.createTrainerProfile(
                new CreateTrainerProfileRequest("John", "Trainer", true, TrainingTypeEnum.YOGA));
        Trainee trainee = gymFacade.createTraineeProfile(
                new CreateTraineeProfileRequest("Jane", "Trainee", true, Optional.empty(), Optional.empty()));

        gymFacade.createTraining(new CreateTrainingRequest(
                "Morning Cardio",
                LocalDateTime.now(),
                60,
                Optional.of(TrainingTypeEnum.YOGA),
                trainee.getUsername(),
                trainer.getUsername()));

        List<Training> trainings = gymFacade.getTraineeTrainings(trainee.getUsername(), TrainingFilter.empty());
        assertThat(trainings).hasSize(1);

        Training saved = trainings.get(0);
        assertThat(saved.getTrainingName()).isEqualTo("Morning Cardio");
        assertThat(saved.getTrainee()).isNotNull();
        assertThat(saved.getTrainer()).isNotNull();
        assertThat(saved.getTrainer().getUsername()).isEqualTo(trainer.getUsername());
    }

    @Test
    void createTraining_multipleSessionsForSameTraineePersistIndependently() {
        Trainer trainer = gymFacade.createTrainerProfile(
                new CreateTrainerProfileRequest("Test", "Trainer", true, TrainingTypeEnum.YOGA));
        Trainee trainee = gymFacade.createTraineeProfile(
                new CreateTraineeProfileRequest("Test", "Trainee", true, Optional.empty(), Optional.empty()));

        gymFacade.createTraining(new CreateTrainingRequest(
                "Session One",
                LocalDateTime.now(),
                60,
                Optional.of(TrainingTypeEnum.YOGA),
                trainee.getUsername(),
                trainer.getUsername()));

        gymFacade.createTraining(new CreateTrainingRequest(
                "Session Two",
                LocalDateTime.now().plusDays(1),
                45,
                Optional.of(TrainingTypeEnum.YOGA),
                trainee.getUsername(),
                trainer.getUsername()));

        List<Training> trainings = gymFacade.getTraineeTrainings(trainee.getUsername(), TrainingFilter.empty());
        assertThat(trainings).hasSize(2);
        assertThat(trainings)
                .extracting(Training::getTrainingName)
                .containsExactlyInAnyOrder("Session One", "Session Two");
    }

    @Test
    void deleteTrainee_removesAllAssociatedTrainingSessions() {
        Trainer trainer = gymFacade.createTrainerProfile(
                new CreateTrainerProfileRequest("Shared", "Trainer", true, TrainingTypeEnum.CARDIO));
        Trainee trainee = gymFacade.createTraineeProfile(
                new CreateTraineeProfileRequest("Test", "Trainee", true, Optional.empty(), Optional.empty()));

        gymFacade.createTraining(new CreateTrainingRequest(
                "Training 1",
                LocalDateTime.now(),
                60,
                Optional.of(TrainingTypeEnum.CARDIO),
                trainee.getUsername(),
                trainer.getUsername()));
        gymFacade.createTraining(new CreateTrainingRequest(
                "Training 2",
                LocalDateTime.now().plusDays(1),
                45,
                Optional.of(TrainingTypeEnum.CARDIO),
                trainee.getUsername(),
                trainer.getUsername()));

        gymFacade.deleteTraineeProfile(trainee.getUsername());

        Long remaining = entityManager
                .createQuery("SELECT COUNT(t) FROM TrainingDAO t", Long.class)
                .getSingleResult();
        assertThat(remaining).isZero();
    }

    @Test
    void deleteTrainee_leavesTrainerAndTheirOtherSessionsIntact() {
        Trainer trainer = gymFacade.createTrainerProfile(
                new CreateTrainerProfileRequest("Shared", "Trainer", true, TrainingTypeEnum.CARDIO));

        Trainee traineeToDelete = gymFacade.createTraineeProfile(
                new CreateTraineeProfileRequest("Delete", "Me", true, Optional.empty(), Optional.empty()));
        Trainee otherTrainee = gymFacade.createTraineeProfile(
                new CreateTraineeProfileRequest("Keep", "Me", true, Optional.empty(), Optional.empty()));

        gymFacade.createTraining(new CreateTrainingRequest(
                "Doomed Session",
                LocalDateTime.now(),
                60,
                Optional.of(TrainingTypeEnum.CARDIO),
                traineeToDelete.getUsername(),
                trainer.getUsername()));
        gymFacade.createTraining(new CreateTrainingRequest(
                "Surviving Session",
                LocalDateTime.now().plusDays(1),
                45,
                Optional.of(TrainingTypeEnum.CARDIO),
                otherTrainee.getUsername(),
                trainer.getUsername()));

        gymFacade.deleteTraineeProfile(traineeToDelete.getUsername());

        assertThat(gymFacade.getTrainerByUsername(trainer.getUsername())).isNotNull();

        List<Training> trainerSessions = gymFacade.getTrainerTrainings(
                trainer.getUsername(), TrainingFilter.forTrainer(Optional.empty(), Optional.empty(), Optional.empty()));
        assertThat(trainerSessions)
                .hasSize(1)
                .extracting(Training::getTrainingName)
                .containsExactly("Surviving Session");
    }
}
