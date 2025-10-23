package com.epam.facade;

import java.time.Duration;
import com.epam.config.AppConfig;
import com.epam.domain.Trainee;
import com.epam.domain.Trainer;
import com.epam.domain.Training;
import com.epam.domain.TrainingType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AppConfig.class)
class GymFacadeIntegrationTest {

	@Autowired
	private GymFacade gymFacade;

	@Test
	void contextLoads() {
		assertThat(gymFacade).isNotNull();
	}

	@Test
	void createTrainee_shouldCreateTraineeWithGeneratedCredentials() {
		// Given
		Trainee trainee = new Trainee(999L, "Michael", "Brown", LocalDate.of(1995, 3, 10));

		// When
		gymFacade.createTrainee(trainee);

		// Then
		Trainee retrieved = gymFacade.getTrainee(999L);
		assertThat(retrieved).isNotNull();
		assertThat(retrieved.getUsername()).isEqualTo("Michael.Brown");
		assertThat(retrieved.getPassword()).isNotNull().hasSize(10);
	}

	@Test
	void updateTrainee_shouldUpdateExistingTrainee() {
		// Given
		Trainee trainee = new Trainee(998L, "Sarah", "Wilson", LocalDate.of(1993, 8, 22));
		gymFacade.createTrainee(trainee);
		trainee.setAddress("999 New Address");

		// When
		gymFacade.updateTrainee(trainee);

		// Then
		Trainee updated = gymFacade.getTrainee(998L);
		assertThat(updated.getAddress()).isEqualTo("999 New Address");
	}

	@Test
	void deleteTrainee_shouldRemoveTrainee() {
		// Given
		Trainee trainee = new Trainee(997L, "Tom", "Davis", LocalDate.of(1991, 12, 5));
		gymFacade.createTrainee(trainee);

		// When
		gymFacade.deleteTrainee(997L);

		// Then
		Trainee deleted = gymFacade.getTrainee(997L);
		assertThat(deleted).isNull();
	}

	@Test
	void getAllTrainees_shouldReturnAllTrainees() {
		// When
		Collection<Trainee> trainees = gymFacade.getAllTrainees();

		// Then
		assertThat(trainees).isNotEmpty();
	}

	@Test
	void createTrainer_shouldCreateTrainerWithGeneratedCredentials() {
		// Given
		Trainer trainer = new Trainer(999L, "Emma", "Taylor", "CrossFit");

		// When
		gymFacade.createTrainer(trainer);

		// Then
		Trainer retrieved = gymFacade.getTrainer(999L);
		assertThat(retrieved).isNotNull();
		assertThat(retrieved.getUsername()).isEqualTo("Emma.Taylor");
		assertThat(retrieved.getPassword()).isNotNull().hasSize(10);
	}

	@Test
	void updateTrainer_shouldUpdateExistingTrainer() {
		// Given
		Trainer trainer = new Trainer(998L, "David", "Martinez", "Swimming");
		gymFacade.createTrainer(trainer);
		trainer.setActive(false);

		// When
		gymFacade.updateTrainer(trainer);

		// Then
		Trainer updated = gymFacade.getTrainer(998L);
		assertThat(updated.isActive()).isFalse();
	}

	@Test
	void getAllTrainers_shouldReturnAllTrainers() {
		// When
		Collection<Trainer> trainers = gymFacade.getAllTrainers();

		// Then
		assertThat(trainers).isNotEmpty();
	}

	@Test
	void deleteTrainer_shouldRemoveTrainer() {
		// Given
		// Create a unique trainer to ensure a clean state for this test ID
		long trainerIdToDelete = 987L;
		Trainer trainer = new Trainer(trainerIdToDelete, "Henry", "Cavill", "Weightlifting");

		// 1. First, create the trainer to ensure it exists
		gymFacade.createTrainer(trainer);

		// Verify it exists before deletion
		assertThat(gymFacade.getTrainer(trainerIdToDelete)).isNotNull();

		// When
		gymFacade.deleteTrainer(trainerIdToDelete);

		// Then
		Trainer deleted = gymFacade.getTrainer(trainerIdToDelete);
		assertThat(deleted).isNull();
	}

	@Test
	void createTraining_shouldCreateTraining() {
		// Given
		Training training = new Training(999L, 101L, 201L, LocalDate.of(2025, 10, 20), Duration.ofHours(1));

		// When
		gymFacade.createTraining(training);

		// Then
		Training retrieved = gymFacade.getTraining(999L);
		assertThat(retrieved).isNotNull();
		assertThat(retrieved.getTrainerId()).isEqualTo(101L);
		assertThat(retrieved.getTraineeId()).isEqualTo(201L);
	}

