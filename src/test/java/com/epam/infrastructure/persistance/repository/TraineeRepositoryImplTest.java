package com.epam.infrastructure.persistance.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.epam.domain.model.Trainee;
import com.epam.infrastructure.persistence.dao.TraineeDao;
import com.epam.infrastructure.persistence.mapper.TraineeMapper;
import com.epam.infrastructure.persistence.repository.TraineeRepositoryImpl;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TraineeRepositoryImplTest {

	private TraineeRepositoryImpl traineeRepositoryImpl;

	private Map<Long, TraineeDao> storage;

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
		assertThat(storage).containsEntry(1L, TraineeMapper.toEntity(trainee));
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
		storage.put(1L, TraineeMapper.toEntity(trainee));

		// When
		Optional<Trainee> result = traineeRepositoryImpl.findById(1L);

		// Then
		assertThat(result).isPresent();
		assertThat(result.get()).isEqualTo(trainee);
	}

	@Test
	void findById_shouldReturnNullWhenNotExists() {
		// When
		Optional<Trainee> result = traineeRepositoryImpl.findById(999L);

		// Then
		assertThat(result).isEmpty();
	}

	@Test
	void delete_shouldRemoveTraineeFromStorage() {
		// Given
		Trainee trainee = new Trainee(1L, "John", "Doe", LocalDate.of(1990, 1, 1));
		trainee.setActive(true);
		trainee.setAddress("123 Main St");
		storage.put(1L, TraineeMapper.toEntity(trainee));

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
		storage.put(1L, TraineeMapper.toEntity(trainee));

		// When
		traineeRepositoryImpl.delete(999L);

		// Then
		assertThat(storage).hasSize(1).containsKey(1L);
	}

}
