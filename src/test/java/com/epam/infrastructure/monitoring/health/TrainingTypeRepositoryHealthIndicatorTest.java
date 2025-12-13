package com.epam.infrastructure.monitoring.health;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import com.epam.domain.model.TrainingType;
import com.epam.domain.model.TrainingTypeEnum;
import com.epam.domain.port.TrainingTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

@ExtendWith(MockitoExtension.class)
class TrainingTypeRepositoryHealthIndicatorTest {

    @Mock
    private TrainingTypeRepository trainingTypeRepository;

    private TrainingTypeRepositoryHealthIndicator healthIndicator;

    @BeforeEach
    void setUp() {
        healthIndicator = new TrainingTypeRepositoryHealthIndicator(trainingTypeRepository);
    }

    @Test
    void health_shouldReturnUp_whenTrainingTypesCountIsAtLeastMinimum() {
        // Given - 5 training types (meets minimum)
        List<TrainingType> trainingTypes = List
                .of(
                    new TrainingType(TrainingTypeEnum.YOGA),
                    new TrainingType(TrainingTypeEnum.CARDIO),
                    new TrainingType(TrainingTypeEnum.BOXING),
                    new TrainingType(TrainingTypeEnum.CROSSFIT),
                    new TrainingType(TrainingTypeEnum.PILATES));
        when(trainingTypeRepository.getTrainingTypes()).thenReturn(trainingTypes);

        // When
        Health health = healthIndicator.health();

        // Then
        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("Training types count", 5);
        assertThat(health.getDetails()).containsEntry("Required minimum", 5);
    }

    @Test
    void health_shouldReturnUp_whenTrainingTypesCountExceedsMinimum() {
        List<TrainingType> trainingTypes = List
                .of(
                    new TrainingType(TrainingTypeEnum.YOGA),
                    new TrainingType(TrainingTypeEnum.CARDIO),
                    new TrainingType(TrainingTypeEnum.BOXING),
                    new TrainingType(TrainingTypeEnum.CROSSFIT),
                    new TrainingType(TrainingTypeEnum.PILATES),
                    new TrainingType(TrainingTypeEnum.STRENGTH));
        when(trainingTypeRepository.getTrainingTypes()).thenReturn(trainingTypes);

        // When
        Health health = healthIndicator.health();

        // Then
        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("Training types count", 6);
    }

    @Test
    void health_shouldReturnDown_whenTrainingTypesCountIsBelowMinimum() {
        // Given - 3 training types (below minimum of 5)
        List<TrainingType> trainingTypes = List
                .of(
                    new TrainingType(TrainingTypeEnum.YOGA),
                    new TrainingType(TrainingTypeEnum.CARDIO),
                    new TrainingType(TrainingTypeEnum.BOXING));
        when(trainingTypeRepository.getTrainingTypes()).thenReturn(trainingTypes);

        // When
        Health health = healthIndicator.health();

        // Then
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("Training types count", 3);
        assertThat(health.getDetails()).containsEntry("Required minimum", 5);
    }

    @Test
	void health_shouldReturnDown_whenTrainingTypesListIsEmpty() {
		// Given - Empty list
		when(trainingTypeRepository.getTrainingTypes()).thenReturn(List.of());

		// When
		Health health = healthIndicator.health();

		// Then
		assertThat(health.getStatus()).isEqualTo(Status.DOWN);
		assertThat(health.getDetails()).containsEntry("Training types count", 0);
	}

    @Test
    void health_shouldReturnDown_whenRepositoryThrowsException() {
        // Given
        RuntimeException exception = new RuntimeException("Database connection failed");
        when(trainingTypeRepository.getTrainingTypes()).thenThrow(exception);

        // When
        Health health = healthIndicator.health();

        // Then
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsKey("error");
        assertThat(health.getDetails()).containsEntry("Reason", "Failed to access TrainingTypeRepository");
        assertThat(health.getDetails()).containsEntry("Required minimum", 5);
    }

    @Test
    void health_shouldReturnDown_whenTrainingTypesCountIsOne() {
        // Given - Edge case: exactly 1 training type
        List<TrainingType> trainingTypes = List.of(new TrainingType(TrainingTypeEnum.YOGA));
        when(trainingTypeRepository.getTrainingTypes()).thenReturn(trainingTypes);

        // When
        Health health = healthIndicator.health();

        // Then
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("Training types count", 1);
    }

}
