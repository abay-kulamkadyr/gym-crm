package com.epam.infrastructure.persistance.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.epam.domain.model.Trainer;
import com.epam.infrastructure.persistence.dao.TrainerDao;
import com.epam.infrastructure.persistence.mapper.TrainerMapper;
import com.epam.infrastructure.persistence.repository.TrainerRepositoryImpl;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TrainerRepositoryImplTest {

	private TrainerRepositoryImpl trainerRepositoryImpl;

	private Map<Long, TrainerDao> storage;

	@BeforeEach
	void setUp() {
		storage = new HashMap<>();
		trainerRepositoryImpl = new TrainerRepositoryImpl();
		trainerRepositoryImpl.setStorage(storage);
	}

	@Test
	void save_shouldStoreTrainerInStorage() {
		// Given
		Trainer trainer = new Trainer(1L, "Alice", "Johnson", "Yoga");

		// When
		trainerRepositoryImpl.save(trainer);

		// Then
		assertThat(storage).containsEntry(1L, TrainerMapper.toEntity(trainer));
	}

	@Test
	void save_shouldUpdateExistingTrainer() {
		// Given
		Trainer trainer = new Trainer(1L, "Alice", "Johnson", "Yoga");
		trainerRepositoryImpl.save(trainer);
		trainer.setActive(false);

		// When
		trainerRepositoryImpl.save(trainer);

		// Then
		assertThat(storage).hasSize(1);
		assertThat(storage.get(1L).isActive()).isFalse();
	}

	@Test
	void findById_shouldReturnTrainerWhenExists() {
		// Given
		Trainer trainer = new Trainer(1L, "Alice", "Johnson", "Yoga");
		storage.put(1L, TrainerMapper.toEntity(trainer));

		// When
		Optional<Trainer> result = trainerRepositoryImpl.findById(1L);

		// Then
		assertThat(result).isPresent();
		assertThat(result.get()).isEqualTo(trainer);
	}

	@Test
	void findById_shouldReturnEmptyWhenNotExists() {
		// When
		Optional<Trainer> result = trainerRepositoryImpl.findById(999L);

		// Then
		assertThat(result).isEmpty();
	}

	@Test
	void delete_shouldRemoveTrainerFromStorage() {
		// Given
		Trainer trainer = new Trainer(1L, "Alice", "Johnson", "Yoga");
		storage.put(1L, TrainerMapper.toEntity(trainer));

		// When
		trainerRepositoryImpl.delete(1L);

		// Then
		assertThat(storage).doesNotContainKey(1L);
	}

	@Test
	void delete_shouldDoNothingWhenTrainerNotExists() {
		// Given
		Trainer trainer = new Trainer(1L, "Alice", "Johnson", "Yoga");

		storage.put(1L, TrainerMapper.toEntity(trainer));

		// When
		trainerRepositoryImpl.delete(999L);

		// Then
		assertThat(storage).hasSize(1).containsKey(1L);
	}

}
