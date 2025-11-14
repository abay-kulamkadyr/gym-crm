package com.epam.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.application.Credentials;
import com.epam.application.exception.AuthenticationException;
import com.epam.application.request.CreateTrainingRequest;
import com.epam.application.service.impl.AuthenticationServiceImpl;
import com.epam.application.service.impl.TrainingServiceImpl;
import com.epam.domain.TrainingFilter;
import com.epam.domain.model.Trainee;
import com.epam.domain.model.Trainer;
import com.epam.domain.model.Training;
import com.epam.domain.model.TrainingType;
import com.epam.domain.repository.TraineeRepository;
import com.epam.domain.repository.TrainerRepository;
import com.epam.domain.repository.TrainingRepository;
import com.epam.domain.repository.TrainingTypeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class TrainingServiceImplTest {

	@Mock
	private TrainingRepository trainingRepository;

	@Mock
	private AuthenticationServiceImpl authenticationService;

	@Mock
	private TrainerRepository trainerRepository;

	@Mock
	private TraineeRepository traineeRepository;

	@Mock
	private TrainingTypeRepository trainingTypeRepository;

	@InjectMocks
	private TrainingServiceImpl trainingService;

	private Credentials traineeCredentials;

	private Credentials trainerCredentials;

	private Trainee testTrainee;

	private Trainer testTrainer;

	private TrainingType cardioType;

	private Training testTraining;

	@BeforeEach
	void setUp() {
		// Setup credentials
		traineeCredentials = new Credentials("John.Doe", "password123");
		trainerCredentials = new Credentials("Alice.Johnson", "trainerpass456");

		// Setup trainee
		testTrainee = new Trainee("John", "Doe", true);
		testTrainee.setTraineeId(1L);
		testTrainee.setUsername("John.Doe");
		testTrainee.setPassword("password123");

		// Setup training type
		cardioType = new TrainingType("Cardio");
		cardioType.setTrainingTypeId(1L);

		// Setup trainer
		testTrainer = new Trainer("Alice", "Johnson", true, cardioType);
		testTrainer.setTrainerId(1L);
		testTrainer.setUsername("Alice.Johnson");
		testTrainer.setPassword("trainerpass456");

		// Setup training
		testTraining = Training.builder()
			.trainingId(1L)
			.trainingName("Morning Cardio Session")
			.trainingDate(LocalDateTime.of(2024, 1, 15, 9, 0))
			.trainingDurationMin(60)
			.trainee(testTrainee)
			.trainer(testTrainer)
			.trainingType(cardioType)
			.build();
	}

	@Test
	void create_shouldCreateTraining_whenAllEntitiesExist() {
		// Given
		CreateTrainingRequest request = new CreateTrainingRequest(traineeCredentials, "Morning Cardio Session",
				LocalDateTime.of(2024, 1, 15, 9, 0), 60, "Cardio", "John.Doe", "Alice.Johnson");

		when(authenticationService.authenticateTrainee(traineeCredentials)).thenReturn(true);
		when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(testTrainee));
		when(trainerRepository.findByUsername("Alice.Johnson")).thenReturn(Optional.of(testTrainer));
		when(trainingTypeRepository.findByTrainingTypeName("Cardio")).thenReturn(Optional.of(cardioType));
		when(trainingRepository.save(any(Training.class))).thenReturn(testTraining);

		// When
		Training created = trainingService.create(request);

		// Then
		assertThat(created).isNotNull();
		assertThat(created.getTrainingName()).isEqualTo("Morning Cardio Session");
		verify(trainingRepository).save(any(Training.class));
	}

	@Test
	void create_shouldThrowAuthenticationException_whenCredentialsInvalid() {
		// Given
		CreateTrainingRequest request = new CreateTrainingRequest(traineeCredentials, "Morning Cardio Session",
				LocalDateTime.now(), 60, "Cardio", "John.Doe", "Alice.Johnson");

		when(authenticationService.authenticateTrainee(traineeCredentials)).thenReturn(false);
		when(authenticationService.authenticateTrainer(traineeCredentials)).thenReturn(false);

		// When/Then
		assertThatThrownBy(() -> trainingService.create(request)).isInstanceOf(AuthenticationException.class);
	}

	@Test
	void create_shouldThrowEntityNotFoundException_whenTraineeNotFound() {
		// Given
		CreateTrainingRequest request = new CreateTrainingRequest(traineeCredentials, "Morning Cardio Session",
				LocalDateTime.now(), 60, "Cardio", "NonExistent.Trainee", "Alice.Johnson");

		when(authenticationService.authenticateTrainee(traineeCredentials)).thenReturn(true);
		when(traineeRepository.findByUsername("NonExistent.Trainee")).thenReturn(Optional.empty());

		// When/Then
		assertThatThrownBy(() -> trainingService.create(request)).isInstanceOf(EntityNotFoundException.class);
	}

	@Test
	void create_shouldThrowEntityNotFoundException_whenTrainerNotFound() {
		// Given
		CreateTrainingRequest request = new CreateTrainingRequest(traineeCredentials, "Morning Cardio Session",
				LocalDateTime.now(), 60, "Cardio", "John.Doe", "NonExistent.Trainer");

		when(authenticationService.authenticateTrainee(traineeCredentials)).thenReturn(true);
		when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(testTrainee));
		when(trainerRepository.findByUsername("NonExistent.Trainer")).thenReturn(Optional.empty());

		// When/Then
		assertThatThrownBy(() -> trainingService.create(request)).isInstanceOf(EntityNotFoundException.class);
	}

	@Test
	void create_shouldThrowEntityNotFoundException_whenTrainingTypeNotFound() {
		// Given
		CreateTrainingRequest request = new CreateTrainingRequest(traineeCredentials, "Morning Cardio Session",
				LocalDateTime.now(), 60, "NonExistentType", "John.Doe", "Alice.Johnson");

		when(authenticationService.authenticateTrainee(traineeCredentials)).thenReturn(true);
		when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(testTrainee));
		when(trainerRepository.findByUsername("Alice.Johnson")).thenReturn(Optional.of(testTrainer));
		when(trainingTypeRepository.findByTrainingTypeName("NonExistentType")).thenReturn(Optional.empty());

		// When/Then
		assertThatThrownBy(() -> trainingService.create(request)).isInstanceOf(EntityNotFoundException.class);
	}

	@Test
	void getTraineeTrainings_shouldReturnTrainings_whenAuthenticated() {
		// Given
		TrainingFilter filter = TrainingFilter.empty();
		List<Training> expectedTrainings = List.of(testTraining);

		when(authenticationService.authenticateTrainee(traineeCredentials)).thenReturn(true);
		when(trainingRepository.getTraineeTrainings("John.Doe", filter)).thenReturn(expectedTrainings);

		// When
		List<Training> trainings = trainingService.getTraineeTrainings(traineeCredentials, filter);

		// Then
		assertThat(trainings).hasSize(1);
		assertThat(trainings.get(0).getTrainingName()).isEqualTo("Morning Cardio Session");
		verify(trainingRepository).getTraineeTrainings("John.Doe", filter);
	}

	@Test
	void getTraineeTrainings_shouldThrowAuthenticationException_whenNotAuthenticated() {
		// Given
		TrainingFilter filter = TrainingFilter.empty();

		when(authenticationService.authenticateTrainee(traineeCredentials)).thenReturn(false);
		when(authenticationService.authenticateTrainer(traineeCredentials)).thenReturn(false);

		// When/Then
		assertThatThrownBy(() -> trainingService.getTraineeTrainings(traineeCredentials, filter))
			.isInstanceOf(AuthenticationException.class);
	}

	@Test
	void getTraineeTrainings_shouldApplyFilter_whenProvided() {
		// Given
		LocalDateTime fromDate = LocalDateTime.of(2024, 1, 1, 0, 0);
		LocalDateTime toDate = LocalDateTime.of(2024, 12, 31, 23, 59);
		TrainingFilter filter = TrainingFilter.forTrainee(Optional.of(fromDate), Optional.of(toDate),
				Optional.of("Alice.Johnson"), Optional.of("Cardio"));

		List<Training> expectedTrainings = List.of(testTraining);

		when(authenticationService.authenticateTrainee(traineeCredentials)).thenReturn(true);
		when(trainingRepository.getTraineeTrainings("John.Doe", filter)).thenReturn(expectedTrainings);

		// When
		List<Training> trainings = trainingService.getTraineeTrainings(traineeCredentials, filter);

		// Then
		assertThat(trainings).hasSize(1);
		verify(trainingRepository).getTraineeTrainings("John.Doe", filter);
	}

	@Test
	void getTrainerTrainings_shouldReturnTrainings_whenAuthenticated() {
		// Given
		TrainingFilter filter = TrainingFilter.empty();
		List<Training> expectedTrainings = List.of(testTraining);

		when(authenticationService.authenticateTrainer(trainerCredentials)).thenReturn(true);
		when(trainingRepository.getTrainerTrainings("Alice.Johnson", filter)).thenReturn(expectedTrainings);

		// When
		List<Training> trainings = trainingService.getTrainerTrainings(trainerCredentials, filter);

		// Then
		assertThat(trainings).hasSize(1);
		assertThat(trainings.get(0).getTrainingName()).isEqualTo("Morning Cardio Session");
		verify(trainingRepository).getTrainerTrainings("Alice.Johnson", filter);
	}

	@Test
	void getTrainerTrainings_shouldThrowAuthenticationException_whenNotAuthenticated() {
		// Given
		TrainingFilter filter = TrainingFilter.empty();

		when(authenticationService.authenticateTrainee(trainerCredentials)).thenReturn(false);
		when(authenticationService.authenticateTrainer(trainerCredentials)).thenReturn(false);

		// When/Then
		assertThatThrownBy(() -> trainingService.getTrainerTrainings(trainerCredentials, filter))
			.isInstanceOf(AuthenticationException.class);
	}

	@Test
	void getTrainerTrainings_shouldApplyFilter_whenProvided() {
		// Given
		LocalDateTime fromDate = LocalDateTime.of(2024, 1, 1, 0, 0);
		LocalDateTime toDate = LocalDateTime.of(2024, 12, 31, 23, 59);
		TrainingFilter filter = TrainingFilter.forTrainer(Optional.of(fromDate), Optional.of(toDate),
				Optional.of("John.Doe"));

		List<Training> expectedTrainings = List.of(testTraining);

		when(authenticationService.authenticateTrainer(trainerCredentials)).thenReturn(true);
		when(trainingRepository.getTrainerTrainings("Alice.Johnson", filter)).thenReturn(expectedTrainings);

		// When
		List<Training> trainings = trainingService.getTrainerTrainings(trainerCredentials, filter);

		// Then
		assertThat(trainings).hasSize(1);
		verify(trainingRepository).getTrainerTrainings("Alice.Johnson", filter);
	}

	@Test
	void create_shouldAuthenticate_asTrainer_whenTraineeAuthFails() {
		// Given
		CreateTrainingRequest request = new CreateTrainingRequest(trainerCredentials, "Morning Cardio Session",
				LocalDateTime.of(2024, 1, 15, 9, 0), 60, "Cardio", "John.Doe", "Alice.Johnson");

		when(authenticationService.authenticateTrainee(trainerCredentials)).thenReturn(false);
		when(authenticationService.authenticateTrainer(trainerCredentials)).thenReturn(true);
		when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(testTrainee));
		when(trainerRepository.findByUsername("Alice.Johnson")).thenReturn(Optional.of(testTrainer));
		when(trainingTypeRepository.findByTrainingTypeName("Cardio")).thenReturn(Optional.of(cardioType));
		when(trainingRepository.save(any(Training.class))).thenReturn(testTraining);

		// When
		Training created = trainingService.create(request);

		// Then
		assertThat(created).isNotNull();
		verify(authenticationService).authenticateTrainee(trainerCredentials);
		verify(authenticationService).authenticateTrainer(trainerCredentials);
	}

}