	@Test
	void getAllTrainings_shouldReturnAllTrainings() {
		// When
		Collection<Training> trainings = gymFacade.getAllTrainings();

		// Then
		assertThat(trainings).isNotEmpty();
	}

	@Test
	void updateTraining_shouldUpdateExistingTraining() {
		// Given
		Training training = new Training(994L, 101L, 201L, LocalDate.of(2025, 10, 20), Duration.ofHours(1));
		gymFacade.createTraining(training);
		training.setTrainingDate(LocalDate.of(2025, 12, 25));

		// When
		gymFacade.updateTraining(training);

		// Then
		Training updated = gymFacade.getTraining(994L);
		assertThat(updated.getTrainingDate()).isEqualTo(LocalDate.of(2025, 12, 25));
	}

	@Test
	void deleteTraining_shouldRemoveTraining() {
		// Given
		Training training = new Training(993L, 101L, 201L, LocalDate.of(2025, 10, 20), Duration.ofHours(1));
		gymFacade.createTraining(training);

		// When
		gymFacade.deleteTraining(993L);

		// Then
		Training deleted = gymFacade.getTraining(993L);
		assertThat(deleted).isNull();
	}

	@Test
	void createTrainee_withDuplicateName_shouldGenerateUniqueUsername() {
		// Given
		Trainee trainee1 = new Trainee(996L, "Duplicate", "Name", LocalDate.of(1990, 1, 1));
		Trainee trainee2 = new Trainee(995L, "Duplicate", "Name", LocalDate.of(1991, 2, 2));

		// When
		gymFacade.createTrainee(trainee1);
		gymFacade.createTrainee(trainee2);

		// Then
		assertThat(trainee1.getUsername()).isEqualTo("Duplicate.Name");
		assertThat(trainee2.getUsername()).isEqualTo("Duplicate.Name2");
	}

	@Test
	void createTrainer_withDuplicateName_shouldGenerateUniqueUsername() {
		// Given
		Trainer trainer1 = new Trainer(996L, "Same", "Person", "Yoga");
		Trainer trainer2 = new Trainer(995L, "Same", "Person", "Pilates");

		// When
		gymFacade.createTrainer(trainer1);
		gymFacade.createTrainer(trainer2);

		// Then
		assertThat(trainer1.getUsername()).isEqualTo("Same.Person");
		assertThat(trainer2.getUsername()).isEqualTo("Same.Person2");
	}

	@Test
	void createTrainingType_shouldCreateTrainingType() {
		// Given
		// Assuming TrainingType(id, name, trainerId, trainingId)
		TrainingType type = new TrainingType(992L, "High Intensity", 100L, 200L);

		// When
		gymFacade.createTrainingType(type);

		// Then
		TrainingType retrieved = gymFacade.getTrainingType(992L);
		assertThat(retrieved).isNotNull();
		assertThat(retrieved.getTrainingNameType()).isEqualTo("High Intensity");
	}

	@Test
	void getTrainingType_shouldReturnTrainingTypeWhenExists() {
		// Given
		TrainingType type = new TrainingType(991L, "Low Impact", 100L, 200L);
		gymFacade.createTrainingType(type);

		// When
		TrainingType retrieved = gymFacade.getTrainingType(991L);

		// Then
		assertThat(retrieved).isEqualTo(type);
	}

	@Test
	void updateTrainingType_shouldUpdateExistingTrainingType() {
		// Given
		TrainingType type = new TrainingType(990L, "Strength", 100L, 200L);
		gymFacade.createTrainingType(type);

		// Modify the type name
		type.setTrainingNameType("Functional Fitness");

		// When
		gymFacade.updateTrainingType(type);

		// Then
		TrainingType updated = gymFacade.getTrainingType(990L);
		assertThat(updated.getTrainingNameType()).isEqualTo("Functional Fitness");
	}

	@Test
	void deleteTrainingType_shouldRemoveTrainingType() {
		// Given
		TrainingType type = new TrainingType(989L, "Spinning", 100L, 200L);
		gymFacade.createTrainingType(type);

		// When
		gymFacade.deleteTrainingType(989L);

		// Then
		TrainingType deleted = gymFacade.getTrainingType(989L);
		assertThat(deleted).isNull();
	}

	@Test
	void getAllTrainingTypes_shouldReturnAllTrainingTypes() {
		// Given: Ensure at least one type exists for the assertion to be meaningful
		TrainingType type = new TrainingType(988L, "Pilates", 100L, 200L);
		gymFacade.createTrainingType(type);

		// When
		Collection<TrainingType> types = gymFacade.getAllTrainingTypes();

		// Then
		assertThat(types).isNotEmpty();
		assertThat(types).anyMatch(t -> t.getId() == 988L);
	}

}
