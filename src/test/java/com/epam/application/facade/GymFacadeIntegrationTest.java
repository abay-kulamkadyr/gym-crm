package com.epam.application.facade;

import static org.assertj.core.api.Assertions.assertThat;

import com.epam.application.facade.GymFacade;
import com.epam.domain.model.Trainee;
import com.epam.domain.model.Trainer;
import com.epam.domain.model.Training;
import com.epam.domain.model.TrainingType;
import com.epam.infrastructure.config.AppConfig;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AppConfig.class)
class GymFacadeIntegrationTest {

	private long idGenerator = 0;

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
		Optional<Trainee> retrieved = gymFacade.getTrainee(999L);
		assertThat(retrieved).isPresent();
		assertThat(retrieved.get().getUsername()).isEqualTo("Michael.Brown");
		assertThat(retrieved.get().getPassword()).isNotNull().hasSize(10);
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
		Optional<Trainee> updated = gymFacade.getTrainee(998L);
		assertThat(updated).isPresent().get().extracting(Trainee::getAddress).isEqualTo("999 New Address");
	}

	@Test
	void deleteTrainee_shouldRemoveTrainee() {
		// Given
		Trainee trainee = new Trainee(997L, "Tom", "Davis", LocalDate.of(1991, 12, 5));
		gymFacade.createTrainee(trainee);

		// When
		gymFacade.deleteTrainee(997L);

		// Then
		Optional<Trainee> deleted = gymFacade.getTrainee(997L);
		assertThat(deleted).isEmpty();
	}

	@Test
	void createTrainer_shouldCreateTrainerWithGeneratedCredentials() {
		// Given
		Trainer trainer = new Trainer(999L, "Emma", "Taylor", "CrossFit");

		// When
		gymFacade.createTrainer(trainer);

		// Then
		Optional<Trainer> retrieved = gymFacade.getTrainer(999L);
		assertThat(retrieved).isPresent();
		assertThat(retrieved).get().satisfies(extractedTrainer -> {
			assertThat(extractedTrainer.getUsername()).isEqualTo("Emma.Taylor");
			assertThat(extractedTrainer.getPassword()).isNotNull().hasSize(10);
		});
	}

	@Test
	void updateTrainer_shouldUpdateExistingTrainer() {
		// Given
		long id = getNextUniqueId();
		Trainer trainer = new Trainer(id, "David", "Martinez", "Swimming");
		gymFacade.createTrainer(trainer);
		trainer.setActive(false); // Update the active status

		// When
		gymFacade.updateTrainer(trainer);

		// Then
		assertThat(gymFacade.getTrainer(id)).as("Checking that the updated trainer is present and inactive")
			.isPresent()
			.get()
			.extracting(Trainer::isActive)
			.isEqualTo(false);
		assertThat(gymFacade.getTrainer(id)).get().extracting(Trainer::getFirstName).isEqualTo("David");
	}

	@Test
	void deleteTrainer_shouldRemoveTrainer() {
		// Given
		long trainerIdToDelete = 987L;
		Trainer trainer = new Trainer(trainerIdToDelete, "Henry", "Cavill", "Weightlifting");
		gymFacade.createTrainer(trainer);
		assertThat(gymFacade.getTrainer(trainerIdToDelete)).isNotNull();

		// When
		gymFacade.deleteTrainer(trainerIdToDelete);

		// Then
		Optional<Trainer> deleted = gymFacade.getTrainer(trainerIdToDelete);
		assertThat(deleted).isEmpty();
	}

	@Test
	void createTraining_shouldCreateTraining() {
		// Given
		Training training = new Training(999L, 101L, 201L, LocalDate.of(2025, 10, 20), Duration.ofHours(1));

		// When
		gymFacade.createTraining(training);

		// Then
		Optional<Training> retrieved = gymFacade.getTraining(999L);
		assertThat(retrieved).isPresent();
		assertThat(retrieved.get().getTrainerId()).isEqualTo(101L);
		assertThat(retrieved.get().getTraineeId()).isEqualTo(201L);
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
		Optional<Training> updated = gymFacade.getTraining(994L);
		assertThat(updated).isPresent();
		assertThat(updated.get().getTrainingDate()).isEqualTo(LocalDate.of(2025, 12, 25));
	}

	@Test
	void deleteTraining_shouldRemoveTraining() {
		// Given
		Training training = new Training(993L, 101L, 201L, LocalDate.of(2025, 10, 20), Duration.ofHours(1));
		gymFacade.createTraining(training);

		// When
		gymFacade.deleteTraining(993L);

		// Then
		Optional<Training> deleted = gymFacade.getTraining(993L);
		assertThat(deleted).isEmpty();
	}

	@Test
	void createTrainee_withDuplicateName_shouldGenerateUniqueUsername() {
		// Given
		Trainee trainee1 = new Trainee(996L, "Duplicate", "Name", LocalDate.of(1990, 1, 1));
		Trainee trainee2 = new Trainee(995L, "Duplicate", "Name", LocalDate.of(1991, 2, 2));

		// When
		gymFacade.createTrainee(trainee1);
		gymFacade.createTrainee(trainee2);
		Optional<Trainee> trainee1Result = gymFacade.getTrainee(trainee1.getUserId());
		Optional<Trainee> trainee2Result = gymFacade.getTrainee(trainee2.getUserId());

		// Then
		assertThat(trainee1Result).isPresent();
		assertThat(trainee1Result.get().getUsername()).isEqualTo("Duplicate.Name");
		assertThat(trainee2Result).isPresent();
		assertThat(trainee2Result.get().getUsername()).isEqualTo("Duplicate.Name1");
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
		assertThat(trainer2.getUsername()).isEqualTo("Same.Person1");
	}

	@Test
	void createTrainingType_shouldCreateTrainingType() {
		// Given
		TrainingType type = new TrainingType(992L, "High Intensity", 100L, 200L);

		// When
		gymFacade.createTrainingType(type);

		// Then
		Optional<TrainingType> retrieved = gymFacade.getTrainingType(992L);
		assertThat(retrieved).isPresent();
		assertThat(retrieved.get().getTrainingNameType()).isEqualTo("High Intensity");
	}

	@Test
	void getTrainingType_shouldReturnTrainingTypeWhenExists() {
		// Given
		TrainingType type = new TrainingType(991L, "Low Impact", 100L, 200L);
		gymFacade.createTrainingType(type);

		// When
		Optional<TrainingType> retrieved = gymFacade.getTrainingType(991L);

		// Then
		assertThat(retrieved).get().isEqualTo(type);
	}

	@Test
	void updateTrainingType_shouldUpdateExistingTrainingType() {
		// Given
		TrainingType type = new TrainingType(990L, "Strength", 100L, 200L);
		gymFacade.createTrainingType(type);
		type.setTrainingNameType("Functional Fitness");

		// When
		gymFacade.updateTrainingType(type);

		// Then
		Optional<TrainingType> updated = gymFacade.getTrainingType(990L);
		assertThat(updated).isPresent();
		assertThat(updated.get().getTrainingNameType()).isEqualTo("Functional Fitness");
	}

	@Test
	void deleteTrainingType_shouldRemoveTrainingType() {
		// Given
		TrainingType type = new TrainingType(989L, "Spinning", 100L, 200L);
		gymFacade.createTrainingType(type);

		// When
		gymFacade.deleteTrainingType(989L);

		// Then
		Optional<TrainingType> deleted = gymFacade.getTrainingType(989L);
		assertThat(deleted).isEmpty();
	}

	long getNextUniqueId() {
		idGenerator++;
		return idGenerator;
	}

}
