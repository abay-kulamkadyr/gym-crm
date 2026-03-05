package com.epam.integration;

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
import com.epam.domain.model.TrainingType;
import com.epam.domain.model.TrainingTypeEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Tests for getTraineeTrainings and getTrainerTrainings filter combinations.
 *
 * Each test builds its own minimal fixture rather than sharing state, so filter
 * assertions are not contaminated by data from other tests.
 */
class TrainingQueryIntegrationTest extends GymIntegrationTestBase {

    @Autowired
    private GymFacadeImpl gymFacade;

    // Shared fixtures created fresh per test via @BeforeEach in GymIntegrationTestBase
    private Trainee trainee;
    private Trainer trainer;
    private TrainingType yoga;
    private TrainingType boxing;

    @BeforeEach
    void seedFixtures() {
        // GymIntegrationTestBase.cleanDatabase() runs first (ordered by @BeforeEach declaration)
        yoga = requireTrainingType(TrainingTypeEnum.YOGA);
        boxing = requireTrainingType(TrainingTypeEnum.BOXING);

        trainer = gymFacade.createTrainerProfile(
                new CreateTrainerProfileRequest("Jane", "Trainer", true, TrainingTypeEnum.YOGA));
        trainee = gymFacade.createTraineeProfile(
                new CreateTraineeProfileRequest("John", "Trainee", true, Optional.empty(), Optional.empty()));
    }

    // -------------------------------------------------------------------------
    // getTraineeTrainings
    // -------------------------------------------------------------------------

    @Test
    void getTraineeTrainings_noFilter_returnsAllSessions() {
        createSession("Morning Yoga", LocalDateTime.now().minusDays(1), 60, yoga);
        createSession("Evening Boxing", LocalDateTime.now().minusDays(10), 45, boxing);

        List<Training> result = gymFacade.getTraineeTrainings(trainee.getUsername(), TrainingFilter.empty());

        assertThat(result).hasSize(2);
    }

