package com.epam.service;

import com.epam.application.service.TrainingService;
import com.epam.infrastructure.dao.TrainingRepositoryImpl;
import com.epam.domain.model.Training;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {

	@Mock
	private TrainingRepositoryImpl trainingRepositoryImpl;

	@InjectMocks
	private TrainingService trainingService;

	private Training testTraining;

	@BeforeEach
	void setUp() {
		testTraining = new Training(1L, 101L, 201L, LocalDate.now(), Duration.ofHours(1));
	}

	@Test
	void create_shouldCallDaoSave() {
		// When
		trainingService.create(testTraining);

		// Then
		verify(trainingRepositoryImpl).save(testTraining);
	}

	@Test
	void create_shouldNotCreateNewIfExists() {
		// Given
		when(trainingRepositoryImpl.findById(1L)).thenReturn(testTraining);

		// Then
		assertThrows(IllegalArgumentException.class, () -> trainingService.create(testTraining));
	}

	@Test
	void update_shouldSucceedWhenExists() {
		// Given
		testTraining.setTrainingDuration(Duration.ofHours(2));
		when(trainingRepositoryImpl.findById(testTraining.getTrainingId())).thenReturn(testTraining);

		// When
		trainingService.update(testTraining);

		// Then
		verify(trainingRepositoryImpl).save(testTraining);
	}

	@Test
	void update_shouldFailWhenNotExists() {
		// Given
		testTraining.setTrainingId(999L);
		when(trainingRepositoryImpl.findById(anyLong())).thenReturn(null);

		// Then
		assertThrows(IllegalArgumentException.class, () -> trainingService.update(testTraining));
		verify(trainingRepositoryImpl, never()).save(any(Training.class));
	}

	@Test
	void delete_shouldCallDaoDeleteWhenExists() {
		// Given
		long trainingId = 1L;
		when(trainingRepositoryImpl.findById(trainingId)).thenReturn(testTraining);

		// When
		trainingService.delete(trainingId);

		// Then
		verify(trainingRepositoryImpl).delete(trainingId);
	}

	@Test
	void delete_shouldNotCallDaoWhenDoesNotExist() {
		// Given
		long trainingId = 999L;
		when(trainingRepositoryImpl.findById(trainingId)).thenReturn(null);

		// When
		trainingService.delete(trainingId);

		// Then
		verify(trainingRepositoryImpl, never()).delete(anyLong());
	}

	@Test
	void getById_shouldReturnTrainingFromDao() {
		// Given
		long trainingId = 1L;
		when(trainingRepositoryImpl.findById(trainingId)).thenReturn(testTraining);

		// When
		Training result = trainingService.getById(trainingId);

		// Then
		assertThat(result).isEqualTo(testTraining);
		verify(trainingRepositoryImpl).findById(trainingId);
	}

	@Test
	void getAll_shouldReturnAllTrainingsFromDao() {
		// Given
		Training training2 = new Training(2L, 102L, 202L, LocalDate.now().plusDays(1), Duration.ofMinutes(45));
		Collection<Training> trainings = Arrays.asList(testTraining, training2);
		when(trainingRepositoryImpl.findAll()).thenReturn(trainings);

		// When
		Collection<Training> result = trainingService.getAll();

		// Then
		assertThat(result).hasSize(2).containsExactlyInAnyOrder(testTraining, training2);
		verify(trainingRepositoryImpl).findAll();
	}

}