package com.epam.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.epam.application.facade.GymFacadeImpl;
import com.epam.application.request.CreateTraineeProfileRequest;
import com.epam.application.request.CreateTrainerProfileRequest;
import com.epam.application.request.CreateTrainingRequest;
import com.epam.domain.model.Trainee;
import com.epam.domain.model.Trainer;
import com.epam.domain.model.TrainingType;
import com.epam.domain.model.TrainingTypeEnum;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class TrainerAssignmentIntegrationTest extends GymIntegrationTestBase {

    @Autowired
    private GymFacadeImpl gymFacade;

    // -------------------------------------------------------------------------
    // updateTraineeTrainersList
    // -------------------------------------------------------------------------

    @Test
    void updateTraineeTrainersList_replacesAssignedTrainers() {
        requireTrainingType(TrainingTypeEnum.YOGA);
        requireTrainingType(TrainingTypeEnum.BOXING);

        Trainee trainee = createTrainee("John", "Doe");
        Trainer trainer1 = createTrainer("Alice", "Smith", TrainingTypeEnum.YOGA);
        Trainer trainer2 = createTrainer("Elon", "Musk", TrainingTypeEnum.BOXING);

        gymFacade.updateTraineeTrainersList(
                trainee.getUsername(), List.of(trainer1.getUsername(), trainer2.getUsername()));

        List<Trainer> assigned = gymFacade.getTraineeTrainers(trainee.getUsername());
        assertThat(assigned)
                .extracting(Trainer::getUsername)
                .containsExactlyInAnyOrder(trainer1.getUsername(), trainer2.getUsername());
    }

    // -------------------------------------------------------------------------
    // getTraineeUnassignedTrainers
    // -------------------------------------------------------------------------

    @Test
    void getTraineeUnassignedTrainers_returnsAllTrainers_whenNoneAssigned() {
        requireTrainingType(TrainingTypeEnum.YOGA);
        requireTrainingType(TrainingTypeEnum.BOXING);
        requireTrainingType(TrainingTypeEnum.CARDIO);

        Trainer t1 = createTrainer("Alice", "Smith", TrainingTypeEnum.YOGA);
        Trainer t2 = createTrainer("Bob", "Jones", TrainingTypeEnum.BOXING);
        Trainer t3 = createTrainer("Carol", "White", TrainingTypeEnum.CARDIO);
        Trainee trainee = createTrainee("Mike", "Taylor");

        List<Trainer> unassigned = gymFacade.getTraineeUnassignedTrainers(trainee.getUsername());

        assertThat(unassigned)
                .extracting(Trainer::getUsername)
                .containsExactlyInAnyOrder(t1.getUsername(), t2.getUsername(), t3.getUsername());
    }

    @Test
    void getTraineeUnassignedTrainers_excludesTrainerAssignedViaUpdateList() {
        requireTrainingType(TrainingTypeEnum.YOGA);
        requireTrainingType(TrainingTypeEnum.BOXING);

        Trainee trainee = createTrainee("John", "Doe");
        Trainer assigned = createTrainer("Alice", "Smith", TrainingTypeEnum.YOGA);
        Trainer free = createTrainer("Bob", "Jones", TrainingTypeEnum.BOXING);

        gymFacade.updateTraineeTrainersList(trainee.getUsername(), List.of(assigned.getUsername()));

        List<Trainer> unassigned = gymFacade.getTraineeUnassignedTrainers(trainee.getUsername());

        assertThat(unassigned).extracting(Trainer::getUsername).containsExactly(free.getUsername());
    }

    @Test
    void getTraineeUnassignedTrainers_excludesTrainerAssignedViaTrainingSession() {
        TrainingType yoga = requireTrainingType(TrainingTypeEnum.YOGA);
        requireTrainingType(TrainingTypeEnum.BOXING);
        requireTrainingType(TrainingTypeEnum.CARDIO);

        Trainee trainee = createTrainee("John", "Doe");
        Trainer assigned = createTrainer("Alice", "Smith", TrainingTypeEnum.YOGA);
        Trainer free1 = createTrainer("Bob", "Jones", TrainingTypeEnum.BOXING);
        Trainer free2 = createTrainer("Carol", "White", TrainingTypeEnum.CARDIO);

        gymFacade.createTraining(new CreateTrainingRequest(
                "Morning Yoga",
                LocalDateTime.now().minusDays(1),
                60,
                Optional.of(yoga.getTrainingTypeName()),
                trainee.getUsername(),
                assigned.getUsername()));

        List<Trainer> unassigned = gymFacade.getTraineeUnassignedTrainers(trainee.getUsername());

        assertThat(unassigned)
                .hasSize(2)
                .extracting(Trainer::getUsername)
                .containsExactlyInAnyOrder(free1.getUsername(), free2.getUsername());
    }

    @Test
    void getTraineeUnassignedTrainers_returnsEmpty_whenAllAssigned() {
        requireTrainingType(TrainingTypeEnum.YOGA);
        requireTrainingType(TrainingTypeEnum.BOXING);

        Trainee trainee = createTrainee("John", "Doe");
        Trainer t1 = createTrainer("Alice", "Smith", TrainingTypeEnum.YOGA);
        Trainer t2 = createTrainer("Bob", "Jones", TrainingTypeEnum.BOXING);

        gymFacade.updateTraineeTrainersList(trainee.getUsername(), List.of(t1.getUsername(), t2.getUsername()));

        assertThat(gymFacade.getTraineeUnassignedTrainers(trainee.getUsername()))
                .isEmpty();
    }

    // -------------------------------------------------------------------------
    // getTraineeTrainers
    // -------------------------------------------------------------------------

    @Test
    void getTraineeTrainers_returnsAllAssignedTrainers() {
        requireTrainingType(TrainingTypeEnum.YOGA);
        requireTrainingType(TrainingTypeEnum.BOXING);

        Trainee trainee = createTrainee("John", "Doe");
        Trainer t1 = createTrainer("Alice", "Smith", TrainingTypeEnum.YOGA);
        Trainer t2 = createTrainer("Bob", "Jones", TrainingTypeEnum.BOXING);

        gymFacade.updateTraineeTrainersList(trainee.getUsername(), List.of(t1.getUsername(), t2.getUsername()));

        List<Trainer> trainers = gymFacade.getTraineeTrainers(trainee.getUsername());
        assertThat(trainers)
                .extracting(Trainer::getUsername)
                .containsExactlyInAnyOrder(t1.getUsername(), t2.getUsername());
    }

    @Test
    void getTraineeTrainers_returnsEmpty_whenNoTrainersAssigned() {
        Trainee trainee = createTrainee("John", "Doe");

        assertThat(gymFacade.getTraineeTrainers(trainee.getUsername())).isEmpty();
    }

    @Test
    void getTraineeTrainers_includesTrainerAssignedViaTrainingSession() {
        TrainingType yoga = requireTrainingType(TrainingTypeEnum.YOGA);
        Trainee trainee = createTrainee("John", "Doe");
        Trainer trainer = createTrainer("Alice", "Smith", TrainingTypeEnum.YOGA);

        gymFacade.createTraining(new CreateTrainingRequest(
                "Morning Yoga",
                LocalDateTime.now(),
                60,
                Optional.of(yoga.getTrainingTypeName()),
                trainee.getUsername(),
                trainer.getUsername()));

        List<Trainer> trainers = gymFacade.getTraineeTrainers(trainee.getUsername());
        assertThat(trainers).hasSize(1);
        assertThat(trainers.get(0).getUsername()).isEqualTo(trainer.getUsername());
    }

    @Test
    void getTraineeTrainers_throwsForUnknownTrainee() {
        assertThatThrownBy(() -> gymFacade.getTraineeTrainers("nonexistent.user"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // -------------------------------------------------------------------------
    // getTrainerTrainees
    // -------------------------------------------------------------------------

    @Test
    void getTrainerTrainees_returnsAllAssignedTrainees() {
        TrainingType yoga = requireTrainingType(TrainingTypeEnum.YOGA);
        Trainer trainer = createTrainer("Jane", "Trainer", TrainingTypeEnum.YOGA);
        Trainee trainee1 = createTrainee("John", "Doe");
        Trainee trainee2 = createTrainee("Jane", "Smith");

        gymFacade.createTraining(new CreateTrainingRequest(
                "Morning",
                LocalDateTime.now(),
                60,
                Optional.of(yoga.getTrainingTypeName()),
                trainee1.getUsername(),
                trainer.getUsername()));
        gymFacade.createTraining(new CreateTrainingRequest(
                "Evening",
                LocalDateTime.now().plusDays(1),
                45,
                Optional.of(yoga.getTrainingTypeName()),
                trainee2.getUsername(),
                trainer.getUsername()));

        List<Trainee> trainees = gymFacade.getTrainerTrainees(trainer.getUsername());
        assertThat(trainees)
                .extracting(Trainee::getUsername)
                .containsExactlyInAnyOrder(trainee1.getUsername(), trainee2.getUsername());
    }

    @Test
    void getTrainerTrainees_returnsEmpty_whenNoTraineesAssigned() {
        requireTrainingType(TrainingTypeEnum.YOGA);
        Trainer trainer = createTrainer("Jane", "Trainer", TrainingTypeEnum.YOGA);

        assertThat(gymFacade.getTrainerTrainees(trainer.getUsername())).isEmpty();
    }

    @Test
    void getTrainerTrainees_deduplicatesTrainee_withMultipleSessions() {
        TrainingType yoga = requireTrainingType(TrainingTypeEnum.YOGA);
        Trainer trainer = createTrainer("Jane", "Trainer", TrainingTypeEnum.YOGA);
        Trainee trainee = createTrainee("John", "Doe");

        gymFacade.createTraining(new CreateTrainingRequest(
                "Session 1",
                LocalDateTime.now(),
                60,
                Optional.of(yoga.getTrainingTypeName()),
                trainee.getUsername(),
                trainer.getUsername()));
        gymFacade.createTraining(new CreateTrainingRequest(
                "Session 2",
                LocalDateTime.now().plusDays(1),
                45,
                Optional.of(yoga.getTrainingTypeName()),
                trainee.getUsername(),
                trainer.getUsername()));

        List<Trainee> trainees = gymFacade.getTrainerTrainees(trainer.getUsername());

        assertThat(trainees).hasSize(1);
        assertThat(trainees.get(0).getUsername()).isEqualTo(trainee.getUsername());
    }

    @Test
    void getTrainerTrainees_throwsForUnknownTrainer() {
        assertThatThrownBy(() -> gymFacade.getTrainerTrainees("nonexistent.trainer"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // -------------------------------------------------------------------------
    // helpers
    // -------------------------------------------------------------------------

    private Trainee createTrainee(String first, String last) {
        return gymFacade.createTraineeProfile(
                new CreateTraineeProfileRequest(first, last, true, Optional.empty(), Optional.empty()));
    }

    private Trainer createTrainer(String first, String last, TrainingTypeEnum type) {
        return gymFacade.createTrainerProfile(new CreateTrainerProfileRequest(first, last, true, type));
    }
}
