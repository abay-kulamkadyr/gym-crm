package com.epam.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.Optional;

import com.epam.application.facade.GymFacadeImpl;
import com.epam.application.request.CreateTraineeProfileRequest;
import com.epam.application.request.UpdateTraineeProfileRequest;
import com.epam.domain.model.Trainee;
import com.epam.infrastructure.persistence.dao.TraineeDAO;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class TraineeProfileIntegrationTest extends GymIntegrationTestBase {

    @Autowired
    private GymFacadeImpl gymFacade;

    // --- Registration ---

    @Test
    void createTrainee_generatesUsernameAndPassword() {
        CreateTraineeProfileRequest request =
                new CreateTraineeProfileRequest("Michael", "Brown", true, Optional.empty(), Optional.empty());

        Trainee created = gymFacade.createTraineeProfile(request);

        assertThat(created.getTraineeId()).isNotNull();
        assertThat(created.getUsername()).isEqualTo("Michael.Brown");
        assertThat(created.getPassword()).isNotNull().hasSize(10);
    }

    @Test
    void createTrainee_persistedAndRetrievable() {
        CreateTraineeProfileRequest request =
                new CreateTraineeProfileRequest("Michael", "Brown", true, Optional.empty(), Optional.empty());

        Trainee created = gymFacade.createTraineeProfile(request);
        Trainee retrieved = gymFacade.getTraineeByUsername(created.getUsername());

        assertThat(retrieved.getUsername()).isEqualTo("Michael.Brown");
    }

    @Test
    void createTrainee_withDuplicateName_appendsNumericSuffix() {
        CreateTraineeProfileRequest request1 =
                new CreateTraineeProfileRequest("Duplicate", "Name", true, Optional.empty(), Optional.empty());
        CreateTraineeProfileRequest request2 =
                new CreateTraineeProfileRequest("Duplicate", "Name", true, Optional.empty(), Optional.empty());

        Trainee first = gymFacade.createTraineeProfile(request1);
        Trainee second = gymFacade.createTraineeProfile(request2);

        assertThat(first.getUsername()).isEqualTo("Duplicate.Name");
        assertThat(second.getUsername()).isEqualTo("Duplicate.Name1");
    }

    // --- Update ---

    @Test
    void updateTrainee_updatesAddressAndDob() {
        Trainee trainee = gymFacade.createTraineeProfile(new CreateTraineeProfileRequest(
                "Sarah", "Wilson", true, Optional.of(LocalDate.of(1990, 1, 1)), Optional.empty()));

        UpdateTraineeProfileRequest update = new UpdateTraineeProfileRequest(
                trainee.getUsername(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(LocalDate.of(1991, 2, 2)),
                Optional.of("999 New Address"));

        Trainee updated = gymFacade.updateTraineeProfile(update);

        assertThat(updated.getAddress()).isEqualTo("999 New Address");
        assertThat(updated.getDob()).isEqualTo(LocalDate.of(1991, 2, 2));
    }

    @Test
    void updateTrainee_changesAreVisibleOnSubsequentRead() {
        Trainee trainee = gymFacade.createTraineeProfile(new CreateTraineeProfileRequest(
                "Sarah", "Wilson", true, Optional.of(LocalDate.of(1990, 1, 1)), Optional.empty()));

        gymFacade.updateTraineeProfile(new UpdateTraineeProfileRequest(
                trainee.getUsername(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of("Updated Address")));

        Trainee retrieved = gymFacade.getTraineeByUsername(trainee.getUsername());
        assertThat(retrieved.getAddress()).isEqualTo("Updated Address");
    }

    // --- Delete ---

    @Test
    void deleteTrainee_removesRecordFromDatabase() {
        Trainee trainee = gymFacade.createTraineeProfile(
                new CreateTraineeProfileRequest("Tom", "Davis", true, Optional.empty(), Optional.empty()));

        assertThat(entityManager.find(TraineeDAO.class, trainee.getTraineeId())).isNotNull();

        gymFacade.deleteTraineeProfile(trainee.getUsername());

        assertThat(entityManager.find(TraineeDAO.class, trainee.getTraineeId())).isNull();
    }

    @Test
    void deleteTrainee_doesNotAffectUnrelatedTrainers() {
        var yoga = requireTrainingType(com.epam.domain.model.TrainingTypeEnum.YOGA);

        var trainer = gymFacade.createTrainerProfile(new com.epam.application.request.CreateTrainerProfileRequest(
                "Shared", "Trainer", true, yoga.getTrainingTypeName()));

        Trainee trainee = gymFacade.createTraineeProfile(
                new CreateTraineeProfileRequest("Test", "Trainee", true, Optional.empty(), Optional.empty()));

        gymFacade.createTraining(new com.epam.application.request.CreateTrainingRequest(
                "Session",
                java.time.LocalDateTime.now(),
                60,
                Optional.of(yoga.getTrainingTypeName()),
                trainee.getUsername(),
                trainer.getUsername()));

        gymFacade.deleteTraineeProfile(trainee.getUsername());

        assertThat(gymFacade.getTrainerByUsername(trainer.getUsername())).isNotNull();
    }

    // --- Active status ---

    @Test
    void toggleTraineeActiveStatus_flipsFromTrueToFalse() {
        Trainee trainee = gymFacade.createTraineeProfile(
                new CreateTraineeProfileRequest("Test", "User", true, Optional.empty(), Optional.empty()));

        assertThat(trainee.getActive()).isTrue();

        gymFacade.toggleTraineeActiveStatus(trainee.getUsername());

        assertThat(gymFacade.getTraineeByUsername(trainee.getUsername()).getActive())
                .isFalse();
    }

    @Test
    void toggleTraineeActiveStatus_flipsFromFalseToTrue() {
        Trainee trainee = gymFacade.createTraineeProfile(
                new CreateTraineeProfileRequest("Test", "User", false, Optional.empty(), Optional.empty()));

        gymFacade.toggleTraineeActiveStatus(trainee.getUsername());

        assertThat(gymFacade.getTraineeByUsername(trainee.getUsername()).getActive())
                .isTrue();
    }

    // --- Error cases ---

    @Test
    void getTraineeByUsername_throwsForUnknownUsername() {
        assertThatThrownBy(() -> gymFacade.getTraineeByUsername("nonexistent.user"))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
