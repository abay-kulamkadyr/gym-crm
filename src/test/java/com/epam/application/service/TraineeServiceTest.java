package com.epam.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.application.service.TraineeService;
import com.epam.domain.model.Trainee;
import com.epam.infrastructure.persistence.repository.TraineeRepositoryImpl;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
		when(traineeRepositoryImpl.findLatestUsername("John.Doe")).thenReturn(Optional.empty());

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
		when(traineeRepositoryImpl.findLatestUsername("John.Doe")).thenReturn(Optional.of("John.Doe1"));

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
		existingTrainee1.setUsername("John.Doe1");
		Trainee existingTrainee2 = new Trainee(3L, "John", "Doe", LocalDate.of(1988, 3, 20));
		existingTrainee2.setUsername("John.Doe2");
		when(traineeRepositoryImpl.findLatestUsername("John.Doe")).thenReturn(Optional.of("John.Doe2"));

		// When
		traineeService.create(testTrainee);

		// Then
		assertThat(testTrainee.getUsername()).isEqualTo("John.Doe3");
		assertThat(testTrainee.getUsername()).isNotEqualTo(existingTrainee2.getUsername());
		verify(traineeRepositoryImpl).save(testTrainee);
	}

	@Test
	void create_shouldGenerateUniqueWhenBaseExists() {
		// Given
		when(traineeRepositoryImpl.findLatestUsername("John.Doe")).thenReturn(Optional.of("John.Doe"));

		// When
		traineeService.create(testTrainee);

		// Then
		assertThat(testTrainee.getUsername()).isEqualTo("John.Doe1");
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
		when(traineeRepositoryImpl.findById(1L)).thenReturn(Optional.of(testTrainee));

		// Then
		assertThrows(IllegalArgumentException.class, () -> traineeService.create(testTrainee));
	}

	@Test
	void update_shouldSucceedWhenExists() {
		// Given
		testTrainee.setUsername("John.Doe");
		testTrainee.setPassword("password123");
		when(traineeRepositoryImpl.findById(testTrainee.getUserId())).thenReturn(Optional.of(testTrainee));

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
		when(traineeRepositoryImpl.findById(traineeId)).thenReturn(Optional.of(testTrainee));

		// When
		Optional<Trainee> result = traineeService.getById(traineeId);

		// Then
		assertThat(result).isPresent();
		assertThat(result.get()).isEqualTo(testTrainee);
		verify(traineeRepositoryImpl).findById(traineeId);
	}

	@Test
	void getById_shouldReturnEmptyWhenNotFound() {
		// Given
		long traineeId = 999L;
		when(traineeRepositoryImpl.findById(traineeId)).thenReturn(Optional.empty());

		// When
		Optional<Trainee> result = traineeService.getById(traineeId);

		// Then
		assertThat(result).isEmpty();
		verify(traineeRepositoryImpl).findById(traineeId);
	}

}
