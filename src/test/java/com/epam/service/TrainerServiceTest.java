package com.epam.service;

import com.epam.dao.TrainerDao;
import com.epam.domain.Trainer;
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
	private TrainerDao trainerDao;

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
		when(trainerDao.findAll()).thenReturn(Collections.emptyList());

		// When
		trainerService.create(testTrainer);

		// Then
		assertThat(testTrainer.getUsername()).isEqualTo("Alice.Johnson");
		assertThat(testTrainer.getPassword()).isNotNull().hasSize(10);
		verify(trainerDao).save(testTrainer);
	}

	@Test
	void create_shouldGenerateUniqueUsernameWhenDuplicateExists() {
		// Given
		Trainer existingTrainer = new Trainer(2L, "Alice", "Johnson", "Zumba");
		existingTrainer.setUsername("Alice.Johnson");
		when(trainerDao.findAll()).thenReturn(Collections.singletonList(existingTrainer));

		// When
		trainerService.create(testTrainer);

		// Then
		assertThat(testTrainer.getUsername()).isEqualTo("Alice.Johnson2");
		assertThat(testTrainer.getPassword()).isNotNull().hasSize(10);
		verify(trainerDao).save(testTrainer);
	}

	@Test
	void create_shouldNotCreateNewIfExists() {
		// Given
		when(trainerDao.findById(1L)).thenReturn(testTrainer);

		// Then
		assertThrows(IllegalArgumentException.class, () -> {
			trainerService.create(testTrainer);
		});
	}

	@Test
	void update_shouldSucceedWhenExists() {
		// Given
		testTrainer.setUsername("Alice.Johnson");
		testTrainer.setPassword("password123");
		when(trainerDao.findById(testTrainer.getUserId())).thenReturn(testTrainer);

		// When
		trainerService.update(testTrainer);

		// Then
		verify(trainerDao).save(testTrainer);
	}

	@Test
	void update_shouldFailWhenNotExists() {
		// Given
		testTrainer.setUserId(999L); // ID not in mock storage
		when(trainerDao.findById(anyLong())).thenReturn(null);

		// Then
		assertThrows(IllegalArgumentException.class, () -> {
			trainerService.update(testTrainer);
		});
		verify(trainerDao, never()).save(any(Trainer.class));
	}

	@Test
	void delete_shouldCallDaoDeleteWhenExists() {
		// Given
		long trainerId = 1L;
		when(trainerDao.findById(trainerId)).thenReturn(testTrainer);

		// When
		trainerService.delete(trainerId);

		// Then
		verify(trainerDao).delete(trainerId);
	}

	@Test
	void delete_shouldNotCallDaoWhenDoesNotExist() {
		// Given
		long trainerId = 999L;
		when(trainerDao.findById(trainerId)).thenReturn(null);

		// When
		trainerService.delete(trainerId);

		// Then
		verify(trainerDao, never()).delete(anyLong());
	}

	@Test
	void getById_shouldReturnTrainerFromDao() {
		// Given
		long trainerId = 1L;
		when(trainerDao.findById(trainerId)).thenReturn(testTrainer);

		// When
		Trainer result = trainerService.getById(trainerId);

		// Then
		assertThat(result).isEqualTo(testTrainer);
		verify(trainerDao).findById(trainerId);
	}

	@Test
	void getAll_shouldReturnAllTrainersFromDao() {
		// Given
		Trainer trainer2 = new Trainer(2L, "Bob", "Smith", "Boxing");
		Collection<Trainer> trainers = Arrays.asList(testTrainer, trainer2);
		when(trainerDao.findAll()).thenReturn(trainers);

		// When
		Collection<Trainer> result = trainerService.getAll();

		// Then
		assertThat(result).hasSize(2).containsExactlyInAnyOrder(testTrainer, trainer2);
		verify(trainerDao).findAll();
	}

}