    @Test
    void getTraineeTrainings_dateRangeFilter_excludesSessionsOutsideRange() {
        createSession("Recent Yoga", LocalDateTime.now().minusDays(1), 60, yoga);
        createSession("Old Boxing", LocalDateTime.now().minusDays(10), 45, boxing);

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

        createSession("My Trainer Session", LocalDateTime.now().minusDays(1), 60, yoga);
        createSessionWith("Other Session", LocalDateTime.now().minusDays(2), 45, yoga, otherTrainer);

        TrainingFilter filter = TrainingFilter.forTrainee(
                Optional.empty(), Optional.empty(), Optional.of(trainer.getUsername()), Optional.empty());

        List<Training> result = gymFacade.getTraineeTrainings(trainee.getUsername(), filter);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTrainer().getUsername()).isEqualTo(trainer.getUsername());
    }

    @Test
    void getTraineeTrainings_trainingTypeFilter_returnsOnlyMatchingType() {
        createSession("Morning Yoga", LocalDateTime.now().minusDays(1), 60, yoga);
        createSession("Evening Boxing", LocalDateTime.now().minusDays(10), 45, boxing);

        TrainingFilter filter = TrainingFilter.forTrainee(
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(yoga.getTrainingTypeName()));

        List<Training> result = gymFacade.getTraineeTrainings(trainee.getUsername(), filter);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTrainingType().getTrainingTypeName()).isEqualTo(TrainingTypeEnum.YOGA);
    }

    @Test
    void getTraineeTrainings_allFiltersApplied_returnsOnlyExactMatch() {
        createSession("Morning Yoga", LocalDateTime.now().minusDays(1), 60, yoga);
        createSession("Old Boxing", LocalDateTime.now().minusDays(10), 45, boxing);

        TrainingFilter filter = TrainingFilter.forTrainee(
                Optional.of(LocalDateTime.now().minusDays(2)),
                Optional.of(LocalDateTime.now()),
                Optional.of(trainer.getUsername()),
                Optional.of(yoga.getTrainingTypeName()));

        List<Training> result = gymFacade.getTraineeTrainings(trainee.getUsername(), filter);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTrainingName()).isEqualTo("Morning Yoga");
    }

    @Test
    void getTraineeTrainings_filterMatchesNothing_returnsEmptyList() {
        createSession("Morning Yoga", LocalDateTime.now().minusDays(1), 60, yoga);

        TrainingFilter filter = TrainingFilter.forTrainee(
                Optional.of(LocalDateTime.now().minusDays(100)),
                Optional.of(LocalDateTime.now().minusDays(90)),
                Optional.empty(),
                Optional.empty());

        List<Training> result = gymFacade.getTraineeTrainings(trainee.getUsername(), filter);

        assertThat(result).isEmpty();
    }

    // -------------------------------------------------------------------------
    // getTrainerTrainings
    // -------------------------------------------------------------------------

    @Test
    void getTrainerTrainings_noFilter_returnsAllSessions() {
        Trainee trainee2 = gymFacade.createTraineeProfile(
                new CreateTraineeProfileRequest("Jane", "Smith", true, Optional.empty(), Optional.empty()));

        createSession("Morning Session", LocalDateTime.now().minusDays(1), 60, yoga);
        createSessionWith("Evening Session", LocalDateTime.now().minusDays(2), 45, yoga, trainer, trainee2);

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

        createSession("Trainee1 Session", LocalDateTime.now().minusDays(1), 60, yoga);
        createSessionWith("Trainee2 Session", LocalDateTime.now().minusDays(2), 45, yoga, trainer, trainee2);

        TrainingFilter filter =
                TrainingFilter.forTrainer(Optional.empty(), Optional.empty(), Optional.of(trainee.getUsername()));

        List<Training> result = gymFacade.getTrainerTrainings(trainer.getUsername(), filter);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTrainee().getUsername()).isEqualTo(trainee.getUsername());
    }

    @Test
    void getTrainerTrainings_dateRangeFilter_excludesSessionsOutsideRange() {
        createSession("Recent Session", LocalDateTime.now().minusDays(1), 60, yoga);
        createSession("Old Session", LocalDateTime.now().minusDays(20), 45, yoga);

        TrainingFilter filter = TrainingFilter.forTrainer(
                Optional.of(LocalDateTime.now().minusDays(7)), Optional.of(LocalDateTime.now()), Optional.empty());

        List<Training> result = gymFacade.getTrainerTrainings(trainer.getUsername(), filter);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTrainingName()).isEqualTo("Recent Session");
    }

    // -------------------------------------------------------------------------
    // helpers
    // -------------------------------------------------------------------------

    /** Creates a session for the default trainee/trainer pair with the given type. */
    private void createSession(String name, LocalDateTime date, int durationMin, TrainingType type) {
        gymFacade.createTraining(new CreateTrainingRequest(
                name,
                date,
                durationMin,
                Optional.of(type.getTrainingTypeName()),
                trainee.getUsername(),
                trainer.getUsername()));
    }

    /** Creates a session for the default trainee but a custom trainer. */
    private void createSessionWith(
            String name, LocalDateTime date, int durationMin, TrainingType type, Trainer customTrainer) {
        gymFacade.createTraining(new CreateTrainingRequest(
                name,
                date,
                durationMin,
                Optional.of(type.getTrainingTypeName()),
                trainee.getUsername(),
                customTrainer.getUsername()));
    }

    /** Creates a session for a custom trainer and a custom trainee. */
    private void createSessionWith(
            String name,
            LocalDateTime date,
            int durationMin,
            TrainingType type,
            Trainer customTrainer,
            Trainee customTrainee) {
        gymFacade.createTraining(new CreateTrainingRequest(
                name,
                date,
                durationMin,
                Optional.of(type.getTrainingTypeName()),
                customTrainee.getUsername(),
                customTrainer.getUsername()));
    }
}
