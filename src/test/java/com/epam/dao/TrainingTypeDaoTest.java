package com.epam.dao;

import com.epam.domain.TrainingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TrainingTypeDaoTest {

	private TrainingTypeDao trainingTypeDao;

	private Map<Long, TrainingType> storage;

	@BeforeEach
	void setUp() {
		// Initialize the in-memory storage (Map)
		storage = new HashMap<>();

		// Instantiate the DAO and inject the storage
		trainingTypeDao = new TrainingTypeDao();
		// NOTE: Assumes TrainingTypeDao has a setter for storage, like your other DAOs
		trainingTypeDao.setStorage(storage);
	}

	// -------------------------------------------------------------------------
	// Save Tests
	// -------------------------------------------------------------------------

	@Test
	void save_shouldStoreTrainingTypeInStorage() {
		TrainingType type = new TrainingType(1L, "Cardio", 1L, 2L);

		// When
		trainingTypeDao.save(type);

		// Then
		assertThat(storage).containsEntry(1L, type);
	}

	// -------------------------------------------------------------------------
	// Find By ID Tests
	// -------------------------------------------------------------------------

	@Test
	void findById_shouldReturnTrainingTypeWhenExists() {
		// Given
		TrainingType type = new TrainingType(1L, "Cardio", 1L, 2L);
		storage.put(1L, type);

		// When
		TrainingType result = trainingTypeDao.findById(1L);

		// Then
		assertThat(result).isEqualTo(type);
	}

	@Test
	void findById_shouldReturnNullWhenNotExists() {
		// When
		TrainingType result = trainingTypeDao.findById(999L);

		// Then
		assertThat(result).isNull();
	}

	// -------------------------------------------------------------------------
	// Find All Tests
	// -------------------------------------------------------------------------

	@Test
	void findAll_shouldReturnAllTrainingTypes() {
		// Given
		TrainingType type1 = new TrainingType(1L, "Cardio", 1L, 2L);
		TrainingType type2 = new TrainingType(2L, "Strength", 1L, 2L);
		storage.put(1L, type1);
		storage.put(2L, type2);

		// When
		Collection<TrainingType> result = trainingTypeDao.findAll();

		// Then
		assertThat(result).hasSize(2).containsExactlyInAnyOrder(type1, type2);
	}

	@Test
	void findAll_shouldReturnEmptyCollectionWhenNoTrainingTypes() {
		// When
		Collection<TrainingType> result = trainingTypeDao.findAll();

		// Then
		assertThat(result).isEmpty();
	}

	// -------------------------------------------------------------------------
	// Delete Tests
	// -------------------------------------------------------------------------

	@Test
	void delete_shouldRemoveTrainingTypeFromStorage() {
		// Given
		TrainingType type = new TrainingType(1L, "Cardio", 1L, 2L);
		storage.put(1L, type);

		// When
		trainingTypeDao.delete(1L);

		// Then
		assertThat(storage).doesNotContainKey(1L);
	}

	@Test
	void delete_shouldDoNothingWhenTrainingTypeNotExists() {
		// Given
		TrainingType type = new TrainingType(1L, "Cardio", 1L, 2L);
		storage.put(1L, type);

		// When
		trainingTypeDao.delete(999L);

		// Then
		assertThat(storage).hasSize(1).containsKey(1L);
	}

}