package com.epam.infrastructure.persistance.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.epam.domain.model.Training;
import com.epam.infrastructure.persistence.dao.TrainingDao;
import com.epam.infrastructure.persistence.mapper.TrainingMapper;
import com.epam.infrastructure.persistence.repository.TrainingRepositoryImpl;
import java.time.Duration;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TrainingRepositoryImplTest {

	private TrainingRepositoryImpl trainingRepositoryImpl;

	private Map<Long, TrainingDao> storage;

	@BeforeEach
	void setUp() {
		storage = new HashMap<>();
		trainingRepositoryImpl = new TrainingRepositoryImpl();
		trainingRepositoryImpl.setStorage(storage);
	}

	@Test
	void save_shouldStoreTrainingInStorage() {
		// Given
		System.out.println("Is negative " + Duration.ofHours(1).isNegative());
		Training training = new Training(1L, 101L, 201L, LocalDate.of(2025, 9, 9), Duration.ofHours(1));

		// When
		trainingRepositoryImpl.save(training);

		// Then
		assertThat(storage).containsEntry(1L, TrainingMapper.toEntity(training));
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
		storage.put(1L, TrainingMapper.toEntity(training));

		// When
		Optional<Training> result = trainingRepositoryImpl.findById(1L);

		// Then
		assertThat(result).isPresent();
		assertThat(result.get()).isEqualTo(training);
	}

	@Test
	void findById_shouldReturnEmptyWhenNotExists() {
		// When
		Optional<Training> result = trainingRepositoryImpl.findById(999L);

		// Then
		assertThat(result).isEmpty();
	}

	@Test
	void delete_shouldRemoveTrainingFromStorage() {
		// Given
		Training training = new Training(1L, 101L, 201L, LocalDate.of(2025, 9, 9), Duration.ofHours(1));
		storage.put(1L, TrainingMapper.toEntity(training));

		// When
		trainingRepositoryImpl.delete(1L);

		// Then
		assertThat(storage).doesNotContainKey(1L);
	}

	@Test
	void delete_shouldDoNothingWhenTrainingNotExists() {
		// Given
		Training training = new Training(1L, 101L, 201L, LocalDate.of(2025, 9, 9), Duration.ofHours(1));
		storage.put(1L, TrainingMapper.toEntity(training));

		// When
		trainingRepositoryImpl.delete(999L);

		// Then
		assertThat(storage).hasSize(1).containsKey(1L);
	}

}
