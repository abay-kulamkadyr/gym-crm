package com.epam.dao;

import static org.assertj.core.api.Assertions.assertThat;

import com.epam.domain.model.TrainingType;
import com.epam.infrastructure.persistence.dao.TrainingTypeDao;
import com.epam.infrastructure.persistence.mapper.TrainingTypeMapper;
import com.epam.infrastructure.persistence.repository.TrainingTypeRepositoryImpl;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TrainingTypeRepositoryImplTest {

	private TrainingTypeRepositoryImpl trainingTypeRepositoryImpl;

	private Map<Long, TrainingTypeDao> storage;

	@BeforeEach
	void setUp() {
		storage = new HashMap<>();
		trainingTypeRepositoryImpl = new TrainingTypeRepositoryImpl();
		trainingTypeRepositoryImpl.setStorage(storage);
	}

	@Test
	void save_shouldStoreTrainingTypeInStorage() {
		TrainingType type = new TrainingType(1L, "Cardio", 1L, 2L);

		// When
		trainingTypeRepositoryImpl.save(type);

		// Then
		assertThat(storage).containsEntry(1L, TrainingTypeMapper.toEntity(type));
	}

	@Test
	void findById_shouldReturnTrainingTypeWhenExists() {
		// Given
		TrainingType type = new TrainingType(1L, "Cardio", 1L, 2L);
		storage.put(1L, TrainingTypeMapper.toEntity(type));

		// When
		TrainingType result = trainingTypeRepositoryImpl.findById(1L);

		// Then
		assertThat(result).isEqualTo(type);
	}

	@Test
	void findById_shouldReturnNullWhenNotExists() {
		// When
		TrainingType result = trainingTypeRepositoryImpl.findById(999L);

		// Then
		assertThat(result).isNull();
	}

	@Test
	void findAll_shouldReturnAllTrainingTypes() {
		// Given
		TrainingType type1 = new TrainingType(1L, "Cardio", 1L, 2L);
		TrainingType type2 = new TrainingType(2L, "Strength", 1L, 2L);
		storage.put(1L, TrainingTypeMapper.toEntity(type1));
		storage.put(2L, TrainingTypeMapper.toEntity(type2));

		// When
		Collection<TrainingType> result = trainingTypeRepositoryImpl.findAll();

		// Then
		assertThat(result).hasSize(2).containsExactlyInAnyOrder(type1, type2);
	}

	@Test
	void findAll_shouldReturnEmptyCollectionWhenNoTrainingTypes() {
		// When
		Collection<TrainingType> result = trainingTypeRepositoryImpl.findAll();

		// Then
		assertThat(result).isEmpty();
	}

	@Test
	void delete_shouldRemoveTrainingTypeFromStorage() {
		// Given
		TrainingType type = new TrainingType(1L, "Cardio", 1L, 2L);
		storage.put(1L, TrainingTypeMapper.toEntity(type));

		// When
		trainingTypeRepositoryImpl.delete(1L);

		// Then
		assertThat(storage).doesNotContainKey(1L);
	}

	@Test
	void delete_shouldDoNothingWhenTrainingTypeNotExists() {
		// Given
		TrainingType type = new TrainingType(1L, "Cardio", 1L, 2L);
		storage.put(1L, TrainingTypeMapper.toEntity(type));

		// When
		trainingTypeRepositoryImpl.delete(999L);

		// Then
		assertThat(storage).hasSize(1).containsKey(1L);
	}

}