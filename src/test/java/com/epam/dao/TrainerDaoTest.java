package com.epam.dao;

import com.epam.domain.Trainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TrainerDaoTest {

	private TrainerDao trainerDao;

	private Map<Long, Trainer> storage;

	@BeforeEach
	void setUp() {
		storage = new HashMap<>();
		trainerDao = new TrainerDao();
		trainerDao.setStorage(storage);
	}

	@Test
	void save_shouldStoreTrainerInStorage() {
		// Given
		Trainer trainer = new Trainer(1L, "Alice", "Johnson", "Yoga");

		// When
		trainerDao.save(trainer);

		// Then
		assertThat(storage).containsEntry(1L, trainer);
	}

	@Test
	void save_shouldUpdateExistingTrainer() {
		// Given
		Trainer trainer = new Trainer(1L, "Alice", "Johnson", "Yoga");
		trainerDao.save(trainer);
		trainer.setActive(false);

		// When
		trainerDao.save(trainer);

		// Then
		assertThat(storage).hasSize(1);
		assertThat(storage.get(1L).isActive()).isFalse();
	}

	@Test
	void findById_shouldReturnTrainerWhenExists() {
		// Given
		Trainer trainer = new Trainer(1L, "Alice", "Johnson", "Yoga");
		storage.put(1L, trainer);

		// When
		Trainer result = trainerDao.findById(1L);

		// Then
		assertThat(result).isEqualTo(trainer);
	}

	@Test
	void findById_shouldReturnNullWhenNotExists() {
		// When
		Trainer result = trainerDao.findById(999L);

		// Then
		assertThat(result).isNull();
	}

	@Test
	void findAll_shouldReturnAllTrainers() {
		// Given
		Trainer trainer1 = new Trainer(1L, "Alice", "Johnson", "Yoga");
		Trainer trainer2 = new Trainer(2L, "Bob", "Smith", "Boxing");

		storage.put(1L, trainer1);
		storage.put(2L, trainer2);

		// When
		Collection<Trainer> result = trainerDao.findAll();

		// Then
		assertThat(result).hasSize(2).containsExactlyInAnyOrder(trainer1, trainer2);
	}

	@Test
	void findAll_shouldReturnEmptyCollectionWhenNoTrainers() {
		// When
		Collection<Trainer> result = trainerDao.findAll();

		// Then
		assertThat(result).isEmpty();
	}

	@Test
	void delete_shouldRemoveTrainerFromStorage() {
		// Given
		Trainer trainer = new Trainer(1L, "Alice", "Johnson", "Yoga");
		storage.put(1L, trainer);

		// When
		trainerDao.delete(1L);

		// Then
		assertThat(storage).doesNotContainKey(1L);
	}

	@Test
	void delete_shouldDoNothingWhenTrainerNotExists() {
		// Given
		Trainer trainer = new Trainer(1L, "Alice", "Johnson", "Yoga");

		storage.put(1L, trainer);

		// When
		trainerDao.delete(999L);

		// Then
		assertThat(storage).hasSize(1).containsKey(1L);
	}

}
