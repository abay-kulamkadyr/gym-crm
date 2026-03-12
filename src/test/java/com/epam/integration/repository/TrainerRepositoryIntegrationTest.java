package com.epam.integration.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Optional;

import com.epam.application.exception.EntityNotFoundException;
import com.epam.domain.model.Trainee;
import com.epam.domain.model.Trainer;
import com.epam.domain.port.TrainerRepository;
import com.epam.integration.base.SeededIntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class TrainerRepositoryIntegrationTest extends SeededIntegrationTestBase {

    private static final String EXISTING_TRAINER = "Sarah.Brown";
    private static final String ASSIGNED_TRAINEE = "James.Wilson";
    private static final String GHOST_USERNAME = "Ghost.User";

    @Autowired
    private TrainerRepository trainerRepository;

    @Test
    void findById_withExistingId_returnsTrainer() {
        Trainer byUsername = trainerRepository.findByUsername(EXISTING_TRAINER).orElseThrow();
        Optional<Trainer> result = trainerRepository.findById(byUsername.getTrainerId());

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo(EXISTING_TRAINER);
    }

    @Test
    void findById_withNonExistentId_returnsEmpty() {
        Optional<Trainer> result = trainerRepository.findById(Long.MAX_VALUE);

        assertThat(result).isEmpty();
    }

    @Test
    void delete_withNonExistentId_throwsEntityNotFoundException() {
        assertThatThrownBy(() -> trainerRepository.delete(Long.MAX_VALUE))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(String.valueOf(Long.MAX_VALUE));
    }

    @Test
    void deleteByUsername_withNonExistentUsername_throwsEntityNotFoundException() {
        assertThatThrownBy(() -> trainerRepository.deleteByUsername(GHOST_USERNAME))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(GHOST_USERNAME);
    }

    @Test
    void getTrainees_returnsAssignedTraineesForTrainer() {
        List<Trainee> trainees = trainerRepository.getTrainees(EXISTING_TRAINER);

        assertThat(trainees).isNotEmpty();
        assertThat(trainees).extracting(Trainee::getUsername).contains(ASSIGNED_TRAINEE);
    }

    @Test
    void getTrainees_withNonExistentTrainer_throwsEntityNotFoundException() {
        assertThatThrownBy(() -> trainerRepository.getTrainees(GHOST_USERNAME))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(GHOST_USERNAME);
    }
}
