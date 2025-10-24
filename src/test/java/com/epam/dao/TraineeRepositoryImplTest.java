package com.epam.dao;

import com.epam.domain.model.Trainee;
import com.epam.infrastructure.dao.TraineeRepositoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TraineeRepositoryImplTest {

	private TraineeRepositoryImpl traineeRepositoryImpl;

	private Map<Long, Trainee> storage;

	@BeforeEach
	void setUp() {
		storage = new HashMap<>();
		traineeRepositoryImpl = new TraineeRepositoryImpl();
		traineeRepositoryImpl.setStorage(storage);
	}

	@Test
	void save_shouldStoreTraineeInStorage() {
		// Given
		Trainee trainee = new Trainee(1L, "John", "Doe", LocalDate.of(1990, 1, 1));
		trainee.setActive(true);
		trainee.setAddress("123 Main St");

		// When
		traineeRepositoryImpl.save(trainee);

		// Then
		assertThat(storage).containsEntry(1L, trainee);
	}

	@Test
	void save_shouldUpdateExistingTrainee() {
		// Given
		Trainee trainee = new Trainee(1L, "John", "Doe", LocalDate.of(1990, 1, 1));
		trainee.setActive(true);
		trainee.setAddress("123 Main St");
		traineeRepositoryImpl.save(trainee);
		trainee.setAddress("456 Oak St");

		// When
		traineeRepositoryImpl.save(trainee);

		// Then
		assertThat(storage).hasSize(1);
		assertThat(storage.get(1L).getAddress()).isEqualTo("456 Oak St");
	}

	@Test
	void findById_shouldReturnTraineeWhenExists() {
		// Given
		Trainee trainee = new Trainee(1L, "John", "Doe", LocalDate.of(1990, 1, 1));
		trainee.setActive(true);
		trainee.setAddress("123 Main St");
		storage.put(1L, trainee);

		// When
		Trainee result = traineeRepositoryImpl.findById(1L);

		// Then
		assertThat(result).isEqualTo(trainee);
	}

	@Test
	void findById_shouldReturnNullWhenNotExists() {
		// When
		Trainee result = traineeRepositoryImpl.findById(999L);

		// Then
		assertThat(result).isNull();
	}

	@Test
	void findAll_shouldReturnAllTrainees() {
		// Given
		Trainee trainee1 = new Trainee(1L, "John", "Doe", LocalDate.of(1990, 1, 1));
		trainee1.setActive(true);
		trainee1.setAddress("123 Main St");
		Trainee trainee2 = new Trainee(2L, "Jane", "Smith", LocalDate.of(1992, 6, 15));
		trainee2.setActive(true);
		trainee2.setAddress("789 Elm St");
		storage.put(1L, trainee1);
		storage.put(2L, trainee2);

		// When
		Collection<Trainee> result = traineeRepositoryImpl.findAll();

		// Then
		assertThat(result).hasSize(2).containsExactlyInAnyOrder(trainee1, trainee2);
	}

	@Test
	void findAll_shouldReturnEmptyCollectionWhenNoTrainees() {
		// When
		Collection<Trainee> result = traineeRepositoryImpl.findAll();

		// Then
		assertThat(result).isEmpty();
	}

	@Test
	void delete_shouldRemoveTraineeFromStorage() {
		// Given
		Trainee trainee = new Trainee(1L, "John", "Doe", LocalDate.of(1990, 1, 1));
		trainee.setActive(true);
		trainee.setAddress("123 Main St");
		storage.put(1L, trainee);

		// When
		traineeRepositoryImpl.delete(1L);

		// Then
		assertThat(storage).doesNotContainKey(1L);
	}

	@Test
	void delete_shouldDoNothingWhenTraineeNotExists() {
		// Given
		Trainee trainee = new Trainee(1L, "John", "Doe", LocalDate.of(1990, 1, 1));
		trainee.setActive(true);
		trainee.setAddress("123 Main St");
		storage.put(1L, trainee);

		// When
		traineeRepositoryImpl.delete(999L);

		// Then
		assertThat(storage).hasSize(1).containsKey(1L);
	}

}
