package com.epam.service;

import com.epam.application.service.TrainingTypeService;
import com.epam.infrastructure.dao.TrainingTypeRepositoryImpl;
import com.epam.domain.model.TrainingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

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
		when(trainingTypeRepositoryImpl.findById(1L)).thenReturn(testTrainingType);

		// Then
		assertThrows(IllegalArgumentException.class, () -> trainingTypeService.create(testTrainingType));
	}

	@Test
	void update_shouldSucceedWhenExists() {
		// Given
		testTrainingType = new TrainingType(1L, "Strength Training", 10L, 20L);
		when(trainingTypeRepositoryImpl.findById(testTrainingType.getId())).thenReturn(testTrainingType);

		// When
		trainingTypeService.update(testTrainingType);

		// Then
		verify(trainingTypeRepositoryImpl).save(testTrainingType);
	}

	@Test
	void update_shouldFailWhenNotExists() {
		// Given
		testTrainingType = new TrainingType(999L, "Invalid", 10L, 20L);
		when(trainingTypeRepositoryImpl.findById(anyLong())).thenReturn(null);

		// Then
		assertThrows(IllegalArgumentException.class, () -> trainingTypeService.update(testTrainingType));
		verify(trainingTypeRepositoryImpl, never()).save(any(TrainingType.class));
	}

	@Test
	void delete_shouldCallDaoDeleteWhenExists() {
		// Given
		long typeId = 1L;
		when(trainingTypeRepositoryImpl.findById(typeId)).thenReturn(testTrainingType);

		// When
		trainingTypeService.delete(typeId);

		// Then
		verify(trainingTypeRepositoryImpl).delete(typeId);
	}

	@Test
	void delete_shouldNotCallDaoWhenDoesNotExist() {
		// Given
		long typeId = 999L;
		when(trainingTypeRepositoryImpl.findById(typeId)).thenReturn(null);

		// When
		trainingTypeService.delete(typeId);

		// Then
		verify(trainingTypeRepositoryImpl, never()).delete(anyLong());
	}

	@Test
	void getById_shouldReturnTrainingTypeFromDao() {
		// Given
		long typeId = 1L;
		when(trainingTypeRepositoryImpl.findById(typeId)).thenReturn(testTrainingType);

		// When
		TrainingType result = trainingTypeService.getById(typeId);

		// Then
		assertThat(result).isEqualTo(testTrainingType);
		verify(trainingTypeRepositoryImpl).findById(typeId);
	}

	@Test
	void getAll_shouldReturnAllTrainingTypesFromDao() {
		// Given
		TrainingType type2 = new TrainingType(2L, "Zumba", 11L, 21L);
		Collection<TrainingType> types = Arrays.asList(testTrainingType, type2);
		when(trainingTypeRepositoryImpl.findAll()).thenReturn(types);

		// When
		Collection<TrainingType> result = trainingTypeService.getAll();

		// Then
		assertThat(result).hasSize(2).containsExactlyInAnyOrder(testTrainingType, type2);
		verify(trainingTypeRepositoryImpl).findAll();
	}

	@Test
	void getAll_shouldReturnEmptyCollectionWhenNoTrainingTypes() {
		// Given
		when(trainingTypeRepositoryImpl.findAll()).thenReturn(Collections.emptyList());

		// When
		Collection<TrainingType> result = trainingTypeService.getAll();

		// Then
		assertThat(result).isEmpty();
		verify(trainingTypeRepositoryImpl).findAll();
	}

}