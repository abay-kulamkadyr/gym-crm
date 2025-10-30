package com.epam.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.application.service.TrainingService;
import com.epam.domain.model.Training;
import com.epam.infrastructure.persistence.repository.TrainingRepositoryImpl;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
		when(trainingRepositoryImpl.findById(1L)).thenReturn(Optional.of(testTraining));

		// Then
		assertThrows(IllegalArgumentException.class, () -> trainingService.create(testTraining));
	}

	@Test
	void update_shouldSucceedWhenExists() {
		// Given
		testTraining.setTrainingDuration(Duration.ofHours(2));
		when(trainingRepositoryImpl.findById(testTraining.getTrainingId())).thenReturn(Optional.of(testTraining));

		// When
		trainingService.update(testTraining);

		// Then
		verify(trainingRepositoryImpl).save(testTraining);
	}

	@Test
	void update_shouldFailWhenNotExists() {
		// Given
		testTraining.setTrainingId(999L);
		when(trainingRepositoryImpl.findById(anyLong())).thenReturn(Optional.empty());

		// Then
		assertThrows(IllegalArgumentException.class, () -> trainingService.update(testTraining));
		verify(trainingRepositoryImpl, never()).save(any(Training.class));
	}

	@Test
	void delete_shouldCallDaoDeleteWhenExists() {
		// Given
		long trainingId = 1L;
		when(trainingRepositoryImpl.findById(trainingId)).thenReturn(Optional.of(testTraining));

		// When
		trainingService.delete(trainingId);

		// Then
		verify(trainingRepositoryImpl).delete(trainingId);
	}

	@Test
	void delete_shouldNotCallDaoWhenDoesNotExist() {
		// Given
		long trainingId = 999L;
		when(trainingRepositoryImpl.findById(trainingId)).thenReturn(Optional.empty());

		// When
		trainingService.delete(trainingId);

		// Then
		verify(trainingRepositoryImpl, never()).delete(anyLong());
	}

	@Test
	void getById_shouldReturnTrainingFromDao() {
		// Given
		long trainingId = 1L;
		when(trainingRepositoryImpl.findById(trainingId)).thenReturn(Optional.of(testTraining));

		// When
		Optional<Training> result = trainingService.getById(trainingId);

		// Then
		assertThat(result).isPresent();
		assertThat(result.get()).isEqualTo(testTraining);
		verify(trainingRepositoryImpl).findById(trainingId);
	}

}