package com.epam.integration.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Optional;

import com.epam.application.exception.EntityNotFoundException;
import com.epam.application.facade.GymFacadeImpl;
import com.epam.application.request.CreateTrainerProfileRequest;
import com.epam.application.request.UpdateTrainerProfileRequest;
import com.epam.domain.model.Trainer;
import com.epam.domain.model.TrainingTypeEnum;
import com.epam.infrastructure.persistence.dao.TrainerDAO;
import com.epam.integration.base.TransactionalTestBase;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class TrainerProfileIntegrationTest extends TransactionalTestBase {

    @Autowired
    private GymFacadeImpl gymFacade;

    @Autowired
    private EntityManager entityManager;

    @Test
    void createTrainer_generatesUsernameAndPassword() {

        Trainer trainer = gymFacade.createTrainerProfile(
                new CreateTrainerProfileRequest("Emma", "Taylor", true, TrainingTypeEnum.CARDIO));

        assertThat(trainer.getTrainerId()).isNotNull();
        assertThat(trainer.getUsername()).isEqualTo("Emma.Taylor");
        assertThat(trainer.getPassword()).isNotNull().hasSize(10);
    }

    @Test
    void createTrainer_persistedAndRetrievable() {

        Trainer trainer = gymFacade.createTrainerProfile(
                new CreateTrainerProfileRequest("Emma", "Taylor", true, TrainingTypeEnum.CARDIO));

        Trainer retrieved = gymFacade.getTrainerByUsername(trainer.getUsername());
        assertThat(retrieved.getUsername()).isEqualTo("Emma.Taylor");
    }

    @Test
    void createTrainer_withDuplicateName_appendsNumericSuffix() {

        Trainer first = gymFacade.createTrainerProfile(
                new CreateTrainerProfileRequest("Same", "Person", true, TrainingTypeEnum.CROSSFIT));
        Trainer second = gymFacade.createTrainerProfile(
                new CreateTrainerProfileRequest("Same", "Person", true, TrainingTypeEnum.CROSSFIT));

        assertThat(first.getUsername()).isEqualTo("Same.Person");
        assertThat(second.getUsername()).isEqualTo("Same.Person1");
    }

    @Test
    void updateTrainer_updatesActiveStatusAndSpecialization() {

        Trainer trainer = gymFacade.createTrainerProfile(
                new CreateTrainerProfileRequest("David", "Martinez", true, TrainingTypeEnum.CROSSFIT));

        Trainer updated = gymFacade.updateTrainerProfile(new UpdateTrainerProfileRequest(
                trainer.getUsername(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(false),
                Optional.of(TrainingTypeEnum.PILATES)));

        assertThat(updated.getActive()).isFalse();
        assertThat(updated.getSpecialization().getTrainingTypeName()).isEqualTo(TrainingTypeEnum.PILATES);
    }

    @Test
    void updateTrainer_changesAreVisibleOnSubsequentRead() {

        Trainer trainer = gymFacade.createTrainerProfile(
                new CreateTrainerProfileRequest("David", "Martinez", true, TrainingTypeEnum.CROSSFIT));

        gymFacade.updateTrainerProfile(new UpdateTrainerProfileRequest(
                trainer.getUsername(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(TrainingTypeEnum.PILATES)));

        Trainer retrieved = gymFacade.getTrainerByUsername(trainer.getUsername());
        assertThat(retrieved.getSpecialization().getTrainingTypeName()).isEqualTo(TrainingTypeEnum.PILATES);
    }

    @Test
    void deleteTrainer_removesRecordFromDatabase() {

        Trainer trainer = gymFacade.createTrainerProfile(
                new CreateTrainerProfileRequest("Henry", "Cavill", true, TrainingTypeEnum.CARDIO));

        assertThat(entityManager.find(TrainerDAO.class, trainer.getTrainerId())).isNotNull();

        gymFacade.deleteTrainerProfile(trainer.getUsername());

        assertThat(entityManager.find(TrainerDAO.class, trainer.getTrainerId())).isNull();
    }

    @Test
    void toggleTrainerActiveStatus_flipsFromTrueToFalse() {

        Trainer trainer = gymFacade.createTrainerProfile(
                new CreateTrainerProfileRequest("Test", "Trainer", true, TrainingTypeEnum.YOGA));

        assertThat(trainer.getActive()).isTrue();

        gymFacade.toggleTrainerActiveStatus(trainer.getUsername());

        assertThat(gymFacade.getTrainerByUsername(trainer.getUsername()).getActive())
                .isFalse();
    }

    @Test
    void toggleTrainerActiveStatus_flipsFromFalseToTrue() {

        Trainer trainer = gymFacade.createTrainerProfile(
                new CreateTrainerProfileRequest("Test", "Trainer", false, TrainingTypeEnum.YOGA));

        gymFacade.toggleTrainerActiveStatus(trainer.getUsername());

        assertThat(gymFacade.getTrainerByUsername(trainer.getUsername()).getActive())
                .isTrue();
    }

    @Test
    void getTrainerByUsername_throwsForUnknownUsername() {
        assertThatThrownBy(() -> gymFacade.getTrainerByUsername("nonexistent.trainer"))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
