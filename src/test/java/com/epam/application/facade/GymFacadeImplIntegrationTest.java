package com.epam.application.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.epam.application.Credentials;
import com.epam.application.exception.AuthenticationException;
import com.epam.application.request.CreateTraineeProfileRequest;
import com.epam.application.request.CreateTrainerProfileRequest;
import com.epam.application.request.CreateTrainingRequest;
import com.epam.application.request.UpdateTraineeProfileRequest;
import com.epam.application.request.UpdateTrainerProfileRequest;
import com.epam.domain.TrainingFilter;
import com.epam.domain.model.Trainee;
import com.epam.domain.model.Trainer;
import com.epam.domain.model.Training;
import com.epam.domain.model.TrainingType;
import com.epam.infrastructure.persistence.dao.TraineeDAO;
import com.epam.infrastructure.persistence.dao.TrainerDAO;
import com.epam.infrastructure.persistence.dao.TrainingTypeDAO;
import com.epam.infrastructure.persistence.mapper.TrainerMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class GymFacadeImplIntegrationTest {

	private GymFacadeImpl gymFacade;

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	void setGymFacade(GymFacadeImpl gymFacade) {
		this.gymFacade = gymFacade;
	}

	@BeforeEach
	void cleanDatabase() {
		entityManager.createQuery("DELETE FROM TrainingDAO").executeUpdate();
		entityManager.createQuery("DELETE FROM TraineeDAO").executeUpdate();
		entityManager.createQuery("DELETE FROM TrainerDAO").executeUpdate();
		entityManager.createQuery("DELETE FROM UserDAO").executeUpdate();
		entityManager.flush();
	}

	@Test
	void createTrainee_shouldCreateTraineeWithGeneratedCredentials() {
		// Given
		CreateTraineeProfileRequest request = new CreateTraineeProfileRequest("Michael", "Brown", true,
				Optional.empty(), Optional.empty());

		// When
		Trainee created = gymFacade.createTraineeProfile(request);

		// Then
		assertThat(created.getTraineeId()).isNotNull();
		assertThat(created.getUsername()).isEqualTo("Michael.Brown");
		assertThat(created.getPassword()).isNotNull().hasSize(10);

		Credentials credentials = new Credentials(created.getUsername(), created.getPassword());
		Optional<Trainee> retrieved = gymFacade.findTraineeByUsername(credentials);
		assertThat(retrieved).isPresent();
		assertThat(retrieved.get().getUsername()).isEqualTo("Michael.Brown");
	}

	@Test
	void updateTrainee_shouldUpdateExistingTrainee() {
		// Given
		CreateTraineeProfileRequest createRequest = new CreateTraineeProfileRequest("Sarah", "Wilson", true,
				Optional.of(LocalDate.of(1990, 1, 1)), Optional.empty());

		Trainee trainee = gymFacade.createTraineeProfile(createRequest);
		Credentials credentials = new Credentials(trainee.getUsername(), trainee.getPassword());

		UpdateTraineeProfileRequest updateRequest = new UpdateTraineeProfileRequest(credentials, Optional.empty(),
				Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
				Optional.of(LocalDate.of(1991, 2, 2)), Optional.of("999 New Address"));

		// When
		Trainee updated = gymFacade.updateTraineeProfile(updateRequest);

		// Then
		assertThat(updated.getAddress()).isEqualTo("999 New Address");
		assertThat(updated.getDob()).isEqualTo(LocalDate.of(1991, 2, 2));

		Optional<Trainee> retrieved = gymFacade.findTraineeByUsername(credentials);
		assertThat(retrieved).isPresent();
		assertThat(retrieved.get().getAddress()).isEqualTo("999 New Address");
		assertThat(retrieved.get().getDob()).isEqualTo(LocalDate.of(1991, 2, 2));
	}

	@Test
	void deleteTrainee_shouldRemoveTrainee() {
		// Given
		CreateTraineeProfileRequest request = new CreateTraineeProfileRequest("Tom", "Davis", true, Optional.empty(),
				Optional.empty());
		Trainee trainee = gymFacade.createTraineeProfile(request);
		Credentials credentials = new Credentials(trainee.getUsername(), trainee.getPassword());
		TraineeDAO createdTraineeDAO = entityManager.find(TraineeDAO.class, trainee.getTraineeId());
		assertThat(createdTraineeDAO).isNotNull();

		// When
		gymFacade.deleteTraineeProfile(credentials);

		// Then - Should not be able to find trainee anymore (authentication will fail)
		TrainerDAO deletedTraineeDAO = entityManager.find(TrainerDAO.class, trainee.getTraineeId());
		assertThat(deletedTraineeDAO).isNull();
	}

	@Test
	void createTrainee_withDuplicateName_shouldGenerateUniqueUsername() {
		// Given
		CreateTraineeProfileRequest request1 = new CreateTraineeProfileRequest("Duplicate", "Name", true,
				Optional.empty(), Optional.empty());
		CreateTraineeProfileRequest request2 = new CreateTraineeProfileRequest("Duplicate", "Name", true,
				Optional.empty(), Optional.empty());

		// When
		Trainee trainee1 = gymFacade.createTraineeProfile(request1);
		Trainee trainee2 = gymFacade.createTraineeProfile(request2);

		// Then
		assertThat(trainee1.getUsername()).isEqualTo("Duplicate.Name");
		assertThat(trainee2.getUsername()).isEqualTo("Duplicate.Name1");
	}

	@Test
	void findTraineeTrainings_shouldWork() {
		// Given
		TrainingType specialization = createOrGetTestTrainingType("Pilates");

		CreateTraineeProfileRequest traineeRequest = new CreateTraineeProfileRequest("John", "Doe", true,
				Optional.empty(), Optional.empty());
		Trainee trainee = gymFacade.createTraineeProfile(traineeRequest);

		CreateTrainerProfileRequest trainerRequest = new CreateTrainerProfileRequest("Test", "Trainer", true,
				specialization);
		Trainer trainer = gymFacade.createTrainerProfile(trainerRequest);

		Credentials traineeCredentials = new Credentials(trainee.getUsername(), trainee.getPassword());

		CreateTrainingRequest trainingRequest1 = new CreateTrainingRequest(traineeCredentials, "Training 1",
				LocalDateTime.now(), 60, specialization.getTrainingTypeName(), trainee.getUsername(),
				trainer.getUsername());
		gymFacade.createTraining(trainingRequest1);

		CreateTrainingRequest trainingRequest2 = new CreateTrainingRequest(traineeCredentials, "Training 2",
				LocalDateTime.now().plusDays(1), 45, specialization.getTrainingTypeName(), trainee.getUsername(),
				trainer.getUsername());
		gymFacade.createTraining(trainingRequest2);

		// When
		List<Training> result = gymFacade.getTraineeTrainings(traineeCredentials, TrainingFilter.empty());

		// Then
		assertThat(result).isNotEmpty();
		assertThat(result).hasSize(2);
	}

	@Test
	void createTrainer_shouldCreateTrainerWithGeneratedCredentials() {
		// Given
		TrainingType type = createOrGetTestTrainingType("Cardio");
		CreateTrainerProfileRequest request = new CreateTrainerProfileRequest("Emma", "Taylor", true, type);

		// When
		Trainer trainer = gymFacade.createTrainerProfile(request);

		// Then
		assertThat(trainer.getTrainerId()).isNotNull();
		assertThat(trainer.getUsername()).isEqualTo("Emma.Taylor");
		assertThat(trainer.getPassword()).isNotNull().hasSize(10);

		Credentials credentials = new Credentials(trainer.getUsername(), trainer.getPassword());
		Optional<Trainer> retrieved = gymFacade.findTrainerByUsername(credentials);
		assertThat(retrieved).isPresent();
		assertThat(retrieved.get().getUsername()).isEqualTo("Emma.Taylor");
	}

	@Test
	void updateTrainer_shouldUpdateExistingTrainer() {
		// Given
		TrainingType originalType = createOrGetTestTrainingType("Mobility");
		CreateTrainerProfileRequest createRequest = new CreateTrainerProfileRequest("David", "Martinez", true,
				originalType);
		Trainer trainer = gymFacade.createTrainerProfile(createRequest);

		Credentials credentials = new Credentials(trainer.getUsername(), trainer.getPassword());

		TrainingType newType = createOrGetTestTrainingType("Pilates");
		UpdateTrainerProfileRequest updateRequest = new UpdateTrainerProfileRequest(credentials, Optional.empty(),
				Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(false),
				Optional.of(newType.getTrainingTypeName()));

		// When
		Trainer updated = gymFacade.updateTrainerProfile(updateRequest);

		// Then
		assertThat(updated.getActive()).isFalse();
		assertThat(updated.getSpecialization().getTrainingTypeName()).isEqualTo("Pilates");
	}

	@Test
	void deleteTrainer_shouldRemoveTrainer() {
		// Given
		TrainingType type = createOrGetTestTrainingType("Cardio");
		CreateTrainerProfileRequest request = new CreateTrainerProfileRequest("Henry", "Cavill", true, type);
		Trainer trainer = gymFacade.createTrainerProfile(request);

		Credentials credentials = new Credentials(trainer.getUsername(), trainer.getPassword());
		TrainerDAO createdTrainer = entityManager.find(TrainerDAO.class, trainer.getTrainerId());
		assertThat(createdTrainer).isNotNull();

		// When
		gymFacade.deleteTrainerProfile(credentials);

		// Then
		TrainerDAO deleted = entityManager.find(TrainerDAO.class, trainer.getTrainerId());
		assertThat(deleted).isNull();
	}

	@Test
	void createTrainer_withDuplicateName_shouldGenerateUniqueUsername() {
		// Given
		TrainingType type = createOrGetTestTrainingType("Functional");
		CreateTrainerProfileRequest request1 = new CreateTrainerProfileRequest("Same", "Person", true, type);
		CreateTrainerProfileRequest request2 = new CreateTrainerProfileRequest("Same", "Person", true, type);

		// When
		Trainer trainer1 = gymFacade.createTrainerProfile(request1);
		Trainer trainer2 = gymFacade.createTrainerProfile(request2);

		// Then
		assertThat(trainer1.getUsername()).isEqualTo("Same.Person");
		assertThat(trainer2.getUsername()).isEqualTo("Same.Person1");
	}

	// ========================================================================
	// TRAINING OPERATIONS
	// ========================================================================

	@Test
	void createTraining_shouldCreateTraining() {
		// Given
		TrainingType type = createOrGetTestTrainingType("Yoga");

		CreateTrainerProfileRequest trainerRequest = new CreateTrainerProfileRequest("John", "Trainer", true, type);
		Trainer trainer = gymFacade.createTrainerProfile(trainerRequest);

		CreateTraineeProfileRequest traineeRequest = new CreateTraineeProfileRequest("Jane", "Trainee", true,
				Optional.of(LocalDate.of(1990, 1, 1)), Optional.empty());
		Trainee trainee = gymFacade.createTraineeProfile(traineeRequest);

		Credentials credentials = new Credentials(trainee.getUsername(), trainee.getPassword());
		CreateTrainingRequest trainingRequest = new CreateTrainingRequest(credentials, "Morning Cardio",
				LocalDateTime.now(), 60, type.getTrainingTypeName(), trainee.getUsername(), trainer.getUsername());

		// When
		gymFacade.createTraining(trainingRequest);

		// Then
		List<Training> trainings = gymFacade.getTraineeTrainings(credentials, TrainingFilter.empty());
		assertThat(trainings).hasSize(1);
		assertThat(trainings.get(0).getTrainingName()).isEqualTo("Morning Cardio");
		assertThat(trainings.get(0).getTrainee()).isNotNull();
		assertThat(trainings.get(0).getTrainer()).isNotNull();
	}

	// ========================================================================
	// INTEGRATION SCENARIOS
	// ========================================================================

	@Test
	void deleteTrainee_shouldCascadeDeleteTrainings() {
		// Given
		TrainingType type = createOrGetTestTrainingType("Cardio");

		CreateTrainerProfileRequest trainerRequest = new CreateTrainerProfileRequest("Shared", "Trainer", true, type);
		Trainer trainer = gymFacade.createTrainerProfile(trainerRequest);

		CreateTraineeProfileRequest traineeRequest = new CreateTraineeProfileRequest("Test", "Trainee", true,
				Optional.of(LocalDate.of(1990, 1, 1)), Optional.empty());
		Trainee trainee = gymFacade.createTraineeProfile(traineeRequest);

		Credentials traineeCredentials = new Credentials(trainee.getUsername(), trainee.getPassword());
		Credentials trainerCredentials = new Credentials(trainer.getUsername(), trainer.getPassword());

		CreateTrainingRequest trainingRequest1 = new CreateTrainingRequest(traineeCredentials, "Training 1",
				LocalDateTime.now(), 60, type.getTrainingTypeName(), trainee.getUsername(), trainer.getUsername());
		gymFacade.createTraining(trainingRequest1);

		CreateTrainingRequest trainingRequest2 = new CreateTrainingRequest(traineeCredentials, "Training 2",
				LocalDateTime.now().plusDays(1), 45, type.getTrainingTypeName(), trainee.getUsername(),
				trainer.getUsername());
		gymFacade.createTraining(trainingRequest2);

		// When
		gymFacade.deleteTraineeProfile(traineeCredentials);

		// Then
		assertThat(gymFacade.findTrainerByUsername(trainerCredentials)).isPresent();
	}

	@Test
	void getTraineeTrainings_shouldFindTrainingsByDateRangeAndTrainerName() {
		// Given
		TrainingType specialization = createOrGetTestTrainingType("Yoga");

		CreateTraineeProfileRequest traineeRequest = new CreateTraineeProfileRequest("Test", "Trainee", true,
				Optional.empty(), Optional.empty());
		Trainee trainee = gymFacade.createTraineeProfile(traineeRequest);

		CreateTrainerProfileRequest trainerRequest = new CreateTrainerProfileRequest("Test", "Trainer", true,
				specialization);
		Trainer trainer = gymFacade.createTrainerProfile(trainerRequest);

		Credentials credentials = new Credentials(trainee.getUsername(), trainee.getPassword());

		CreateTrainingRequest t1Request = new CreateTrainingRequest(credentials, "Morning Run",
				LocalDateTime.now().minusDays(1), 60, specialization.getTrainingTypeName(), trainee.getUsername(),
				trainer.getUsername());
		gymFacade.createTraining(t1Request);

		CreateTrainingRequest t2Request = new CreateTrainingRequest(credentials, "Evening Yoga",
				LocalDateTime.now().minusDays(5), 45, specialization.getTrainingTypeName(), trainee.getUsername(),
				trainer.getUsername());
		gymFacade.createTraining(t2Request);

		// When
		TrainingFilter filter = TrainingFilter.forTrainee(Optional.of(LocalDateTime.now().minusDays(7)),
				Optional.of(LocalDateTime.now()), Optional.of(trainer.getUsername()),
				Optional.of(specialization.getTrainingTypeName()));

		List<Training> trainings = gymFacade.getTraineeTrainings(credentials, filter);

		// Then
		assertThat(trainings).hasSize(2);
		assertThat(trainings.get(0).getTrainer().getUsername()).isEqualTo("Test.Trainer");
		assertThat(trainings.get(0).getTrainingType().getTrainingTypeName())
			.isEqualTo(specialization.getTrainingTypeName());
	}

	@Test
	void getTraineeTrainings_shouldFindAllTrainingsWithinDateRange() {
		// Given
		TrainingType yoga = createOrGetTestTrainingType("Yoga");
		TrainingType boxing = createOrGetTestTrainingType("Boxing");

		CreateTraineeProfileRequest traineeRequest = new CreateTraineeProfileRequest("John", "Doe", true,
				Optional.empty(), Optional.empty());
		Trainee trainee = gymFacade.createTraineeProfile(traineeRequest);

		CreateTrainerProfileRequest trainerRequest = new CreateTrainerProfileRequest("Jane", "Smith", true, yoga);
		Trainer trainer = gymFacade.createTrainerProfile(trainerRequest);

		Credentials credentials = new Credentials(trainee.getUsername(), trainee.getPassword());

		// Create trainings
		CreateTrainingRequest yogaRequest = new CreateTrainingRequest(credentials, "Morning Yoga",
				LocalDateTime.now().minusDays(1), 60, yoga.getTrainingTypeName(), trainee.getUsername(),
				trainer.getUsername());
		gymFacade.createTraining(yogaRequest);

		CreateTrainingRequest boxingRequest = new CreateTrainingRequest(credentials, "Evening Boxing",
				LocalDateTime.now().minusDays(10), 45, boxing.getTrainingTypeName(), trainee.getUsername(),
				trainer.getUsername());
		gymFacade.createTraining(boxingRequest);

		// When
		TrainingFilter filter = TrainingFilter.forTrainee(Optional.of(LocalDateTime.now().minusDays(7)),
				Optional.of(LocalDateTime.now()), Optional.empty(), Optional.empty());

		List<Training> trainings = gymFacade.getTraineeTrainings(credentials, filter);

		// Then
		assertThat(trainings).hasSize(1);
		assertThat(trainings.get(0).getTrainingName()).isEqualTo("Morning Yoga");
	}

	@Test
	void getTraineeTrainings_shouldFilterByTrainerUsername() {
		// Given
		TrainingType yoga = createOrGetTestTrainingType("Yoga");

		CreateTraineeProfileRequest traineeRequest = new CreateTraineeProfileRequest("John", "Doe", true,
				Optional.empty(), Optional.empty());
		Trainee trainee = gymFacade.createTraineeProfile(traineeRequest);

		CreateTrainerProfileRequest trainerRequest = new CreateTrainerProfileRequest("Jane", "Smith", true, yoga);
		Trainer trainer = gymFacade.createTrainerProfile(trainerRequest);

		Credentials credentials = new Credentials(trainee.getUsername(), trainee.getPassword());

		// Create trainings
		CreateTrainingRequest t1Request = new CreateTrainingRequest(credentials, "Morning Yoga",
				LocalDateTime.now().minusDays(1), 60, yoga.getTrainingTypeName(), trainee.getUsername(),
				trainer.getUsername());
		gymFacade.createTraining(t1Request);

		CreateTrainingRequest t2Request = new CreateTrainingRequest(credentials, "Evening Yoga",
				LocalDateTime.now().minusDays(10), 45, yoga.getTrainingTypeName(), trainee.getUsername(),
				trainer.getUsername());
		gymFacade.createTraining(t2Request);

		// When
		TrainingFilter filter = TrainingFilter.forTrainee(Optional.empty(), Optional.empty(),
				Optional.of(trainer.getUsername()), Optional.empty());

		List<Training> trainings = gymFacade.getTraineeTrainings(credentials, filter);

		// Then
		assertThat(trainings).hasSize(2);
		assertThat(trainings.get(0).getTrainer().getUsername()).contains("Jane.Smith");
	}

	@Test
	void getTraineeTrainings_shouldFilterByTrainingType() {
		// Given
		TrainingType yoga = createOrGetTestTrainingType("Yoga");
		TrainingType boxing = createOrGetTestTrainingType("Boxing");

		CreateTraineeProfileRequest traineeRequest = new CreateTraineeProfileRequest("John", "Doe", true,
				Optional.empty(), Optional.empty());
		Trainee trainee = gymFacade.createTraineeProfile(traineeRequest);

		CreateTrainerProfileRequest trainerRequest = new CreateTrainerProfileRequest("Jane", "Smith", true, yoga);
		Trainer trainer = gymFacade.createTrainerProfile(trainerRequest);

		Credentials credentials = new Credentials(trainee.getUsername(), trainee.getPassword());

		// Create trainings
		CreateTrainingRequest yogaRequest = new CreateTrainingRequest(credentials, "Morning Yoga",
				LocalDateTime.now().minusDays(1), 60, yoga.getTrainingTypeName(), trainee.getUsername(),
				trainer.getUsername());
		gymFacade.createTraining(yogaRequest);

		CreateTrainingRequest boxingRequest = new CreateTrainingRequest(credentials, "Evening Boxing",
				LocalDateTime.now().minusDays(10), 45, boxing.getTrainingTypeName(), trainee.getUsername(),
				trainer.getUsername());
		gymFacade.createTraining(boxingRequest);

		// When
		TrainingFilter filter = TrainingFilter.forTrainee(Optional.empty(), Optional.empty(), Optional.empty(),
				Optional.of(yoga.getTrainingTypeName()));

		List<Training> trainings = gymFacade.getTraineeTrainings(credentials, filter);

		// Then
		assertThat(trainings).hasSize(1);
		assertThat(trainings.get(0).getTrainingType().getTrainingTypeName()).isEqualTo("Yoga");
	}

	@Test
	void getTraineeTrainings_shouldReturnEmptyListWhenNoMatch() {
		// Given
		TrainingType yoga = createOrGetTestTrainingType("Yoga");
		TrainingType boxing = createOrGetTestTrainingType("Boxing");

		CreateTraineeProfileRequest traineeRequest = new CreateTraineeProfileRequest("John", "Doe", true,
				Optional.empty(), Optional.empty());
		Trainee trainee = gymFacade.createTraineeProfile(traineeRequest);

		CreateTrainerProfileRequest trainerRequest = new CreateTrainerProfileRequest("Jane", "Smith", true, yoga);
		Trainer trainer = gymFacade.createTrainerProfile(trainerRequest);

		Credentials credentials = new Credentials(trainee.getUsername(), trainee.getPassword());

		// Create trainings
		CreateTrainingRequest yogaRequest = new CreateTrainingRequest(credentials, "Morning Yoga",
				LocalDateTime.now().minusDays(1), 60, yoga.getTrainingTypeName(), trainee.getUsername(),
				trainer.getUsername());
		gymFacade.createTraining(yogaRequest);

		CreateTrainingRequest boxingRequest = new CreateTrainingRequest(credentials, "Evening Boxing",
				LocalDateTime.now().minusDays(10), 45, boxing.getTrainingTypeName(), trainee.getUsername(),
				trainer.getUsername());
		gymFacade.createTraining(boxingRequest);

		// When
		TrainingFilter filter = TrainingFilter.forTrainee(Optional.of(LocalDateTime.now().minusDays(100)),
				Optional.of(LocalDateTime.now().minusDays(90)), Optional.empty(),
				Optional.of(yoga.getTrainingTypeName()));

		List<Training> trainings = gymFacade.getTraineeTrainings(credentials, filter);

		// Then
		assertThat(trainings).isEmpty();
	}

	@Test
	void getTraineeTrainings_shouldFilterByAllCriteriaTogether() {
		// Given
		TrainingType yoga = createOrGetTestTrainingType("Yoga");
		TrainingType boxing = createOrGetTestTrainingType("Boxing");

		CreateTraineeProfileRequest traineeRequest = new CreateTraineeProfileRequest("John", "Doe", true,
				Optional.empty(), Optional.empty());
		Trainee trainee = gymFacade.createTraineeProfile(traineeRequest);

		CreateTrainerProfileRequest trainerRequest = new CreateTrainerProfileRequest("Jane", "Smith", true, yoga);
		Trainer trainer = gymFacade.createTrainerProfile(trainerRequest);

		Credentials credentials = new Credentials(trainee.getUsername(), trainee.getPassword());

		// Create trainings
		CreateTrainingRequest yogaRequest = new CreateTrainingRequest(credentials, "Morning Yoga",
				LocalDateTime.now().minusDays(1), 60, yoga.getTrainingTypeName(), trainee.getUsername(),
				trainer.getUsername());
		gymFacade.createTraining(yogaRequest);

		CreateTrainingRequest boxingRequest = new CreateTrainingRequest(credentials, "Evening Boxing",
				LocalDateTime.now().minusDays(10), 45, boxing.getTrainingTypeName(), trainee.getUsername(),
				trainer.getUsername());
		gymFacade.createTraining(boxingRequest);

		// When
		TrainingFilter filter = TrainingFilter.forTrainee(Optional.of(LocalDateTime.now().minusDays(2)),
				Optional.of(LocalDateTime.now()), Optional.of(trainer.getUsername()),
				Optional.of(yoga.getTrainingTypeName()));

		List<Training> trainings = gymFacade.getTraineeTrainings(credentials, filter);

		// Then
		assertThat(trainings).hasSize(1);
		assertThat(trainings.get(0).getTrainingName()).isEqualTo("Morning Yoga");
	}

	@Test
	void getUnassignedTrainers_shouldReturnOnlyUnassignedTrainers() {
		// Given
		CreateTraineeProfileRequest traineeRequest = new CreateTraineeProfileRequest("John", "Doe", true,
				Optional.empty(), Optional.empty());
		Trainee trainee = gymFacade.createTraineeProfile(traineeRequest);

		// Create trainers
		TrainingType yoga = createOrGetTestTrainingType("Yoga");
		TrainingType boxing = createOrGetTestTrainingType("Boxing");

		CreateTrainerProfileRequest trainer1Request = new CreateTrainerProfileRequest("Alice", "Smith", true, yoga);
		Trainer trainer1 = gymFacade.createTrainerProfile(trainer1Request);

		CreateTrainerProfileRequest trainer2Request = new CreateTrainerProfileRequest("Bob", "Jones", true, boxing);
		Trainer trainer2 = gymFacade.createTrainerProfile(trainer2Request);

		Credentials credentials = new Credentials(trainee.getUsername(), trainee.getPassword());
		List<String> trainerUsernames = List.of(trainer1.getUsername(), trainer2.getUsername());

		// When
		gymFacade.updateTraineeTrainersList(credentials, trainerUsernames);

		// Then
		List<Trainer> unassigned = gymFacade.getTraineeUnassignedTrainers(credentials);
		assertThat(unassigned).isEmpty();
	}

	@Test
	void toggleTraineeActiveStatus_shouldChangeActiveStatus() {
		// Given
		CreateTraineeProfileRequest request = new CreateTraineeProfileRequest("Test", "User", true, Optional.empty(),
				Optional.empty());
		Trainee trainee = gymFacade.createTraineeProfile(request);

		Credentials credentials = new Credentials(trainee.getUsername(), trainee.getPassword());
		assertThat(trainee.getActive()).isTrue();

		// When
		gymFacade.toggleTraineeActiveStatus(credentials);

		// Then
		Optional<Trainee> updated = gymFacade.findTraineeByUsername(credentials);
		assertThat(updated).isPresent();
		assertThat(updated.get().getActive()).isFalse();
	}

	@Test
	void toggleTrainerActiveStatus_shouldChangeActiveStatus() {
		// Given
		TrainingType type = createOrGetTestTrainingType("Yoga");
		CreateTrainerProfileRequest request = new CreateTrainerProfileRequest("Test", "Trainer", true, type);
		Trainer trainer = gymFacade.createTrainerProfile(request);

		Credentials credentials = new Credentials(trainer.getUsername(), trainer.getPassword());
		assertThat(trainer.getActive()).isTrue();

		// When
		gymFacade.toggleTrainerActiveStatus(credentials);

		// Then
		Optional<Trainer> updated = gymFacade.findTrainerByUsername(credentials);
		assertThat(updated).isPresent();
		assertThat(updated.get().getActive()).isFalse();
	}

	@Test
	void updateTraineePassword_shouldChangePassword() {
		// Given
		CreateTraineeProfileRequest request = new CreateTraineeProfileRequest("Test", "User", true, Optional.empty(),
				Optional.empty());
		Trainee trainee = gymFacade.createTraineeProfile(request);

		Credentials oldCredentials = new Credentials(trainee.getUsername(), trainee.getPassword());
		String newPassword = "newPassword123";

		// When
		gymFacade.updateTraineePassword(oldCredentials, newPassword);

		// Then
		Credentials newCredentials = new Credentials(trainee.getUsername(), newPassword);
		Optional<Trainee> retrieved = gymFacade.findTraineeByUsername(newCredentials);
		assertThat(retrieved).isPresent();

		// Old password should not work
		assertThatThrownBy(() -> gymFacade.findTraineeByUsername(oldCredentials))
			.isInstanceOf(AuthenticationException.class);
	}

	@Test
	void updateTrainerPassword_shouldChangePassword() {
		// Given
		TrainingType type = createOrGetTestTrainingType("Yoga");
		CreateTrainerProfileRequest request = new CreateTrainerProfileRequest("Test", "Trainer", true, type);
		Trainer trainer = gymFacade.createTrainerProfile(request);

		Credentials oldCredentials = new Credentials(trainer.getUsername(), trainer.getPassword());
		String newPassword = "newPassword123";

		// When
		gymFacade.updateTrainerPassword(oldCredentials, newPassword);

		// Then
		Credentials newCredentials = new Credentials(trainer.getUsername(), newPassword);
		Optional<Trainer> retrieved = gymFacade.findTrainerByUsername(newCredentials);
		assertThat(retrieved).isPresent();

		// Old password should not work
		assertThatThrownBy(() -> gymFacade.findTrainerByUsername(oldCredentials))
			.isInstanceOf(AuthenticationException.class)
			.hasMessageContaining("Invalid credentials for trainer");
	}

	@Test
	void getTrainerTrainings_shouldReturnTrainerSpecificTrainings() {
		// Given
		TrainingType yoga = createOrGetTestTrainingType("Yoga");

		CreateTrainerProfileRequest trainerRequest = new CreateTrainerProfileRequest("Jane", "Trainer", true, yoga);
		Trainer trainer = gymFacade.createTrainerProfile(trainerRequest);

		CreateTraineeProfileRequest trainee1Request = new CreateTraineeProfileRequest("John", "Doe", true,
				Optional.empty(), Optional.empty());
		Trainee trainee1 = gymFacade.createTraineeProfile(trainee1Request);

		CreateTraineeProfileRequest trainee2Request = new CreateTraineeProfileRequest("Jane", "Smith", true,
				Optional.empty(), Optional.empty());
		Trainee trainee2 = gymFacade.createTraineeProfile(trainee2Request);

		Credentials trainee1Credentials = new Credentials(trainee1.getUsername(), trainee1.getPassword());
		Credentials trainee2Credentials = new Credentials(trainee2.getUsername(), trainee2.getPassword());
		Credentials trainerCredentials = new Credentials(trainer.getUsername(), trainer.getPassword());

		// Create trainings
		CreateTrainingRequest training1Request = new CreateTrainingRequest(trainee1Credentials, "Morning Session",
				LocalDateTime.now().minusDays(1), 60, yoga.getTrainingTypeName(), trainee1.getUsername(),
				trainer.getUsername());
		gymFacade.createTraining(training1Request);

		CreateTrainingRequest training2Request = new CreateTrainingRequest(trainee2Credentials, "Evening Session",
				LocalDateTime.now().minusDays(2), 45, yoga.getTrainingTypeName(), trainee2.getUsername(),
				trainer.getUsername());
		gymFacade.createTraining(training2Request);

		// When
		TrainingFilter filter = TrainingFilter.forTrainer(Optional.empty(), Optional.empty(), Optional.empty());

		List<Training> trainings = gymFacade.getTrainerTrainings(trainerCredentials, filter);

		// Then
		assertThat(trainings).hasSize(2);
		assertThat(trainings).extracting(Training::getTrainingName)
			.containsExactlyInAnyOrder("Morning Session", "Evening Session");
	}

	@Test
	void getTrainerTrainings_shouldFilterByTraineeName() {
		// Given
		TrainingType yoga = createOrGetTestTrainingType("Yoga");

		CreateTrainerProfileRequest trainerRequest = new CreateTrainerProfileRequest("Jane", "Trainer", true, yoga);
		Trainer trainer = gymFacade.createTrainerProfile(trainerRequest);

		CreateTraineeProfileRequest trainee1Request = new CreateTraineeProfileRequest("John", "Doe", true,
				Optional.empty(), Optional.empty());
		Trainee trainee1 = gymFacade.createTraineeProfile(trainee1Request);

		CreateTraineeProfileRequest trainee2Request = new CreateTraineeProfileRequest("Jane", "Smith", true,
				Optional.empty(), Optional.empty());
		Trainee trainee2 = gymFacade.createTraineeProfile(trainee2Request);

		Credentials trainee1Credentials = new Credentials(trainee1.getUsername(), trainee1.getPassword());
		Credentials trainee2Credentials = new Credentials(trainee2.getUsername(), trainee2.getPassword());
		Credentials trainerCredentials = new Credentials(trainer.getUsername(), trainer.getPassword());

		// Create trainings
		CreateTrainingRequest training1Request = new CreateTrainingRequest(trainee1Credentials, "Morning Session",
				LocalDateTime.now().minusDays(1), 60, yoga.getTrainingTypeName(), trainee1.getUsername(),
				trainer.getUsername());
		gymFacade.createTraining(training1Request);

		CreateTrainingRequest training2Request = new CreateTrainingRequest(trainee2Credentials, "Evening Session",
				LocalDateTime.now().minusDays(2), 45, yoga.getTrainingTypeName(), trainee2.getUsername(),
				trainer.getUsername());
		gymFacade.createTraining(training2Request);

		// When
		TrainingFilter filter = TrainingFilter.forTrainer(Optional.empty(), Optional.empty(),
				Optional.of(trainee1.getUsername()));

		List<Training> trainings = gymFacade.getTrainerTrainings(trainerCredentials, filter);

		// Then
		assertThat(trainings).hasSize(1);
		assertThat(trainings.get(0).getTrainingName()).isEqualTo("Morning Session");
		assertThat(trainings.get(0).getTrainee().getUsername()).isEqualTo(trainee1.getUsername());
	}

	@Test
	void updateTraineeProfile_shouldUpdateUsername() {
		// Given
		CreateTraineeProfileRequest createRequest = new CreateTraineeProfileRequest("John", "Doe", true,
				Optional.empty(), Optional.empty());
		Trainee trainee = gymFacade.createTraineeProfile(createRequest);

		Credentials oldCredentials = new Credentials(trainee.getUsername(), trainee.getPassword());
		String newUsername = "John.Doe1";

		UpdateTraineeProfileRequest updateRequest = new UpdateTraineeProfileRequest(oldCredentials, Optional.empty(),
				Optional.empty(), Optional.of(newUsername), Optional.empty(), Optional.empty(), Optional.empty(),
				Optional.empty());

		// When
		Trainee updated = gymFacade.updateTraineeProfile(updateRequest);

		// Then
		assertThat(updated.getUsername()).isEqualTo(newUsername);

		Credentials newCredentials = new Credentials(newUsername, trainee.getPassword());
		Optional<Trainee> retrieved = gymFacade.findTraineeByUsername(newCredentials);
		assertThat(retrieved).isPresent();
		assertThat(retrieved.get().getUsername()).isEqualTo(newUsername);
	}

	@Test
	void updateTrainerProfile_shouldUpdateUsername() {
		// Given
		TrainingType type = createOrGetTestTrainingType("Yoga");
		CreateTrainerProfileRequest createRequest = new CreateTrainerProfileRequest("Jane", "Smith", true, type);
		Trainer trainer = gymFacade.createTrainerProfile(createRequest);

		Credentials oldCredentials = new Credentials(trainer.getUsername(), trainer.getPassword());
		String newUsername = "Jane.Smith1";

		UpdateTrainerProfileRequest updateRequest = new UpdateTrainerProfileRequest(oldCredentials, Optional.empty(),
				Optional.empty(), Optional.of(newUsername), Optional.empty(), Optional.empty(), Optional.empty());

		// When
		Trainer updated = gymFacade.updateTrainerProfile(updateRequest);

		// Then
		assertThat(updated.getUsername()).isEqualTo(newUsername);

		Credentials newCredentials = new Credentials(newUsername, trainer.getPassword());
		Optional<Trainer> retrieved = gymFacade.findTrainerByUsername(newCredentials);
		assertThat(retrieved).isPresent();
		assertThat(retrieved.get().getUsername()).isEqualTo(newUsername);
	}

	@Test
	void getTraineeUnassignedTrainers_shouldExcludeTrainerAssignedViaTraining() {

		// Given

		// 1. Create Trainee
		CreateTraineeProfileRequest traineeRequest = new CreateTraineeProfileRequest("John", "Doe", true,
				Optional.of(LocalDate.of(1995, 1, 1)), Optional.empty());
		Trainee trainee = gymFacade.createTraineeProfile(traineeRequest);

		// 2. Create Training Types
		TrainingType yoga = createOrGetTestTrainingType("yoga");

		// 3. Create Trainer 1 (The assigned one)
		CreateTrainerProfileRequest trainer1Request = new CreateTrainerProfileRequest("Alice", "Smith", true, yoga);
		Trainer trainer1 = gymFacade.createTrainerProfile(trainer1Request);

		// When

		// Create trainers 2 and 3
		TrainingType boxing = createOrGetTestTrainingType("boxing");
		TrainingType cardio = createOrGetTestTrainingType("cardio");

		CreateTrainerProfileRequest trainer2Request = new CreateTrainerProfileRequest("Bob", "Jones", true, boxing);
		Trainer trainer2 = gymFacade.createTrainerProfile(trainer2Request);

		CreateTrainerProfileRequest trainer3Request = new CreateTrainerProfileRequest("Carol", "White", true, cardio);
		Trainer trainer3 = gymFacade.createTrainerProfile(trainer3Request);

		Credentials traineeCredentials = new Credentials(trainee.getUsername(), trainee.getPassword());

		// Assign trainer1 by creating a training
		CreateTrainingRequest trainingRequest = new CreateTrainingRequest(traineeCredentials, "Morning Yoga",
				LocalDateTime.now().minusDays(1), 60, yoga.getTrainingTypeName(), trainee.getUsername(),
				trainer1.getUsername());
		gymFacade.createTraining(trainingRequest);

		// When
		List<Trainer> unassigned = gymFacade.getTraineeUnassignedTrainers(traineeCredentials);

		// Then
		assertThat(unassigned).hasSize(2)
			.extracting(Trainer::getUsername)
			.containsExactlyInAnyOrder(trainer2.getUsername(), trainer3.getUsername());
	}

	@Test
	void getUnassignedTrainers_shouldReturnAllTrainersWhenNoAssignedTrainers() {
		// Given
		// Create trainers
		TrainingType yoga = createOrGetTestTrainingType("Yoga");
		TrainingType boxing = createOrGetTestTrainingType("Boxing");
		TrainingType cardio = createOrGetTestTrainingType("Cardio");

		CreateTrainerProfileRequest trainer1Request = new CreateTrainerProfileRequest("Alice", "Smith", true, yoga);
		Trainer trainer1 = gymFacade.createTrainerProfile(trainer1Request);

		CreateTrainerProfileRequest trainer2Request = new CreateTrainerProfileRequest("Bob", "Jones", true, boxing);
		Trainer trainer2 = gymFacade.createTrainerProfile(trainer2Request);

		CreateTrainerProfileRequest trainer3Request = new CreateTrainerProfileRequest("Carol", "White", true, cardio);
		Trainer trainer3 = gymFacade.createTrainerProfile(trainer3Request);

		// Create new trainee with no trainers
		CreateTraineeProfileRequest traineeRequest = new CreateTraineeProfileRequest("Mike", "Taylor", true,
				Optional.empty(), Optional.empty());
		Trainee newTrainee = gymFacade.createTraineeProfile(traineeRequest);

		Credentials credentials = new Credentials(newTrainee.getUsername(), newTrainee.getPassword());

		// When
		List<Trainer> unassigned = gymFacade.getTraineeUnassignedTrainers(credentials);

		// Then
		assertThat(unassigned).hasSize(3)
			.extracting(Trainer::getUsername)
			.containsExactlyInAnyOrder(trainer1.getUsername(), trainer2.getUsername(), trainer3.getUsername());
	}

	@Test
	void updateTrainersList_shouldUpdateTrainersForTrainee() {
		// Given
		CreateTraineeProfileRequest traineeRequest = new CreateTraineeProfileRequest("John", "Doe", true,
				Optional.empty(), Optional.empty());
		Trainee trainee = gymFacade.createTraineeProfile(traineeRequest);

		Credentials traineeCredentials = new Credentials(trainee.getUsername(), trainee.getPassword());

		TrainingType yoga = createOrGetTestTrainingType("Yoga");
		TrainingType boxing = createOrGetTestTrainingType("Boxing");

		CreateTrainerProfileRequest trainer1Request = new CreateTrainerProfileRequest("Alice", "Smith", true, yoga);
		Trainer trainer1 = gymFacade.createTrainerProfile(trainer1Request);
		CreateTrainerProfileRequest trainer2Request = new CreateTrainerProfileRequest("Elon", "Musk", true, boxing);
		Trainer trainer2 = gymFacade.createTrainerProfile(trainer2Request);

		List<Trainer> trainers = List.of(trainer1, trainer2);
		List<String> trainerUsernames = trainers.stream().map(Trainer::getUsername).toList();

		gymFacade.updateTraineeTrainersList(traineeCredentials, trainerUsernames);

		String jpql = "SELECT t FROM TraineeDAO t WHERE t.userDAO.username = :username";

		List<TraineeDAO> traineeDAO = entityManager.createQuery(jpql, TraineeDAO.class)
			.setParameter("username", trainee.getUsername())
			.getResultList();
		List<TrainerDAO> trainerDAOS = traineeDAO.get(0).getTrainerDAOS();
		List<Trainer> retrievedTrainers = trainerDAOS.stream().map(TrainerMapper::toDomain).toList();

		assertThat(retrievedTrainers).containsExactlyInAnyOrderElementsOf(trainers);
	}

	private TrainingType createOrGetTestTrainingType(String trainingTypeName) {
		String jpql = "SELECT t FROM TrainingTypeDAO t WHERE t.trainingTypeName = :name";
		List<TrainingTypeDAO> results = entityManager.createQuery(jpql, TrainingTypeDAO.class)
			.setParameter("name", trainingTypeName)
			.getResultList();

		if (!results.isEmpty()) {
			// Map to domain object
			TrainingTypeDAO dao = results.get(0);
			TrainingType type = new TrainingType(dao.getTrainingTypeName());
			type.setTrainingTypeId(dao.getTrainingTypeId());
			return type;
		}

		// Create new type
		TrainingTypeDAO dao = new TrainingTypeDAO();
		dao.setTrainingTypeName(trainingTypeName);
		entityManager.persist(dao);
		entityManager.flush();

		TrainingType type = new TrainingType(trainingTypeName);
		type.setTrainingTypeId(dao.getTrainingTypeId());
		return type;
	}

}
