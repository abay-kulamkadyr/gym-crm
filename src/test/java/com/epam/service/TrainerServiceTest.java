package com.epam.service;

import com.epam.application.service.TrainerService;
import com.epam.infrastructure.persistence.repository.TrainerRepositoryImpl;
import com.epam.domain.model.Trainer;
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
class TrainerServiceTest {

	@Mock
	private TrainerRepositoryImpl trainerRepositoryImpl;

	@InjectMocks
	private TrainerService trainerService;

	private Trainer testTrainer;

	@BeforeEach
	void setUp() {
		testTrainer = new Trainer(1L, "Alice", "Johnson", "Yoga");
		testTrainer.setActive(true);
	}

	@Test
	void create_shouldGenerateUsernameAndPassword() {
		// Given
		when(trainerRepositoryImpl.findAll()).thenReturn(Collections.emptyList());

		// When
		trainerService.create(testTrainer);

		// Then
		assertThat(testTrainer.getUsername()).isEqualTo("Alice.Johnson");
		assertThat(testTrainer.getPassword()).isNotNull().hasSize(10);
		verify(trainerRepositoryImpl).save(testTrainer);
	}

	@Test
	void create_shouldGenerateUniqueUsernameWhenDuplicateExists() {
		// Given
		Trainer existingTrainer = new Trainer(2L, "Alice", "Johnson", "Zumba");
		existingTrainer.setUsername("Alice.Johnson");
		when(trainerRepositoryImpl.findAll()).thenReturn(Collections.singletonList(existingTrainer));

		// When
		trainerService.create(testTrainer);

		// Then
		assertThat(testTrainer.getUsername()).isEqualTo("Alice.Johnson2");
		assertThat(testTrainer.getPassword()).isNotNull().hasSize(10);
		verify(trainerRepositoryImpl).save(testTrainer);
	}

	@Test
	void create_shouldNotCreateNewIfExists() {
		// Given
		when(trainerRepositoryImpl.findById(1L)).thenReturn(testTrainer);

		// Then
		assertThrows(IllegalArgumentException.class, () -> trainerService.create(testTrainer));
	}

	@Test
	void update_shouldSucceedWhenExists() {
		// Given
		testTrainer.setUsername("Alice.Johnson");
		testTrainer.setPassword("password123");
		when(trainerRepositoryImpl.findById(testTrainer.getUserId())).thenReturn(testTrainer);

		// When
		trainerService.update(testTrainer);

		// Then
		verify(trainerRepositoryImpl).save(testTrainer);
	}

	@Test
	void update_shouldFailWhenNotExists() {
		// Given
		testTrainer.setUserId(999L); // ID not in mock storage
		when(trainerRepositoryImpl.findById(anyLong())).thenReturn(null);

		// Then
		assertThrows(IllegalArgumentException.class, () -> trainerService.update(testTrainer));
		verify(trainerRepositoryImpl, never()).save(any(Trainer.class));
	}

	@Test
	void delete_shouldCallDaoDeleteWhenExists() {
		// Given
		long trainerId = 1L;
		when(trainerRepositoryImpl.findById(trainerId)).thenReturn(testTrainer);

		// When
		trainerService.delete(trainerId);

		// Then
		verify(trainerRepositoryImpl).delete(trainerId);
	}

	@Test
	void delete_shouldNotCallDaoWhenDoesNotExist() {
		// Given
		long trainerId = 999L;
		when(trainerRepositoryImpl.findById(trainerId)).thenReturn(null);

		// When
		trainerService.delete(trainerId);

		// Then
		verify(trainerRepositoryImpl, never()).delete(anyLong());
	}

	@Test
	void getById_shouldReturnTrainerFromDao() {
		// Given
		long trainerId = 1L;
		when(trainerRepositoryImpl.findById(trainerId)).thenReturn(testTrainer);

		// When
		Trainer result = trainerService.getById(trainerId);

		// Then
		assertThat(result).isEqualTo(testTrainer);
		verify(trainerRepositoryImpl).findById(trainerId);
	}

	@Test
	void getAll_shouldReturnAllTrainersFromDao() {
		// Given
		Trainer trainer2 = new Trainer(2L, "Bob", "Smith", "Boxing");
		Collection<Trainer> trainers = Arrays.asList(testTrainer, trainer2);
		when(trainerRepositoryImpl.findAll()).thenReturn(trainers);

		// When
		Collection<Trainer> result = trainerService.getAll();

		// Then
		assertThat(result).hasSize(2).containsExactlyInAnyOrder(testTrainer, trainer2);
		verify(trainerRepositoryImpl).findAll();
	}

}