package com.epam.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.application.service.TrainingTypeService;
import com.epam.domain.model.TrainingType;
import com.epam.infrastructure.persistence.repository.TrainingTypeRepositoryImpl;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TrainingTypeServiceTest {

	@Mock
	private TrainingTypeRepositoryImpl trainingTypeRepositoryImpl;

	@InjectMocks
	private TrainingTypeService trainingTypeService;

	private TrainingType testTrainingType;

	@BeforeEach
	void setUp() {
		testTrainingType = new TrainingType(1L, "Cardio", 10L, 20L);
	}

	@Test
	void create_shouldCallDaoSave() {
		// When
		trainingTypeService.create(testTrainingType);

		// Then
		verify(trainingTypeRepositoryImpl).save(testTrainingType);
	}

	@Test
	void create_shouldNotCreateNewIfExists() {
		// Given
		when(trainingTypeRepositoryImpl.findById(1L)).thenReturn(Optional.of(testTrainingType));

		// Then
		assertThrows(IllegalArgumentException.class, () -> trainingTypeService.create(testTrainingType));
	}

	@Test
	void update_shouldSucceedWhenExists() {
		// Given
		testTrainingType = new TrainingType(1L, "Strength Training", 10L, 20L);
		when(trainingTypeRepositoryImpl.findById(testTrainingType.getTrainingTypeId()))
			.thenReturn(Optional.of(testTrainingType));

		// When
		trainingTypeService.update(testTrainingType);

		// Then
		verify(trainingTypeRepositoryImpl).save(testTrainingType);
	}

	@Test
	void update_shouldFailWhenNotExists() {
		// Given
		testTrainingType = new TrainingType(999L, "Invalid", 10L, 20L);
		when(trainingTypeRepositoryImpl.findById(anyLong())).thenReturn(Optional.empty());

		// Then
		assertThrows(IllegalArgumentException.class, () -> trainingTypeService.update(testTrainingType));
		verify(trainingTypeRepositoryImpl, never()).save(any(TrainingType.class));
	}

	@Test
	void delete_shouldCallDaoDeleteWhenExists() {
		// Given
		long typeId = 1L;
		when(trainingTypeRepositoryImpl.findById(typeId)).thenReturn(Optional.of(testTrainingType));

		// When
		trainingTypeService.delete(typeId);

		// Then
		verify(trainingTypeRepositoryImpl).delete(typeId);
	}

	@Test
	void delete_shouldNotCallDaoWhenDoesNotExist() {
		// Given
		long typeId = 999L;
		when(trainingTypeRepositoryImpl.findById(typeId)).thenReturn(Optional.empty());

		// When
		trainingTypeService.delete(typeId);

		// Then
		verify(trainingTypeRepositoryImpl, never()).delete(anyLong());
	}

	@Test
	void getById_shouldReturnTrainingTypeFromDao() {
		// Given
		long typeId = 1L;
		when(trainingTypeRepositoryImpl.findById(typeId)).thenReturn(Optional.of(testTrainingType));

		// When
		Optional<TrainingType> result = trainingTypeService.getById(typeId);

		// Then
		assertThat(result).isPresent();
		assertThat(result.get()).isEqualTo(testTrainingType);
		verify(trainingTypeRepositoryImpl).findById(typeId);
	}

}