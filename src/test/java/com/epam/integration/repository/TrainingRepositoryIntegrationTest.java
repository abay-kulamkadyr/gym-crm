package com.epam.integration.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.epam.domain.TrainingFilter;
import com.epam.domain.model.Training;
import com.epam.domain.model.TrainingTypeEnum;
import com.epam.domain.port.TrainingRepository;
import com.epam.integration.base.SeededIntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class TrainingRepositoryIntegrationTest extends SeededIntegrationTestBase {

    private static final String TRAINEE_USERNAME = "Lisa.Miller";
    private static final String TRAINER_USERNAME = "Michael.Williams";
    private static final LocalDateTime SEEDED_DATE = LocalDateTime.of(2024, 1, 20, 17, 0);

    @Autowired
    private TrainingRepository trainingRepository;

    @Test
    void findById_withExistingTraining_returnsTraining() {
        List<Training> all = trainingRepository.getTraineeTrainings(TRAINEE_USERNAME, emptyTraineeFilter());
        assertThat(all).isNotEmpty();

        Long id = all.get(0).getTrainingId();
        Optional<Training> result = trainingRepository.findById(id);

        assertThat(result).isPresent();
        assertThat(result.get().getTrainingId()).isEqualTo(id);
    }

    @Test
    void findById_withNonExistentId_returnsEmpty() {
        Optional<Training> result = trainingRepository.findById(Long.MAX_VALUE);

        assertThat(result).isEmpty();
    }

    @Test
    void getTraineeTrainings_withFromDateFilter_excludesEarlierTrainings() {
        LocalDateTime boundary = LocalDateTime.of(2024, 7, 1, 0, 0);
        TrainingFilter filter =
                TrainingFilter.forTrainee(Optional.of(boundary), Optional.empty(), Optional.empty(), Optional.empty());

        List<Training> result = trainingRepository.getTraineeTrainings(TRAINEE_USERNAME, filter);

        assertThat(result).allSatisfy(t -> assertThat(t.getTrainingDate()).isAfterOrEqualTo(boundary));
    }

    @Test
    void getTraineeTrainings_withToDateFilter_excludesLaterTrainings() {
        LocalDateTime boundary = LocalDateTime.of(2024, 8, 1, 0, 0);
        TrainingFilter filter =
                TrainingFilter.forTrainee(Optional.empty(), Optional.of(boundary), Optional.empty(), Optional.empty());

        List<Training> result = trainingRepository.getTraineeTrainings(TRAINEE_USERNAME, filter);

        assertThat(result).allSatisfy(t -> assertThat(t.getTrainingDate()).isBeforeOrEqualTo(boundary));
    }

    @Test
    void getTraineeTrainings_withTrainerNameFilter_returnsOnlyThatTrainersSessionss() {
        TrainingFilter filter = TrainingFilter.forTrainee(
                Optional.empty(), Optional.empty(), Optional.of(TRAINER_USERNAME), Optional.empty());

        List<Training> result = trainingRepository.getTraineeTrainings(TRAINEE_USERNAME, filter);

        assertThat(result).isNotEmpty();
        assertThat(result)
                .allSatisfy(t -> assertThat(t.getTrainer().getUsername()).isEqualTo(TRAINER_USERNAME));
    }

    @Test
    void getTraineeTrainings_withTrainingTypeFilter_returnsOnlyMatchingType() {
        TrainingFilter filter = TrainingFilter.forTrainee(
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(TrainingTypeEnum.YOGA));

        List<Training> result = trainingRepository.getTraineeTrainings(TRAINEE_USERNAME, filter);

        result.forEach(
                t -> assertThat(t.getTrainingType().getTrainingTypeName()).isEqualTo(TrainingTypeEnum.YOGA));
    }

    @Test
    void getTraineeTrainings_withNoMatchingFilter_returnsEmptyList() {
        LocalDateTime distantFuture = LocalDateTime.of(2099, 1, 1, 0, 0);
        TrainingFilter filter = TrainingFilter.forTrainee(
                Optional.of(distantFuture), Optional.empty(), Optional.empty(), Optional.empty());

        List<Training> result = trainingRepository.getTraineeTrainings(TRAINEE_USERNAME, filter);

        assertThat(result).isEmpty();
    }

    @Test
    void getTrainerTrainings_withNoFilters_returnsAllForTrainer() {
        TrainingFilter filter = TrainingFilter.forTrainer(Optional.empty(), Optional.empty(), Optional.empty());

        List<Training> result = trainingRepository.getTrainerTrainings(TRAINER_USERNAME, filter);

        assertThat(result).isNotEmpty();
        assertThat(result)
                .allSatisfy(t -> assertThat(t.getTrainer().getUsername()).isEqualTo(TRAINER_USERNAME));
    }

    @Test
    void getTrainerTrainings_withTraineeNameFilter_returnsOnlyThatTraineeSessions() {
        TrainingFilter filter =
                TrainingFilter.forTrainer(Optional.empty(), Optional.empty(), Optional.of(TRAINEE_USERNAME));

        List<Training> result = trainingRepository.getTrainerTrainings(TRAINER_USERNAME, filter);

        assertThat(result).isNotEmpty();
        assertThat(result)
                .allSatisfy(t -> assertThat(t.getTrainee().getUsername()).isEqualTo(TRAINEE_USERNAME));
    }

    @Test
    void getTrainerTrainings_withDateRangeThatMatchesNothing_returnsEmpty() {
        LocalDateTime distantFuture = LocalDateTime.of(2099, 1, 1, 0, 0);
        TrainingFilter filter =
                TrainingFilter.forTrainer(Optional.of(distantFuture), Optional.empty(), Optional.empty());

        List<Training> result = trainingRepository.getTrainerTrainings(TRAINER_USERNAME, filter);

        assertThat(result).isEmpty();
    }

    @Test
    void findByTrainerAndTraineeAndDate_withMatchingRecord_returnsTraining() {
        Optional<Training> result = trainingRepository.findByTrainerUsernameAndTraineeUsernameAndDate(
                TRAINER_USERNAME, TRAINEE_USERNAME, SEEDED_DATE);

        assertThat(result).isPresent();
        assertThat(result.get().getTrainer().getUsername()).isEqualTo(TRAINER_USERNAME);
        assertThat(result.get().getTrainee().getUsername()).isEqualTo(TRAINEE_USERNAME);
    }

    @Test
    void findByTrainerAndTraineeAndDate_withNoMatch_returnsEmpty() {
        Optional<Training> result = trainingRepository.findByTrainerUsernameAndTraineeUsernameAndDate(
                TRAINER_USERNAME, TRAINEE_USERNAME, LocalDateTime.of(2099, 1, 1, 0, 0));

        assertThat(result).isEmpty();
    }

    @Test
    void deleteByTraineeTrainerAndDate_withMatchingRecord_removesIt() {
        // Confirm it exists first
        assertThat(trainingRepository.findByTrainerUsernameAndTraineeUsernameAndDate(
                        TRAINER_USERNAME, TRAINEE_USERNAME, SEEDED_DATE))
                .isPresent();

        trainingRepository.deleteByTraineeTrainerAndDate(TRAINEE_USERNAME, TRAINER_USERNAME, SEEDED_DATE);

        assertThat(trainingRepository.findByTrainerUsernameAndTraineeUsernameAndDate(
                        TRAINER_USERNAME, TRAINEE_USERNAME, SEEDED_DATE))
                .isEmpty();
    }

    @Test
    void deleteByTraineeTrainerAndDate_withNoMatch_doesNotThrow() {
        trainingRepository.deleteByTraineeTrainerAndDate(
                "Ghost.User", "Ghost.Trainer", LocalDateTime.of(2099, 1, 1, 0, 0));
    }

    private TrainingFilter emptyTraineeFilter() {
        return TrainingFilter.forTrainee(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    }
}
