package com.epam.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.application.service.TrainerService;
import com.epam.domain.model.Trainer;
import com.epam.infrastructure.persistence.repository.TrainerRepositoryImpl;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
		when(trainerRepositoryImpl.findLatestUsername(any())).thenReturn(Optional.empty());

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
		when(trainerRepositoryImpl.findLatestUsername("Alice.Johnson")).thenReturn(Optional.of("Alice.Johnson1"));

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
		when(trainerRepositoryImpl.findById(1L)).thenReturn(Optional.of(testTrainer));

		// Then
		assertThrows(IllegalArgumentException.class, () -> trainerService.create(testTrainer));
	}

	@Test
	void create_shouldGenerateUniqueWhenBaseExists() {
		// Given
		when(trainerRepositoryImpl.findLatestUsername("Alice.Johnson")).thenReturn(Optional.of("Alice.Johnson"));

		// When
		trainerService.create(testTrainer);

		// Then
		assertThat(testTrainer.getUsername()).isEqualTo("Alice.Johnson1");
		verify(trainerRepositoryImpl).save(testTrainer);
	}

	@Test
	void update_shouldSucceedWhenExists() {
		// Given
		testTrainer.setUsername("Alice.Johnson");
		testTrainer.setPassword("password123");
		when(trainerRepositoryImpl.findById(testTrainer.getUserId())).thenReturn(Optional.of(testTrainer));

		// When
		trainerService.update(testTrainer);

		// Then
		verify(trainerRepositoryImpl).save(testTrainer);
	}

	@Test
	void update_shouldFailWhenNotExists() {
		// Given
		testTrainer.setUserId(999L); // ID not in mock storage
		when(trainerRepositoryImpl.findById(anyLong())).thenReturn(Optional.empty());

		// Then
		assertThrows(IllegalArgumentException.class, () -> trainerService.update(testTrainer));
		verify(trainerRepositoryImpl, never()).save(any(Trainer.class));
	}

	@Test
	void delete_shouldCallDaoDeleteWhenExists() {
		// Given
		long trainerId = 1L;
		when(trainerRepositoryImpl.findById(trainerId)).thenReturn(Optional.of(testTrainer));

		// When
		trainerService.delete(trainerId);

		// Then
		verify(trainerRepositoryImpl).delete(trainerId);
	}

	@Test
	void delete_shouldNotCallDaoWhenDoesNotExist() {
		// Given
		long trainerId = 999L;
		when(trainerRepositoryImpl.findById(trainerId)).thenReturn(Optional.empty());

		// When
		trainerService.delete(trainerId);

		// Then
		verify(trainerRepositoryImpl, never()).delete(anyLong());
	}

	@Test
	void getById_shouldReturnTrainerFromDao() {
		// Given
		long trainerId = 1L;
		when(trainerRepositoryImpl.findById(trainerId)).thenReturn(Optional.of(testTrainer));

		// When
		Optional<Trainer> result = trainerService.getById(trainerId);

		// Then
		assertThat(result).isPresent();
		assertThat(result.get()).isEqualTo(testTrainer);
		verify(trainerRepositoryImpl).findById(trainerId);
	}

}