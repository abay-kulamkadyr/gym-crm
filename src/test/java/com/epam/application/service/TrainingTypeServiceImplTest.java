package com.epam.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.application.service.impl.TrainingTypeServiceImpl;
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
class TrainingTypeServiceImplTest {

	@Mock
	private TrainingTypeRepositoryImpl trainingTypeRepositoryImpl;

	@InjectMocks
	private TrainingTypeServiceImpl trainingTypeServiceImpl;

	private TrainingType testTrainingType;

	@BeforeEach
	void setUp() {
		testTrainingType = new TrainingType(null, "Cardio", 10L, 20L);
	}

	@Test
	void create_shouldCallDaoSave() {
		// When
		trainingTypeServiceImpl.create(testTrainingType);

		// Then
		verify(trainingTypeRepositoryImpl).save(testTrainingType);
	}

	@Test
	void create_shouldNotCreateNewIfExists() {
		// Given
		testTrainingType.setTrainingTypeId(1L);

		// Then
		assertThrows(IllegalArgumentException.class, () -> trainingTypeServiceImpl.create(testTrainingType));
	}

	@Test
	void update_shouldSucceedWhenExists() {
		// Given
		testTrainingType = new TrainingType(1L, "Strength Training", 10L, 20L);
		when(trainingTypeRepositoryImpl.findById(testTrainingType.getTrainingTypeId()))
			.thenReturn(Optional.of(testTrainingType));

		// When
		trainingTypeServiceImpl.update(testTrainingType);

		// Then
		verify(trainingTypeRepositoryImpl).save(testTrainingType);
	}

	@Test
	void update_shouldFailWhenNotExists() {
		// Given
		testTrainingType = new TrainingType(999L, "Invalid", 10L, 20L);
		when(trainingTypeRepositoryImpl.findById(anyLong())).thenReturn(Optional.empty());

		// Then
		assertThrows(IllegalArgumentException.class, () -> trainingTypeServiceImpl.update(testTrainingType));
		verify(trainingTypeRepositoryImpl, never()).save(any(TrainingType.class));
	}

	@Test
	void delete_shouldCallDaoDeleteWhenExists() {

		// When
		trainingTypeServiceImpl.delete(1L);

		// Then
		verify(trainingTypeRepositoryImpl).delete(1L);
	}

	@Test
	void getById_shouldReturnTrainingTypeFromDao() {
		// Given
		long typeId = 1L;
		when(trainingTypeRepositoryImpl.findById(typeId)).thenReturn(Optional.of(testTrainingType));

		// When
		Optional<TrainingType> result = trainingTypeServiceImpl.getById(typeId);

		// Then
		assertThat(result).isPresent();
		assertThat(result.get()).isEqualTo(testTrainingType);
		verify(trainingTypeRepositoryImpl).findById(typeId);
	}

}