package com.epam.service;

import com.epam.application.service.TraineeService;
import com.epam.infrastructure.persistence.repository.TraineeRepositoryImpl;
import com.epam.domain.model.Trainee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeServiceTest {

	@Mock
	private TraineeRepositoryImpl traineeRepositoryImpl;

	@InjectMocks
	private TraineeService traineeService;

	private Trainee testTrainee;

	@BeforeEach
	void setUp() {
		testTrainee = new Trainee(1L, "John", "Doe", LocalDate.of(1990, 1, 1));
		testTrainee.setActive(true);
		testTrainee.setAddress("123 Main St");
	}

	@Test
	void create_shouldGenerateUsernameAndPassword() {
		// Given
		when(traineeRepositoryImpl.findAll()).thenReturn(Collections.emptyList());

		// When
		traineeService.create(testTrainee);

		// Then
		assertThat(testTrainee.getUsername()).isEqualTo("John.Doe");
		assertThat(testTrainee.getPassword()).isNotNull().hasSize(10);
		verify(traineeRepositoryImpl).save(testTrainee);
	}

	@Test
	void create_shouldGenerateUniqueUsernameWhenDuplicateExists() {
		// Given
		Trainee existingTrainee = new Trainee(2L, "John", "Doe", LocalDate.of(1985, 5, 15));
		existingTrainee.setUsername("John.Doe");
		when(traineeRepositoryImpl.findAll()).thenReturn(Collections.singletonList(existingTrainee));

		// When
		traineeService.create(testTrainee);

		// Then
		assertThat(testTrainee.getUsername()).isEqualTo("John.Doe2");
		assertThat(testTrainee.getPassword()).isNotNull().hasSize(10);
		verify(traineeRepositoryImpl).save(testTrainee);
	}

	@Test
	void create_shouldGenerateUniqueUsernameWithMultipleDuplicates() {
		// Given
		Trainee existingTrainee1 = new Trainee(2L, "John", "Doe", LocalDate.of(1985, 5, 15));
		existingTrainee1.setUsername("John.Doe");
		Trainee existingTrainee2 = new Trainee(3L, "John", "Doe", LocalDate.of(1988, 3, 20));
		existingTrainee2.setUsername("John.Doe2");
		when(traineeRepositoryImpl.findAll()).thenReturn(Arrays.asList(existingTrainee1, existingTrainee2));

		// When
		traineeService.create(testTrainee);

		// Then
		assertThat(testTrainee.getUsername()).isEqualTo("John.Doe3");
		verify(traineeRepositoryImpl).save(testTrainee);
	}

	@Test
	void create_shouldCallDaoSave() {
		// Given
		testTrainee.setUsername("John.Doe");
		testTrainee.setPassword("password123");

		// When
		traineeService.create(testTrainee);

		// Then
		verify(traineeRepositoryImpl).save(testTrainee);
	}

	@Test
	void create_shouldNotCreateNewIfExists() {
		// Given
		when(traineeRepositoryImpl.findById(1L)).thenReturn(testTrainee);

		// Then
		assertThrows(IllegalArgumentException.class, () -> traineeService.create(testTrainee));
	}

	@Test
	void update_shouldSucceedWhenExists() {
		// Given
		testTrainee.setUsername("John.Doe");
		testTrainee.setPassword("password123");
		when(traineeRepositoryImpl.findById(testTrainee.getUserId())).thenReturn(testTrainee);

		// When
		traineeService.update(testTrainee);

		// Then
		verify(traineeRepositoryImpl).save(testTrainee);
	}

	@Test
	void update_shouldFailWhenNotExists() {
		// Given
		testTrainee.setUsername("John.Doe");
		testTrainee.setPassword("password123");

		// Then
		assertThrows(IllegalArgumentException.class, () -> traineeService.update(testTrainee));
	}

	@Test
	void delete_shouldNotCallDaoWhenDoesNotExist() {
		// Given
		long traineeId = 1L;

		// When
		traineeService.delete(traineeId);

		// Then
		verify(traineeRepositoryImpl, never()).delete(anyLong());
	}

	@Test
	void getById_shouldReturnTraineeFromDao() {
		// Given
		long traineeId = 1L;
		when(traineeRepositoryImpl.findById(traineeId)).thenReturn(testTrainee);

		// When
		Trainee result = traineeService.getById(traineeId);

		// Then
		assertThat(result).isEqualTo(testTrainee);
		verify(traineeRepositoryImpl).findById(traineeId);
	}

	@Test
	void getById_shouldReturnNullWhenNotFound() {
		// Given
		long traineeId = 999L;
		when(traineeRepositoryImpl.findById(traineeId)).thenReturn(null);

		// When
		Trainee result = traineeService.getById(traineeId);

		// Then
		assertThat(result).isNull();
		verify(traineeRepositoryImpl).findById(traineeId);
	}

	@Test
	void getAll_shouldReturnAllTraineesFromDao() {
		// Given
		Trainee trainee2 = new Trainee(2L, "Jane", "Smith", LocalDate.of(1992, 6, 15));
		Collection<Trainee> trainees = Arrays.asList(testTrainee, trainee2);
		when(traineeRepositoryImpl.findAll()).thenReturn(trainees);

		// When
		Collection<Trainee> result = traineeService.getAll();

		// Then
		assertThat(result).hasSize(2).containsExactlyInAnyOrder(testTrainee, trainee2);
		verify(traineeRepositoryImpl).findAll();
	}

	@Test
	void getAll_shouldReturnEmptyCollectionWhenNoTrainees() {
		// Given
		when(traineeRepositoryImpl.findAll()).thenReturn(Collections.emptyList());

		// When
		Collection<Trainee> result = traineeService.getAll();

		// Then
		assertThat(result).isEmpty();
		verify(traineeRepositoryImpl).findAll();
	}

}
