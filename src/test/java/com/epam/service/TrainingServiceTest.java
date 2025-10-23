package com.epam.service;

import com.epam.dao.TrainingDao;
import com.epam.domain.Training;
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
	private TrainingDao trainingDao;

	// NOTE: In a real app, this service would also mock TraineeDao and TrainerDao
	// to validate IDs, but we'll focus on the core TrainingDao interactions.
	@InjectMocks
	private TrainingService trainingService;

	private Training testTraining;

	@BeforeEach
	void setUp() {
		// Assuming Training has a constructor like: new Training(id, trainerId,
		// traineeId, date, duration)
		testTraining = new Training(1L, 101L, 201L, LocalDate.now(), Duration.ofHours(1));
	}

	@Test
	void create_shouldCallDaoSave() {
		// When
		trainingService.create(testTraining);

		// Then
		verify(trainingDao).save(testTraining);
	}

	@Test
	void create_shouldNotCreateNewIfExists() {
		// Given
		when(trainingDao.findById(1L)).thenReturn(testTraining);

		// Then
		assertThrows(IllegalArgumentException.class, () -> {
			trainingService.create(testTraining);
		});
	}

	@Test
	void update_shouldSucceedWhenExists() {
		// Given
		testTraining.setTrainingDuration(Duration.ofHours(2));
		when(trainingDao.findById(testTraining.getTrainingId())).thenReturn(testTraining);

		// When
		trainingService.update(testTraining);

		// Then
		verify(trainingDao).save(testTraining);
	}

	@Test
	void update_shouldFailWhenNotExists() {
		// Given
		testTraining.setTrainingId(999L);
		when(trainingDao.findById(anyLong())).thenReturn(null);

		// Then
		assertThrows(IllegalArgumentException.class, () -> {
			trainingService.update(testTraining);
		});
		verify(trainingDao, never()).save(any(Training.class));
	}

	@Test
	void delete_shouldCallDaoDeleteWhenExists() {
		// Given
		long trainingId = 1L;
		when(trainingDao.findById(trainingId)).thenReturn(testTraining);

		// When
		trainingService.delete(trainingId);

		// Then
		verify(trainingDao).delete(trainingId);
	}

	@Test
	void delete_shouldNotCallDaoWhenDoesNotExist() {
		// Given
		long trainingId = 999L;
		when(trainingDao.findById(trainingId)).thenReturn(null);

		// When
		trainingService.delete(trainingId);

		// Then
		verify(trainingDao, never()).delete(anyLong());
	}

	@Test
	void getById_shouldReturnTrainingFromDao() {
		// Given
		long trainingId = 1L;
		when(trainingDao.findById(trainingId)).thenReturn(testTraining);

		// When
		Training result = trainingService.getById(trainingId);

		// Then
		assertThat(result).isEqualTo(testTraining);
		verify(trainingDao).findById(trainingId);
	}

	@Test
	void getAll_shouldReturnAllTrainingsFromDao() {
		// Given
		Training training2 = new Training(2L, 102L, 202L, LocalDate.now().plusDays(1), Duration.ofMinutes(45));
		Collection<Training> trainings = Arrays.asList(testTraining, training2);
		when(trainingDao.findAll()).thenReturn(trainings);

		// When
		Collection<Training> result = trainingService.getAll();

		// Then
		assertThat(result).hasSize(2).containsExactlyInAnyOrder(testTraining, training2);
		verify(trainingDao).findAll();
	}

}