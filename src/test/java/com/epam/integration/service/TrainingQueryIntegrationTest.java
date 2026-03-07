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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class TrainingQueryIntegrationTest extends TransactionalTestBase {

    @Autowired
    private GymFacadeImpl gymFacade;

    private Trainee trainee;
    private Trainer trainer;

    @BeforeEach
    void seedFixtures() {

        trainer = gymFacade.createTrainerProfile(
                new CreateTrainerProfileRequest("Jane", "Trainer", true, TrainingTypeEnum.YOGA));
        trainee = gymFacade.createTraineeProfile(
                new CreateTraineeProfileRequest("John", "Trainee", true, Optional.empty(), Optional.empty()));
    }

    @Test
    void getTraineeTrainings_noFilter_returnsAllSessions() {
        createTrainingSession("Morning Yoga", LocalDateTime.now().minusDays(1), 60, TrainingTypeEnum.YOGA);
        createTrainingSession("Evening Boxing", LocalDateTime.now().minusDays(10), 45, TrainingTypeEnum.BOXING);

        List<Training> result = gymFacade.getTraineeTrainings(trainee.getUsername(), TrainingFilter.empty());

        assertThat(result).hasSize(2);
    }

    @Test
    void getTraineeTrainings_dateRangeFilter_excludesSessionsOutsideRange() {
        createTrainingSession("Recent Yoga", LocalDateTime.now().minusDays(1), 60, TrainingTypeEnum.YOGA);
        createTrainingSession("Old Boxing", LocalDateTime.now().minusDays(10), 45, TrainingTypeEnum.BOXING);

        TrainingFilter filter = TrainingFilter.forTrainee(
                Optional.of(LocalDateTime.now().minusDays(7)),
                Optional.of(LocalDateTime.now()),
                Optional.empty(),
                Optional.empty());

        List<Training> result = gymFacade.getTraineeTrainings(trainee.getUsername(), filter);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTrainingName()).isEqualTo("Recent Yoga");
    }

    @Test
    void getTraineeTrainings_trainerFilter_returnsOnlyMatchingSessions() {
        Trainer otherTrainer = gymFacade.createTrainerProfile(
                new CreateTrainerProfileRequest("Other", "Trainer", true, TrainingTypeEnum.YOGA));

        createTrainingSession("My Trainer Session", LocalDateTime.now().minusDays(1), 60, TrainingTypeEnum.YOGA);
        createTrainingSessionWith(
                "Other Session", LocalDateTime.now().minusDays(2), 45, TrainingTypeEnum.YOGA, otherTrainer);

        TrainingFilter filter = TrainingFilter.forTrainee(
                Optional.empty(), Optional.empty(), Optional.of(trainer.getUsername()), Optional.empty());

        List<Training> result = gymFacade.getTraineeTrainings(trainee.getUsername(), filter);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTrainer().getUsername()).isEqualTo(trainer.getUsername());
    }

    @Test
    void getTraineeTrainings_trainingTypeFilter_returnsOnlyMatchingType() {
        createTrainingSession("Morning Yoga", LocalDateTime.now().minusDays(1), 60, TrainingTypeEnum.YOGA);
        createTrainingSession("Evening Boxing", LocalDateTime.now().minusDays(10), 45, TrainingTypeEnum.BOXING);

        TrainingFilter filter = TrainingFilter.forTrainee(
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(TrainingTypeEnum.YOGA));

        List<Training> result = gymFacade.getTraineeTrainings(trainee.getUsername(), filter);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTrainingType().getTrainingTypeName()).isEqualTo(TrainingTypeEnum.YOGA);
    }

    @Test
    void getTraineeTrainings_allFiltersApplied_returnsOnlyExactMatch() {
        createTrainingSession("Morning Yoga", LocalDateTime.now().minusDays(1), 60, TrainingTypeEnum.YOGA);
        createTrainingSession("Old Boxing", LocalDateTime.now().minusDays(10), 45, TrainingTypeEnum.BOXING);

        TrainingFilter filter = TrainingFilter.forTrainee(
                Optional.of(LocalDateTime.now().minusDays(2)),
                Optional.of(LocalDateTime.now()),
                Optional.of(trainer.getUsername()),
                Optional.of(TrainingTypeEnum.YOGA));

        List<Training> result = gymFacade.getTraineeTrainings(trainee.getUsername(), filter);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTrainingName()).isEqualTo("Morning Yoga");
    }

    @Test
    void getTraineeTrainings_filterMatchesNothing_returnsEmptyList() {
        createTrainingSession("Morning Yoga", LocalDateTime.now().minusDays(1), 60, TrainingTypeEnum.YOGA);

        TrainingFilter filter = TrainingFilter.forTrainee(
                Optional.of(LocalDateTime.now().minusDays(100)),
                Optional.of(LocalDateTime.now().minusDays(90)),
                Optional.empty(),
                Optional.empty());

        List<Training> result = gymFacade.getTraineeTrainings(trainee.getUsername(), filter);

        assertThat(result).isEmpty();
    }

    @Test
    void getTrainerTrainings_noFilter_returnsAllSessions() {
        Trainee trainee2 = gymFacade.createTraineeProfile(
                new CreateTraineeProfileRequest("Jane", "Smith", true, Optional.empty(), Optional.empty()));

        createTrainingSession("Morning Session", LocalDateTime.now().minusDays(1), 60, TrainingTypeEnum.YOGA);
        createTrainingSessionWith(
                "Evening Session", LocalDateTime.now().minusDays(2), 45, TrainingTypeEnum.YOGA, trainer, trainee2);

        TrainingFilter filter = TrainingFilter.forTrainer(Optional.empty(), Optional.empty(), Optional.empty());

        List<Training> result = gymFacade.getTrainerTrainings(trainer.getUsername(), filter);

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Training::getTrainingName)
                .containsExactlyInAnyOrder("Morning Session", "Evening Session");
    }

    @Test
    void getTrainerTrainings_traineeFilter_returnsOnlyMatchingSessions() {
        Trainee trainee2 = gymFacade.createTraineeProfile(
                new CreateTraineeProfileRequest("Jane", "Smith", true, Optional.empty(), Optional.empty()));

        createTrainingSession("Trainee1 Session", LocalDateTime.now().minusDays(1), 60, TrainingTypeEnum.YOGA);
        createTrainingSessionWith(
                "Trainee2 Session", LocalDateTime.now().minusDays(2), 45, TrainingTypeEnum.YOGA, trainer, trainee2);

        TrainingFilter filter =
                TrainingFilter.forTrainer(Optional.empty(), Optional.empty(), Optional.of(trainee.getUsername()));

        List<Training> result = gymFacade.getTrainerTrainings(trainer.getUsername(), filter);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTrainee().getUsername()).isEqualTo(trainee.getUsername());
    }

    @Test
    void getTrainerTrainings_dateRangeFilter_excludesSessionsOutsideRange() {
        createTrainingSession("Recent Session", LocalDateTime.now().minusDays(1), 60, TrainingTypeEnum.YOGA);
        createTrainingSession("Old Session", LocalDateTime.now().minusDays(20), 45, TrainingTypeEnum.YOGA);

        TrainingFilter filter = TrainingFilter.forTrainer(
                Optional.of(LocalDateTime.now().minusDays(7)), Optional.of(LocalDateTime.now()), Optional.empty());

        List<Training> result = gymFacade.getTrainerTrainings(trainer.getUsername(), filter);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTrainingName()).isEqualTo("Recent Session");
    }

    private void createTrainingSession(String name, LocalDateTime date, int durationMin, TrainingTypeEnum type) {
        gymFacade.createTraining(new CreateTrainingRequest(
                name, date, durationMin, Optional.of(type), trainee.getUsername(), trainer.getUsername()));
    }

    private void createTrainingSessionWith(
            String name, LocalDateTime date, int durationMin, TrainingTypeEnum type, Trainer customTrainer) {
        gymFacade.createTraining(new CreateTrainingRequest(
                name, date, durationMin, Optional.of(type), trainee.getUsername(), customTrainer.getUsername()));
    }

    private void createTrainingSessionWith(
            String name,
            LocalDateTime date,
            int durationMin,
            TrainingTypeEnum type,
            Trainer customTrainer,
            Trainee customTrainee) {
        gymFacade.createTraining(new CreateTrainingRequest(
                name, date, durationMin, Optional.of(type), customTrainee.getUsername(), customTrainer.getUsername()));
    }
}
