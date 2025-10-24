package com.epam.dao;

import com.epam.infrastructure.dao.TrainingRepositoryImpl;
import java.time.Duration;
import java.time.LocalDate;
import com.epam.domain.model.Training;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TrainingRepositoryImplTest {

	private TrainingRepositoryImpl trainingRepositoryImpl;

	private Map<Long, Training> storage;

	@BeforeEach
	void setUp() {
		storage = new HashMap<>();
		trainingRepositoryImpl = new TrainingRepositoryImpl();
		trainingRepositoryImpl.setStorage(storage);
	}

	@Test
	void save_shouldStoreTrainingInStorage() {
		// Given
		Training training = new Training(1L, 101L, 201L, LocalDate.of(2025, 9, 9), Duration.ofHours(1));

		// When
		trainingRepositoryImpl.save(training);

		// Then
		assertThat(storage).containsEntry(1L, training);
	}

	@Test
	void save_shouldUpdateExistingTraining() {
		// Given
		Training training = new Training(1L, 101L, 201L, LocalDate.of(2025, 9, 9), Duration.ofHours(1));
		trainingRepositoryImpl.save(training);

		// When
		trainingRepositoryImpl.save(training);

		// Then
		assertThat(storage).hasSize(1);
	}

	@Test
	void findById_shouldReturnTrainingWhenExists() {
		// Given
		Training training = new Training(1L, 101L, 201L, LocalDate.of(2025, 9, 9), Duration.ofHours(1));
		storage.put(1L, training);

		// When
		Training result = trainingRepositoryImpl.findById(1L);

		// Then
		assertThat(result).isEqualTo(training);
	}

	@Test
	void findById_shouldReturnNullWhenNotExists() {
		// When
		Training result = trainingRepositoryImpl.findById(999L);

		// Then
		assertThat(result).isNull();
	}

	@Test
	void findAll_shouldReturnAllTrainings() {
		// Given
		Training training1 = new Training(1L, 101L, 201L, LocalDate.of(2025, 9, 9), Duration.ofHours(1));
		Training training2 = new Training(2L, 102L, 202L, LocalDate.of(2025, 9, 9), Duration.ofHours(1));

		storage.put(1L, training1);
		storage.put(2L, training2);

		// When
		Collection<Training> result = trainingRepositoryImpl.findAll();

		// Then
		assertThat(result).hasSize(2).containsExactlyInAnyOrder(training1, training2);
	}

	@Test
	void findAll_shouldReturnEmptyCollectionWhenNoTrainings() {
		// When
		Collection<Training> result = trainingRepositoryImpl.findAll();

		// Then
		assertThat(result).isEmpty();
	}

	@Test
	void delete_shouldRemoveTrainingFromStorage() {
		// Given
		Training training = new Training(1L, 101L, 201L, LocalDate.of(2025, 9, 9), Duration.ofHours(1));
		storage.put(1L, training);

		// When
		trainingRepositoryImpl.delete(1L);

		// Then
		assertThat(storage).doesNotContainKey(1L);
	}

	@Test
	void delete_shouldDoNothingWhenTrainingNotExists() {
		// Given
		Training training = new Training(1L, 101L, 201L, LocalDate.of(2025, 9, 9), Duration.ofHours(1));
		storage.put(1L, training);

		// When
		trainingRepositoryImpl.delete(999L);

		// Then
		assertThat(storage).hasSize(1).containsKey(1L);
	}

}
