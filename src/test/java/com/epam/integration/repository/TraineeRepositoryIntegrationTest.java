package com.epam.integration.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Optional;

import com.epam.application.exception.EntityNotFoundException;
import com.epam.domain.model.Trainee;
import com.epam.domain.model.Trainer;
import com.epam.domain.port.TraineeRepository;
import com.epam.integration.base.SeededIntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class TraineeRepositoryIntegrationTest extends SeededIntegrationTestBase {

    private static final String EXISTING_TRAINEE = "David.Davis";
    private static final String ASSIGNED_TRAINER = "Emma.Johnson";
    private static final String GHOST_USERNAME = "Ghost.User";

    @Autowired
    private TraineeRepository traineeRepository;

    @Test
    void findById_withExistingId_returnsTrainee() {
        Trainee byUsername = traineeRepository.findByUsername(EXISTING_TRAINEE).orElseThrow();
        Optional<Trainee> result = traineeRepository.findById(byUsername.getTraineeId());

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo(EXISTING_TRAINEE);
    }

    @Test
    void findById_withNonExistentId_returnsEmpty() {
        Optional<Trainee> result = traineeRepository.findById(Long.MAX_VALUE);

        assertThat(result).isEmpty();
    }

    @Test
    void delete_withNonExistentId_throwsEntityNotFoundException() {
        assertThatThrownBy(() -> traineeRepository.delete(Long.MAX_VALUE))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(String.valueOf(Long.MAX_VALUE));
    }

    @Test
    void deleteByUsername_withNonExistentUsername_throwsEntityNotFoundException() {
        assertThatThrownBy(() -> traineeRepository.deleteByUsername(GHOST_USERNAME))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(GHOST_USERNAME);
    }

    @Test
    void getTrainers_returnsAssignedTrainersForTrainee() {
        List<Trainer> trainers = traineeRepository.getTrainers(EXISTING_TRAINEE);

        assertThat(trainers).isNotEmpty();
        assertThat(trainers).extracting(Trainer::getUsername).contains(ASSIGNED_TRAINER);
    }

    @Test
    void getTrainers_withNonExistentTrainee_throwsEntityNotFoundException() {
        assertThatThrownBy(() -> traineeRepository.getTrainers(GHOST_USERNAME))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(GHOST_USERNAME);
    }

    @Test
    void getUnassignedTrainers_doesNotContainAlreadyAssignedTrainer() {
        List<Trainer> unassigned = traineeRepository.getUnassignedTrainers(EXISTING_TRAINEE);

        List<String> unassignedUsernames =
                unassigned.stream().map(Trainer::getUsername).toList();

        assertThat(unassignedUsernames).doesNotContain(ASSIGNED_TRAINER);
    }

    @Test
    void getUnassignedTrainers_containsOnlyActiveTrainers() {
        List<Trainer> unassigned = traineeRepository.getUnassignedTrainers(EXISTING_TRAINEE);

        assertThat(unassigned)
                .allSatisfy(trainer -> assertThat(trainer.getActive()).isTrue());
    }

    @Test
    void getUnassignedTrainers_withNonExistentTrainee_throwsEntityNotFoundException() {
        assertThatThrownBy(() -> traineeRepository.getUnassignedTrainers(GHOST_USERNAME))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(GHOST_USERNAME);
    }

    @Test
    void updateTrainersList_withNonExistentTrainee_throwsEntityNotFoundException() {
        assertThatThrownBy(() -> traineeRepository.updateTrainersList(GHOST_USERNAME, List.of(ASSIGNED_TRAINER)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(GHOST_USERNAME);
    }

    @Test
    void updateTrainersList_withNonExistentTrainer_throwsEntityNotFoundException() {
        assertThatThrownBy(() -> traineeRepository.updateTrainersList(EXISTING_TRAINEE, List.of(GHOST_USERNAME)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(GHOST_USERNAME);
    }
}
