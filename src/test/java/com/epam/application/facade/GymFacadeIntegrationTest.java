package com.epam.application.facade;

import static org.assertj.core.api.Assertions.assertThat;

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

	@Autowired
	private GymFacade gymFacade;

	@Test
	void contextLoads() {
		assertThat(gymFacade).isNotNull();
	}

	@Test
	void createTrainee_shouldCreateTraineeWithGeneratedCredentials() {
		// Given
		Trainee trainee = new Trainee(null, "Michael", "Brown", LocalDate.of(1995, 3, 10));

		// When
		gymFacade.createTrainee(trainee);

		// Then
		assertThat(trainee.getUserId()).isNotNull();
		Optional<Trainee> retrieved = gymFacade.getTrainee(trainee.getUserId());
		assertThat(retrieved).isPresent();
		assertThat(retrieved.get().getUsername()).isEqualTo("Michael.Brown");
		assertThat(retrieved.get().getPassword()).isNotNull().hasSize(10);
	}

	@Test
	void updateTrainee_shouldUpdateExistingTrainee() {
		// Given
		Trainee trainee = new Trainee(null, "Sarah", "Wilson", LocalDate.of(1993, 8, 22));
		gymFacade.createTrainee(trainee);
		Long generatedId = trainee.getUserId();
		trainee.setAddress("999 New Address");

		// When
		gymFacade.updateTrainee(trainee);

		// Then
		Optional<Trainee> updated = gymFacade.getTrainee(generatedId);
		assertThat(updated).isPresent().get().extracting(Trainee::getAddress).isEqualTo("999 New Address");
	}

	@Test
	void deleteTrainee_shouldRemoveTrainee() {
		// Given
		Trainee trainee = new Trainee(null, "Tom", "Davis", LocalDate.of(1991, 12, 5));
		gymFacade.createTrainee(trainee);
		Long generatedId = trainee.getUserId();

		// When
		gymFacade.deleteTrainee(generatedId);

		// Then
		Optional<Trainee> deleted = gymFacade.getTrainee(generatedId);
		assertThat(deleted).isEmpty();
	}

	@Test
	void createTrainer_shouldCreateTrainerWithGeneratedCredentials() {
		// Given
		Trainer trainer = new Trainer(null, "Emma", "Taylor", "CrossFit");

		// When
		gymFacade.createTrainer(trainer);

		// Then
		assertThat(trainer.getUserId()).isNotNull();
		Optional<Trainer> retrieved = gymFacade.getTrainer(trainer.getUserId());
		assertThat(retrieved).isPresent();
		assertThat(retrieved).get().satisfies(extractedTrainer -> {
			assertThat(extractedTrainer.getUsername()).isEqualTo("Emma.Taylor");
			assertThat(extractedTrainer.getPassword()).isNotNull().hasSize(10);
		});
	}

	@Test
	void updateTrainer_shouldUpdateExistingTrainer() {
		// Given
		Trainer trainer = new Trainer(null, "David", "Martinez", "Swimming");
		gymFacade.createTrainer(trainer);
		Long generatedId = trainer.getUserId();
		trainer.setActive(false);

		// When
		gymFacade.updateTrainer(trainer);

		// Then
		assertThat(gymFacade.getTrainer(generatedId)).as("Checking that the updated trainer is present and inactive")
			.isPresent()
			.get()
			.extracting(Trainer::isActive)
			.isEqualTo(false);
		assertThat(gymFacade.getTrainer(generatedId)).get().extracting(Trainer::getFirstName).isEqualTo("David");
	}

	@Test
	void deleteTrainer_shouldRemoveTrainer() {
		// Given
		Trainer trainer = new Trainer(null, "Henry", "Cavill", "Weightlifting");
		gymFacade.createTrainer(trainer);
		Long generatedId = trainer.getUserId();
		assertThat(gymFacade.getTrainer(generatedId)).isPresent();

		// When
		gymFacade.deleteTrainer(generatedId);

		// Then
		Optional<Trainer> deleted = gymFacade.getTrainer(generatedId);
		assertThat(deleted).isEmpty();
	}

	@Test
	void createTraining_shouldCreateTraining() {
		// Given
		Trainer trainer = new Trainer(null, "John", "Trainer", "Fitness");
		Trainee trainee = new Trainee(null, "Jane", "Trainee", LocalDate.of(1990, 1, 1));
		gymFacade.createTrainer(trainer);
		gymFacade.createTrainee(trainee);

		Training training = new Training(null, trainer.getUserId(), trainee.getUserId(), LocalDate.of(2025, 10, 20),
				Duration.ofHours(1));

		// When
		gymFacade.createTraining(training);

		// Then
		assertThat(training.getTrainingId()).isNotNull();
		Optional<Training> retrieved = gymFacade.getTraining(training.getTrainingId());
		assertThat(retrieved).isPresent();
		assertThat(retrieved.get().getTrainerId()).isEqualTo(trainer.getUserId());
		assertThat(retrieved.get().getTraineeId()).isEqualTo(trainee.getUserId());
	}

	@Test
	void updateTraining_shouldUpdateExistingTraining() {
		// Given
		Trainer trainer = new Trainer(null, "Alex", "Coach", "Boxing");
		Trainee trainee = new Trainee(null, "Bob", "Student", LocalDate.of(1992, 5, 15));
		gymFacade.createTrainer(trainer);
		gymFacade.createTrainee(trainee);

		Training training = new Training(null, trainer.getUserId(), trainee.getUserId(), LocalDate.of(2025, 10, 20),
				Duration.ofHours(1));
		gymFacade.createTraining(training);
		Long generatedId = training.getTrainingId();
		training.setTrainingDate(LocalDate.of(2025, 12, 25));

		// When
		gymFacade.updateTraining(training);

		// Then
		Optional<Training> updated = gymFacade.getTraining(generatedId);
		assertThat(updated).isPresent();
		assertThat(updated.get().getTrainingDate()).isEqualTo(LocalDate.of(2025, 12, 25));
	}

	@Test
	void deleteTraining_shouldRemoveTraining() {
		// Given
		Trainer trainer = new Trainer(null, "Chris", "Instructor", "Yoga");
		Trainee trainee = new Trainee(null, "Diana", "Member", LocalDate.of(1988, 7, 20));
		gymFacade.createTrainer(trainer);
		gymFacade.createTrainee(trainee);

		Training training = new Training(null, trainer.getUserId(), trainee.getUserId(), LocalDate.of(2025, 10, 20),
				Duration.ofHours(1));
		gymFacade.createTraining(training);
		Long generatedId = training.getTrainingId();

		// When
		gymFacade.deleteTraining(generatedId);

		// Then
		Optional<Training> deleted = gymFacade.getTraining(generatedId);
		assertThat(deleted).isEmpty();
	}

	@Test
	void createTrainee_withDuplicateName_shouldGenerateUniqueUsername() {
		// Given
		Trainee trainee1 = new Trainee(null, "Duplicate", "Name", LocalDate.of(1990, 1, 1));
		Trainee trainee2 = new Trainee(null, "Duplicate", "Name", LocalDate.of(1991, 2, 2));

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
		Trainer trainer1 = new Trainer(null, "Same", "Person", "Yoga");
		Trainer trainer2 = new Trainer(null, "Same", "Person", "Pilates");

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
		Trainer trainer = new Trainer(null, "Fitness", "Pro", "General");
		Trainee trainee = new Trainee(null, "Gym", "Goer", LocalDate.of(1985, 3, 12));
		gymFacade.createTrainer(trainer);
		gymFacade.createTrainee(trainee);

		TrainingType type = new TrainingType(null, "High Intensity", trainer.getUserId(), trainee.getUserId());

		// When
		gymFacade.createTrainingType(type);

		// Then
		assertThat(type.getTrainingTypeId()).isNotNull();
		Optional<TrainingType> retrieved = gymFacade.getTrainingType(type.getTrainingTypeId());
		assertThat(retrieved).isPresent();
		assertThat(retrieved.get().getTrainingNameType()).isEqualTo("High Intensity");
	}

	@Test
	void getTrainingType_shouldReturnTrainingTypeWhenExists() {
		// Given
		Trainer trainer = new Trainer(null, "Sports", "Coach", "Athletics");
		Trainee trainee = new Trainee(null, "Athlete", "Runner", LocalDate.of(1987, 9, 18));
		gymFacade.createTrainer(trainer);
		gymFacade.createTrainee(trainee);

		TrainingType type = new TrainingType(null, "Low Impact", trainer.getUserId(), trainee.getUserId());
		gymFacade.createTrainingType(type);

		// When
		Optional<TrainingType> retrieved = gymFacade.getTrainingType(type.getTrainingTypeId());

		// Then
		assertThat(retrieved).isPresent();
		assertThat(retrieved.get().getTrainingNameType()).isEqualTo("Low Impact");
	}

	@Test
	void updateTrainingType_shouldUpdateExistingTrainingType() {
		// Given
		Trainer trainer = new Trainer(null, "Power", "Lifter", "Strength");
		Trainee trainee = new Trainee(null, "Strong", "Person", LocalDate.of(1989, 11, 25));
		gymFacade.createTrainer(trainer);
		gymFacade.createTrainee(trainee);

		TrainingType type = new TrainingType(null, "Strength", trainer.getUserId(), trainee.getUserId());
		gymFacade.createTrainingType(type);
		Long generatedId = type.getTrainingTypeId();
		type.setTrainingNameType("Functional Fitness");

		// When
		gymFacade.updateTrainingType(type);

		// Then
		Optional<TrainingType> updated = gymFacade.getTrainingType(generatedId);
		assertThat(updated).isPresent();
		assertThat(updated.get().getTrainingNameType()).isEqualTo("Functional Fitness");
	}

	@Test
	void deleteTrainingType_shouldRemoveTrainingType() {
		// Given
		Trainer trainer = new Trainer(null, "Spin", "Master", "Cycling");
		Trainee trainee = new Trainee(null, "Bike", "Rider", LocalDate.of(1986, 4, 8));
		gymFacade.createTrainer(trainer);
		gymFacade.createTrainee(trainee);

		TrainingType type = new TrainingType(null, "Spinning", trainer.getUserId(), trainee.getUserId());
		gymFacade.createTrainingType(type);
		Long generatedId = type.getTrainingTypeId();

		// When
		gymFacade.deleteTrainingType(generatedId);

		// Then
		Optional<TrainingType> deleted = gymFacade.getTrainingType(generatedId);
		assertThat(deleted).isEmpty();
	}

}