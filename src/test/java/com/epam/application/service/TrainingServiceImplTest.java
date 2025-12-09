package com.epam.application.service;

import com.epam.application.request.CreateTrainingRequest;
import com.epam.application.service.impl.TrainingServiceImpl;
import com.epam.domain.TrainingFilter;
import com.epam.domain.model.Trainee;
import com.epam.domain.model.Trainer;
import com.epam.domain.model.Training;
import com.epam.domain.model.TrainingType;
import com.epam.domain.model.TrainingTypeEnum;
import com.epam.domain.port.TraineeRepository;
import com.epam.domain.port.TrainerRepository;
import com.epam.domain.port.TrainingRepository;
import com.epam.domain.port.TrainingTypeRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingServiceImplTest {

	@Mock
	private TrainingRepository trainingRepository;

	@Mock
	private TrainerRepository trainerRepository;

	@Mock
	private TraineeRepository traineeRepository;

	@Mock
	private TrainingTypeRepository trainingTypeRepository;

	@InjectMocks
	private TrainingServiceImpl trainingService;

	private Trainee testTrainee;

	private Trainer testTrainer;

	private TrainingType cardioType;

	private Training testTraining;

	@BeforeEach
	void setUp() {
		// Setup trainee
		testTrainee = new Trainee("John", "Doe", true);
		testTrainee.setTraineeId(1L);
		testTrainee.setUsername("John.Doe");
		testTrainee.setPassword("password123");

		// Setup training type
		cardioType = new TrainingType(TrainingTypeEnum.CARDIO);
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
		CreateTrainingRequest request = new CreateTrainingRequest("Morning Cardio Session",
				LocalDateTime.of(2024, 1, 15, 9, 0), 60, Optional.of(TrainingTypeEnum.CARDIO), "John.Doe",
				"Alice.Johnson");

		when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(testTrainee));
		when(trainerRepository.findByUsername("Alice.Johnson")).thenReturn(Optional.of(testTrainer));
		when(trainingTypeRepository.findByTrainingTypeName(TrainingTypeEnum.CARDIO))
			.thenReturn(Optional.of(cardioType));
		when(trainingRepository.save(any(Training.class))).thenReturn(testTraining);

		// When
		Training created = trainingService.create(request);

		// Then
		assertThat(created).isNotNull();
		assertThat(created.getTrainingName()).isEqualTo("Morning Cardio Session");
		verify(trainingRepository).save(any(Training.class));
	}

	@Test
	void create_shouldThrowEntityNotFoundException_whenTraineeNotFound() {
		// Given
		CreateTrainingRequest request = new CreateTrainingRequest("Morning Cardio Session", LocalDateTime.now(), 60,
				Optional.of(TrainingTypeEnum.CARDIO), "NonExistent.Trainee", "Alice.Johnson");

		when(traineeRepository.findByUsername("NonExistent.Trainee")).thenReturn(Optional.empty());

		// When/Then
		assertThatThrownBy(() -> trainingService.create(request)).isInstanceOf(EntityNotFoundException.class);
	}

	@Test
	void create_shouldThrowEntityNotFoundException_whenTrainerNotFound() {
		// Given
		CreateTrainingRequest request = new CreateTrainingRequest("Morning Cardio Session", LocalDateTime.now(), 60,
				Optional.of(TrainingTypeEnum.CARDIO), "John.Doe", "NonExistent.Trainer");

		when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(testTrainee));
		when(trainerRepository.findByUsername("NonExistent.Trainer")).thenReturn(Optional.empty());

		// When/Then
		assertThatThrownBy(() -> trainingService.create(request)).isInstanceOf(EntityNotFoundException.class);
	}

	@Test
	void create_shouldThrowIllegalArgumentExceptionException_whenTrainingTypeNotFound() {
		// Given
		assertThatThrownBy(() -> new CreateTrainingRequest("Morning Cardio Session", LocalDateTime.now(), 60,
				Optional.of(TrainingTypeEnum.valueOf("NonExistentTYpe")), "John.Doe", "Alice.Johnson"))
			.isInstanceOf(IllegalArgumentException.class);

	}

	@Test
	void getTraineeTrainings_shouldReturnTrainings_whenAuthenticated() {
		// Given
		TrainingFilter filter = TrainingFilter.empty();
		List<Training> expectedTrainings = List.of(testTraining);

		when(trainingRepository.getTraineeTrainings(testTrainee.getUsername(), filter)).thenReturn(expectedTrainings);
		when(traineeRepository.findByUsername(testTrainee.getUsername())).thenReturn(Optional.ofNullable(testTrainee));

		// When
		List<Training> trainings = trainingService.getTraineeTrainings(testTrainee.getUsername(), filter);

		// Then
		assertThat(trainings).hasSize(1);
		assertThat(trainings.get(0).getTrainingName()).isEqualTo("Morning Cardio Session");
		verify(trainingRepository).getTraineeTrainings("John.Doe", filter);
	}

	@Test
	void getTraineeTrainings_shouldApplyFilter_whenProvided() {
		// Given
		LocalDateTime fromDate = LocalDateTime.of(2024, 1, 1, 0, 0);
		LocalDateTime toDate = LocalDateTime.of(2024, 12, 31, 23, 59);
		TrainingFilter filter = TrainingFilter.forTrainee(Optional.of(fromDate), Optional.of(toDate),
				Optional.of("Alice.Johnson"), Optional.of(TrainingTypeEnum.CARDIO));

		List<Training> expectedTrainings = List.of(testTraining);

		when(trainingRepository.getTraineeTrainings("John.Doe", filter)).thenReturn(expectedTrainings);
		when(traineeRepository.findByUsername(testTrainee.getUsername())).thenReturn(Optional.ofNullable(testTrainee));

		// When
		List<Training> trainings = trainingService.getTraineeTrainings(testTrainee.getUsername(), filter);

		// Then
		assertThat(trainings).hasSize(1);
		verify(trainingRepository).getTraineeTrainings("John.Doe", filter);
	}

	@Test
	void getTrainerTrainings_shouldReturnTrainings_whenAuthenticated() {
		// Given
		TrainingFilter filter = TrainingFilter.empty();
		List<Training> expectedTrainings = List.of(testTraining);

		when(trainingRepository.getTrainerTrainings(testTrainer.getUsername(), filter)).thenReturn(expectedTrainings);
		when(trainerRepository.findByUsername(testTrainer.getUsername())).thenReturn(Optional.ofNullable(testTrainer));

		// When
		List<Training> trainings = trainingService.getTrainerTrainings(testTrainer.getUsername(), filter);

		// Then
		assertThat(trainings).hasSize(1);
		assertThat(trainings.get(0).getTrainingName()).isEqualTo("Morning Cardio Session");
		verify(trainingRepository).getTrainerTrainings("Alice.Johnson", filter);
	}

	@Test
	void getTrainerTrainings_shouldApplyFilter_whenProvided() {
		// Given
		LocalDateTime fromDate = LocalDateTime.of(2024, 1, 1, 0, 0);
		LocalDateTime toDate = LocalDateTime.of(2024, 12, 31, 23, 59);
		TrainingFilter filter = TrainingFilter.forTrainer(Optional.of(fromDate), Optional.of(toDate),
				Optional.of("John.Doe"));

		List<Training> expectedTrainings = List.of(testTraining);

		when(trainingRepository.getTrainerTrainings(testTrainer.getUsername(), filter)).thenReturn(expectedTrainings);
		when(trainerRepository.findByUsername(testTrainer.getUsername())).thenReturn(Optional.ofNullable(testTrainer));

		// When
		List<Training> trainings = trainingService.getTrainerTrainings(testTrainer.getUsername(), filter);

		// Then
		assertThat(trainings).hasSize(1);
		verify(trainingRepository).getTrainerTrainings("Alice.Johnson", filter);
	}

}