package com.epam.application.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
import com.epam.domain.model.TrainingTypeEnum;
import com.epam.infrastructure.persistence.dao.TraineeDAO;
import com.epam.infrastructure.persistence.dao.TrainerDAO;
import com.epam.infrastructure.persistence.dao.TrainingTypeDAO;
import com.epam.infrastructure.persistence.mapper.TrainerMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@TestPropertySource(properties = "spring.main.banner-mode=off")
@Transactional
@ActiveProfiles("test")
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
        CreateTraineeProfileRequest request =
                new CreateTraineeProfileRequest("Michael", "Brown", true, Optional.empty(), Optional.empty());

        // When
        Trainee created = gymFacade.createTraineeProfile(request);

        // Then
        assertThat(created.getTraineeId()).isNotNull();
        assertThat(created.getUsername()).isEqualTo("Michael.Brown");
        assertThat(created.getPassword()).isNotNull().hasSize(10);

        Trainee retrieved = gymFacade.getTraineeByUsername(created.getUsername());
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getUsername()).isEqualTo("Michael.Brown");
    }

    @Test
    void updateTrainee_shouldUpdateExistingTrainee() {
        // Given
        CreateTraineeProfileRequest createRequest = new CreateTraineeProfileRequest(
                "Sarah", "Wilson", true, Optional.of(LocalDate.of(1990, 1, 1)), Optional.empty());

        Trainee trainee = gymFacade.createTraineeProfile(createRequest);

        UpdateTraineeProfileRequest updateRequest = new UpdateTraineeProfileRequest(
                trainee.getUsername(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(LocalDate.of(1991, 2, 2)),
                Optional.of("999 New Address"));

        // When
        Trainee updated = gymFacade.updateTraineeProfile(updateRequest);

        // Then
        assertThat(updated.getAddress()).isEqualTo("999 New Address");
        assertThat(updated.getDob()).isEqualTo(LocalDate.of(1991, 2, 2));

        Trainee retrieved = gymFacade.getTraineeByUsername(trainee.getUsername());
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getAddress()).isEqualTo("999 New Address");
        assertThat(retrieved.getDob()).isEqualTo(LocalDate.of(1991, 2, 2));
    }

    @Test
    void deleteTrainee_shouldRemoveTrainee() {
        // Given
        CreateTraineeProfileRequest request =
                new CreateTraineeProfileRequest("Tom", "Davis", true, Optional.empty(), Optional.empty());
        Trainee trainee = gymFacade.createTraineeProfile(request);
        TraineeDAO createdTraineeDAO = entityManager.find(TraineeDAO.class, trainee.getTraineeId());
        assertThat(createdTraineeDAO).isNotNull();

        // When
        gymFacade.deleteTraineeProfile(trainee.getUsername());

        // Then - Should not be able to find trainee anymore (authentication will fail)
        TrainerDAO deletedTraineeDAO = entityManager.find(TrainerDAO.class, trainee.getTraineeId());
        assertThat(deletedTraineeDAO).isNull();
    }

    @Test
    void createTrainee_withDuplicateName_shouldGenerateUniqueUsername() {
        // Given
        CreateTraineeProfileRequest request1 =
                new CreateTraineeProfileRequest("Duplicate", "Name", true, Optional.empty(), Optional.empty());
        CreateTraineeProfileRequest request2 =
                new CreateTraineeProfileRequest("Duplicate", "Name", true, Optional.empty(), Optional.empty());

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
        TrainingType specialization = createOrGetTestTrainingType(TrainingTypeEnum.PILATES);

        CreateTraineeProfileRequest traineeRequest =
                new CreateTraineeProfileRequest("John", "Doe", true, Optional.empty(), Optional.empty());
        Trainee trainee = gymFacade.createTraineeProfile(traineeRequest);

        CreateTrainerProfileRequest trainerRequest =
                new CreateTrainerProfileRequest("Test", "Trainer", true, specialization.getTrainingTypeName());
        Trainer trainer = gymFacade.createTrainerProfile(trainerRequest);

        CreateTrainingRequest trainingRequest1 = new CreateTrainingRequest(
                "Training 1",
                LocalDateTime.now(),
                60,
                Optional.of(specialization.getTrainingTypeName()),
                trainee.getUsername(),
                trainer.getUsername());
        gymFacade.createTraining(trainingRequest1);

        CreateTrainingRequest trainingRequest2 = new CreateTrainingRequest(
                "Training 2",
                LocalDateTime.now().plusDays(1),
                45,
                Optional.of(specialization.getTrainingTypeName()),
                trainee.getUsername(),
                trainer.getUsername());
        gymFacade.createTraining(trainingRequest2);

        // When
        List<Training> result = gymFacade.getTraineeTrainings(trainee.getUsername(), TrainingFilter.empty());

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(2);
    }

    @Test
    void createTrainer_shouldCreateTrainerWithGeneratedCredentials() {
        // Given
        createOrGetTestTrainingType(TrainingTypeEnum.CARDIO);
        CreateTrainerProfileRequest request =
                new CreateTrainerProfileRequest("Emma", "Taylor", true, TrainingTypeEnum.CARDIO);

        // When
        Trainer trainer = gymFacade.createTrainerProfile(request);

        // Then
        assertThat(trainer.getTrainerId()).isNotNull();
        assertThat(trainer.getUsername()).isEqualTo("Emma.Taylor");
        assertThat(trainer.getPassword()).isNotNull().hasSize(10);

        Trainer retrieved = gymFacade.getTrainerByUsername(trainer.getUsername());
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getUsername()).isEqualTo("Emma.Taylor");
    }

    @Test
    void updateTrainer_shouldUpdateExistingTrainer() {
        // Given
        createOrGetTestTrainingType(TrainingTypeEnum.CROSSFIT);
        CreateTrainerProfileRequest createRequest =
                new CreateTrainerProfileRequest("David", "Martinez", true, TrainingTypeEnum.CROSSFIT);
        Trainer trainer = gymFacade.createTrainerProfile(createRequest);

        TrainingType newType = createOrGetTestTrainingType(TrainingTypeEnum.PILATES);
        UpdateTrainerProfileRequest updateRequest = new UpdateTrainerProfileRequest(
                trainer.getUsername(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(false),
                Optional.of(newType.getTrainingTypeName()));

        // When
        Trainer updated = gymFacade.updateTrainerProfile(updateRequest);

        // Then
        assertThat(updated.getActive()).isFalse();
        assertThat(updated.getSpecialization().getTrainingTypeName()).isEqualTo(TrainingTypeEnum.PILATES);
    }

    @Test
    void deleteTrainer_shouldRemoveTrainer() {
        // Given
        createOrGetTestTrainingType(TrainingTypeEnum.CARDIO);
        CreateTrainerProfileRequest request =
                new CreateTrainerProfileRequest("Henry", "Cavill", true, TrainingTypeEnum.CARDIO);
        Trainer trainer = gymFacade.createTrainerProfile(request);
        TrainerDAO createdTrainer = entityManager.find(TrainerDAO.class, trainer.getTrainerId());
        assertThat(createdTrainer).isNotNull();

        // When
        gymFacade.deleteTrainerProfile(trainer.getUsername());

        // Then
        TrainerDAO deleted = entityManager.find(TrainerDAO.class, trainer.getTrainerId());
        assertThat(deleted).isNull();
    }

    @Test
    void createTrainer_withDuplicateName_shouldGenerateUniqueUsername() {
        // Given
        createOrGetTestTrainingType(TrainingTypeEnum.CROSSFIT);
        CreateTrainerProfileRequest request1 =
                new CreateTrainerProfileRequest("Same", "Person", true, TrainingTypeEnum.CROSSFIT);
        CreateTrainerProfileRequest request2 =
                new CreateTrainerProfileRequest("Same", "Person", true, TrainingTypeEnum.CROSSFIT);

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
        TrainingType type = createOrGetTestTrainingType(TrainingTypeEnum.YOGA);

        CreateTrainerProfileRequest trainerRequest =
                new CreateTrainerProfileRequest("John", "Trainer", true, TrainingTypeEnum.YOGA);
        Trainer trainer = gymFacade.createTrainerProfile(trainerRequest);

        CreateTraineeProfileRequest traineeRequest = new CreateTraineeProfileRequest(
                "Jane", "Trainee", true, Optional.of(LocalDate.of(1990, 1, 1)), Optional.empty());
        Trainee trainee = gymFacade.createTraineeProfile(traineeRequest);

        CreateTrainingRequest trainingRequest = new CreateTrainingRequest(
                "Morning Cardio",
                LocalDateTime.now(),
                60,
                Optional.of(type.getTrainingTypeName()),
                trainee.getUsername(),
                trainer.getUsername());

        // When
        gymFacade.createTraining(trainingRequest);

        // Then
        List<Training> trainings = gymFacade.getTraineeTrainings(trainee.getUsername(), TrainingFilter.empty());
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
        TrainingType type = createOrGetTestTrainingType(TrainingTypeEnum.CARDIO);

        CreateTrainerProfileRequest trainerRequest =
                new CreateTrainerProfileRequest("Shared", "Trainer", true, TrainingTypeEnum.CARDIO);
        Trainer trainer = gymFacade.createTrainerProfile(trainerRequest);

        CreateTraineeProfileRequest traineeRequest = new CreateTraineeProfileRequest(
                "Test", "Trainee", true, Optional.of(LocalDate.of(1990, 1, 1)), Optional.empty());
        Trainee trainee = gymFacade.createTraineeProfile(traineeRequest);

        CreateTrainingRequest trainingRequest1 = new CreateTrainingRequest(
                "Training 1",
                LocalDateTime.now(),
                60,
                Optional.of(type.getTrainingTypeName()),
                trainee.getUsername(),
                trainer.getUsername());
        gymFacade.createTraining(trainingRequest1);

        CreateTrainingRequest trainingRequest2 = new CreateTrainingRequest(
                "Training 2",
                LocalDateTime.now().plusDays(1),
                45,
                Optional.of(type.getTrainingTypeName()),
                trainee.getUsername(),
                trainer.getUsername());
        gymFacade.createTraining(trainingRequest2);

        // When
        gymFacade.deleteTraineeProfile(trainee.getUsername());

        // Then
        assertThat(gymFacade.getTrainerByUsername(trainer.getUsername())).isNotNull();
    }

    @Test
    void getTraineeTrainings_shouldFindTrainingsByDateRangeAndTrainerName() {
        // Given
        TrainingType specialization = createOrGetTestTrainingType(TrainingTypeEnum.YOGA);

        CreateTraineeProfileRequest traineeRequest =
                new CreateTraineeProfileRequest("Test", "Trainee", true, Optional.empty(), Optional.empty());
        Trainee trainee = gymFacade.createTraineeProfile(traineeRequest);

        CreateTrainerProfileRequest trainerRequest =
                new CreateTrainerProfileRequest("Test", "Trainer", true, TrainingTypeEnum.YOGA);
        Trainer trainer = gymFacade.createTrainerProfile(trainerRequest);

        CreateTrainingRequest t1Request = new CreateTrainingRequest(
                "Morning Run",
                LocalDateTime.now().minusDays(1),
                60,
                Optional.of(specialization.getTrainingTypeName()),
                trainee.getUsername(),
                trainer.getUsername());
        gymFacade.createTraining(t1Request);

        CreateTrainingRequest t2Request = new CreateTrainingRequest(
                "Evening Yoga",
                LocalDateTime.now().minusDays(5),
                45,
                Optional.of(specialization.getTrainingTypeName()),
                trainee.getUsername(),
                trainer.getUsername());
        gymFacade.createTraining(t2Request);

        // When
        TrainingFilter filter = TrainingFilter.forTrainee(
                Optional.of(LocalDateTime.now().minusDays(7)),
                Optional.of(LocalDateTime.now()),
                Optional.of(trainer.getUsername()),
                Optional.of(specialization.getTrainingTypeName()));

        List<Training> trainings = gymFacade.getTraineeTrainings(trainee.getUsername(), filter);

        // Then
        assertThat(trainings).hasSize(2);
        assertThat(trainings.get(0).getTrainer().getUsername()).isEqualTo("Test.Trainer");
        assertThat(trainings.get(0).getTrainingType().getTrainingTypeName())
                .isEqualTo(specialization.getTrainingTypeName());
    }

    @Test
    void getTraineeTrainings_shouldFindAllTrainingsWithinDateRange() {
        // Given
        TrainingType yoga = createOrGetTestTrainingType(TrainingTypeEnum.YOGA);
        TrainingType boxing = createOrGetTestTrainingType(TrainingTypeEnum.BOXING);

        CreateTraineeProfileRequest traineeRequest =
                new CreateTraineeProfileRequest("John", "Doe", true, Optional.empty(), Optional.empty());
        Trainee trainee = gymFacade.createTraineeProfile(traineeRequest);

        CreateTrainerProfileRequest trainerRequest =
                new CreateTrainerProfileRequest("Jane", "Smith", true, TrainingTypeEnum.YOGA);
        Trainer trainer = gymFacade.createTrainerProfile(trainerRequest);

        // Create trainings
        CreateTrainingRequest yogaRequest = new CreateTrainingRequest(
                "Morning Yoga",
                LocalDateTime.now().minusDays(1),
                60,
                Optional.of(yoga.getTrainingTypeName()),
                trainee.getUsername(),
                trainer.getUsername());
        gymFacade.createTraining(yogaRequest);

        CreateTrainingRequest boxingRequest = new CreateTrainingRequest(
                "Evening Boxing",
                LocalDateTime.now().minusDays(10),
                45,
                Optional.of(boxing.getTrainingTypeName()),
                trainee.getUsername(),
                trainer.getUsername());
        gymFacade.createTraining(boxingRequest);

        // When
        TrainingFilter filter = TrainingFilter.forTrainee(
                Optional.of(LocalDateTime.now().minusDays(7)),
                Optional.of(LocalDateTime.now()),
                Optional.empty(),
                Optional.empty());

        List<Training> trainings = gymFacade.getTraineeTrainings(trainee.getUsername(), filter);

        // Then
        assertThat(trainings).hasSize(1);
        assertThat(trainings.get(0).getTrainingName()).isEqualTo("Morning Yoga");
    }

    @Test
    void getTraineeTrainings_shouldFilterByTrainerUsername() {
        // Given
        TrainingType yoga = createOrGetTestTrainingType(TrainingTypeEnum.YOGA);

        CreateTraineeProfileRequest traineeRequest =
                new CreateTraineeProfileRequest("John", "Doe", true, Optional.empty(), Optional.empty());
        Trainee trainee = gymFacade.createTraineeProfile(traineeRequest);

        CreateTrainerProfileRequest trainerRequest =
                new CreateTrainerProfileRequest("Jane", "Smith", true, TrainingTypeEnum.YOGA);
        Trainer trainer = gymFacade.createTrainerProfile(trainerRequest);

        // Create trainings
        CreateTrainingRequest t1Request = new CreateTrainingRequest(
                "Morning Yoga",
                LocalDateTime.now().minusDays(1),
                60,
                Optional.of(yoga.getTrainingTypeName()),
                trainee.getUsername(),
                trainer.getUsername());
        gymFacade.createTraining(t1Request);

        CreateTrainingRequest t2Request = new CreateTrainingRequest(
                "Evening Yoga",
                LocalDateTime.now().minusDays(10),
                45,
                Optional.of(yoga.getTrainingTypeName()),
                trainee.getUsername(),
                trainer.getUsername());
        gymFacade.createTraining(t2Request);

        // When
        TrainingFilter filter = TrainingFilter.forTrainee(
                Optional.empty(), Optional.empty(), Optional.of(trainer.getUsername()), Optional.empty());

        List<Training> trainings = gymFacade.getTraineeTrainings(trainee.getUsername(), filter);

        // Then
        assertThat(trainings).hasSize(2);
        assertThat(trainings.get(0).getTrainer().getUsername()).contains("Jane.Smith");
    }

    @Test
    void getTraineeTrainings_shouldFilterByTrainingType() {
        // Given
        TrainingType yoga = createOrGetTestTrainingType(TrainingTypeEnum.YOGA);
        TrainingType boxing = createOrGetTestTrainingType(TrainingTypeEnum.BOXING);

        CreateTraineeProfileRequest traineeRequest =
                new CreateTraineeProfileRequest("John", "Doe", true, Optional.empty(), Optional.empty());
        Trainee trainee = gymFacade.createTraineeProfile(traineeRequest);

        CreateTrainerProfileRequest trainerRequest =
                new CreateTrainerProfileRequest("Jane", "Smith", true, TrainingTypeEnum.YOGA);
        Trainer trainer = gymFacade.createTrainerProfile(trainerRequest);

        // Create trainings
        CreateTrainingRequest yogaRequest = new CreateTrainingRequest(
                "Morning Yoga",
                LocalDateTime.now().minusDays(1),
                60,
                Optional.of(yoga.getTrainingTypeName()),
                trainee.getUsername(),
                trainer.getUsername());
        gymFacade.createTraining(yogaRequest);

        CreateTrainingRequest boxingRequest = new CreateTrainingRequest(
                "Evening Boxing",
                LocalDateTime.now().minusDays(10),
                45,
                Optional.of(boxing.getTrainingTypeName()),
                trainee.getUsername(),
                trainer.getUsername());
        gymFacade.createTraining(boxingRequest);

        // When
        TrainingFilter filter = TrainingFilter.forTrainee(
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(yoga.getTrainingTypeName()));

        List<Training> trainings = gymFacade.getTraineeTrainings(trainee.getUsername(), filter);

        // Then
        assertThat(trainings).hasSize(1);
        assertThat(trainings.get(0).getTrainingType().getTrainingTypeName()).isEqualTo(TrainingTypeEnum.YOGA);
    }

    @Test
    void getTraineeTrainings_shouldReturnEmptyListWhenNoMatch() {
        // Given
        TrainingType yoga = createOrGetTestTrainingType(TrainingTypeEnum.YOGA);
        TrainingType boxing = createOrGetTestTrainingType(TrainingTypeEnum.BOXING);

        CreateTraineeProfileRequest traineeRequest =
                new CreateTraineeProfileRequest("John", "Doe", true, Optional.empty(), Optional.empty());
        Trainee trainee = gymFacade.createTraineeProfile(traineeRequest);

        CreateTrainerProfileRequest trainerRequest =
                new CreateTrainerProfileRequest("Jane", "Smith", true, TrainingTypeEnum.YOGA);
        Trainer trainer = gymFacade.createTrainerProfile(trainerRequest);

        // Create trainings
        CreateTrainingRequest yogaRequest = new CreateTrainingRequest(
                "Morning Yoga",
                LocalDateTime.now().minusDays(1),
                60,
                Optional.of(yoga.getTrainingTypeName()),
                trainee.getUsername(),
                trainer.getUsername());
        gymFacade.createTraining(yogaRequest);

        CreateTrainingRequest boxingRequest = new CreateTrainingRequest(
                "Evening Boxing",
                LocalDateTime.now().minusDays(10),
                45,
                Optional.of(boxing.getTrainingTypeName()),
                trainee.getUsername(),
                trainer.getUsername());
        gymFacade.createTraining(boxingRequest);

        // When
        TrainingFilter filter = TrainingFilter.forTrainee(
                Optional.of(LocalDateTime.now().minusDays(100)),
                Optional.of(LocalDateTime.now().minusDays(90)),
                Optional.empty(),
                Optional.of(yoga.getTrainingTypeName()));

        List<Training> trainings = gymFacade.getTraineeTrainings(trainee.getUsername(), filter);

        // Then
        assertThat(trainings).isEmpty();
    }

    @Test
    void getTraineeTrainings_shouldFilterByAllCriteriaTogether() {
        // Given
        TrainingType yoga = createOrGetTestTrainingType(TrainingTypeEnum.YOGA);
        TrainingType boxing = createOrGetTestTrainingType(TrainingTypeEnum.BOXING);

        CreateTraineeProfileRequest traineeRequest =
                new CreateTraineeProfileRequest("John", "Doe", true, Optional.empty(), Optional.empty());
        Trainee trainee = gymFacade.createTraineeProfile(traineeRequest);

        CreateTrainerProfileRequest trainerRequest =
                new CreateTrainerProfileRequest("Jane", "Smith", true, TrainingTypeEnum.YOGA);
        Trainer trainer = gymFacade.createTrainerProfile(trainerRequest);

        // Create trainings
        CreateTrainingRequest yogaRequest = new CreateTrainingRequest(
                "Morning Yoga",
                LocalDateTime.now().minusDays(1),
                60,
                Optional.of(yoga.getTrainingTypeName()),
                trainee.getUsername(),
                trainer.getUsername());
        gymFacade.createTraining(yogaRequest);

        CreateTrainingRequest boxingRequest = new CreateTrainingRequest(
                "Evening Boxing",
                LocalDateTime.now().minusDays(10),
                45,
                Optional.of(boxing.getTrainingTypeName()),
                trainee.getUsername(),
                trainer.getUsername());
        gymFacade.createTraining(boxingRequest);

        // When
        TrainingFilter filter = TrainingFilter.forTrainee(
                Optional.of(LocalDateTime.now().minusDays(2)),
                Optional.of(LocalDateTime.now()),
                Optional.of(trainer.getUsername()),
                Optional.of(yoga.getTrainingTypeName()));

        List<Training> trainings = gymFacade.getTraineeTrainings(trainee.getUsername(), filter);

        // Then
        assertThat(trainings).hasSize(1);
        assertThat(trainings.get(0).getTrainingName()).isEqualTo("Morning Yoga");
    }

    @Test
    void getUnassignedTrainers_shouldReturnOnlyUnassignedTrainers() {
        // Given
        CreateTraineeProfileRequest traineeRequest =
                new CreateTraineeProfileRequest("John", "Doe", true, Optional.empty(), Optional.empty());
        Trainee trainee = gymFacade.createTraineeProfile(traineeRequest);

        // Create trainers
        createOrGetTestTrainingType(TrainingTypeEnum.YOGA);
        createOrGetTestTrainingType(TrainingTypeEnum.BOXING);

        CreateTrainerProfileRequest trainer1Request =
                new CreateTrainerProfileRequest("Alice", "Smith", true, TrainingTypeEnum.BOXING);
        Trainer trainer1 = gymFacade.createTrainerProfile(trainer1Request);

        CreateTrainerProfileRequest trainer2Request =
                new CreateTrainerProfileRequest("Bob", "Jones", true, TrainingTypeEnum.BOXING);
        Trainer trainer2 = gymFacade.createTrainerProfile(trainer2Request);

        List<String> trainerUsernames = List.of(trainer1.getUsername(), trainer2.getUsername());

        // When
        gymFacade.updateTraineeTrainersList(trainee.getUsername(), trainerUsernames);

        // Then
        List<Trainer> unassigned = gymFacade.getTraineeUnassignedTrainers(trainee.getUsername());
        assertThat(unassigned).isEmpty();
    }

    @Test
    void toggleTraineeActiveStatus_shouldChangeActiveStatus() {
        // Given
        CreateTraineeProfileRequest request =
                new CreateTraineeProfileRequest("Test", "User", true, Optional.empty(), Optional.empty());
        Trainee trainee = gymFacade.createTraineeProfile(request);
        assertThat(trainee.getActive()).isTrue();

        // When
        gymFacade.toggleTraineeActiveStatus(trainee.getUsername());

        // Then
        Trainee updated = gymFacade.getTraineeByUsername(trainee.getUsername());
        assertThat(updated).isNotNull();
        assertThat(updated.getActive()).isFalse();
    }

    @Test
    void toggleTrainerActiveStatus_shouldChangeActiveStatus() {
        // Given
        createOrGetTestTrainingType(TrainingTypeEnum.YOGA);
        CreateTrainerProfileRequest request =
                new CreateTrainerProfileRequest("Test", "Trainer", true, TrainingTypeEnum.YOGA);
        Trainer trainer = gymFacade.createTrainerProfile(request);
        assertThat(trainer.getActive()).isTrue();

        // When
        gymFacade.toggleTrainerActiveStatus(trainer.getUsername());

        // Then
        Trainer updated = gymFacade.getTrainerByUsername(trainer.getUsername());
        assertThat(updated).isNotNull();
        assertThat(updated.getActive()).isFalse();
    }

    @Test
    void getTrainerTrainings_shouldReturnTrainerSpecificTrainings() {
        // Given
        TrainingType yoga = createOrGetTestTrainingType(TrainingTypeEnum.YOGA);

        CreateTrainerProfileRequest trainerRequest =
                new CreateTrainerProfileRequest("Jane", "Trainer", true, TrainingTypeEnum.YOGA);
        Trainer trainer = gymFacade.createTrainerProfile(trainerRequest);

        CreateTraineeProfileRequest trainee1Request =
                new CreateTraineeProfileRequest("John", "Doe", true, Optional.empty(), Optional.empty());
        Trainee trainee1 = gymFacade.createTraineeProfile(trainee1Request);

        CreateTraineeProfileRequest trainee2Request =
                new CreateTraineeProfileRequest("Jane", "Smith", true, Optional.empty(), Optional.empty());
        Trainee trainee2 = gymFacade.createTraineeProfile(trainee2Request);

        // Create trainings
        CreateTrainingRequest training1Request = new CreateTrainingRequest(
                "Morning Session",
                LocalDateTime.now().minusDays(1),
                60,
                Optional.of(yoga.getTrainingTypeName()),
                trainee1.getUsername(),
                trainer.getUsername());
        gymFacade.createTraining(training1Request);

        CreateTrainingRequest training2Request = new CreateTrainingRequest(
                "Evening Session",
                LocalDateTime.now().minusDays(2),
                45,
                Optional.of(yoga.getTrainingTypeName()),
                trainee2.getUsername(),
                trainer.getUsername());
        gymFacade.createTraining(training2Request);

        // When
        TrainingFilter filter = TrainingFilter.forTrainer(Optional.empty(), Optional.empty(), Optional.empty());

        List<Training> trainings = gymFacade.getTrainerTrainings(trainer.getUsername(), filter);

        // Then
        assertThat(trainings).hasSize(2);
        assertThat(trainings)
                .extracting(Training::getTrainingName)
                .containsExactlyInAnyOrder("Morning Session", "Evening Session");
    }

    @Test
    void getTrainerTrainings_shouldFilterByTraineeName() {
        // Given
        TrainingType yoga = createOrGetTestTrainingType(TrainingTypeEnum.YOGA);

        CreateTrainerProfileRequest trainerRequest =
                new CreateTrainerProfileRequest("Jane", "Trainer", true, TrainingTypeEnum.YOGA);
        Trainer trainer = gymFacade.createTrainerProfile(trainerRequest);

        CreateTraineeProfileRequest trainee1Request =
                new CreateTraineeProfileRequest("John", "Doe", true, Optional.empty(), Optional.empty());
        Trainee trainee1 = gymFacade.createTraineeProfile(trainee1Request);

        CreateTraineeProfileRequest trainee2Request =
                new CreateTraineeProfileRequest("Jane", "Smith", true, Optional.empty(), Optional.empty());
        Trainee trainee2 = gymFacade.createTraineeProfile(trainee2Request);

        // Create trainings
        CreateTrainingRequest training1Request = new CreateTrainingRequest(
                "Morning Session",
                LocalDateTime.now().minusDays(1),
                60,
                Optional.of(yoga.getTrainingTypeName()),
                trainee1.getUsername(),
                trainer.getUsername());
        gymFacade.createTraining(training1Request);

        CreateTrainingRequest training2Request = new CreateTrainingRequest(
                "Evening Session",
                LocalDateTime.now().minusDays(2),
                45,
                Optional.of(yoga.getTrainingTypeName()),
                trainee2.getUsername(),
                trainer.getUsername());
        gymFacade.createTraining(training2Request);

        // When
        TrainingFilter filter =
                TrainingFilter.forTrainer(Optional.empty(), Optional.empty(), Optional.of(trainee1.getUsername()));

        List<Training> trainings = gymFacade.getTrainerTrainings(trainer.getUsername(), filter);

        // Then
        assertThat(trainings).hasSize(1);
        assertThat(trainings.get(0).getTrainingName()).isEqualTo("Morning Session");
        assertThat(trainings.get(0).getTrainee().getUsername()).isEqualTo(trainee1.getUsername());
    }

    @Test
    void getTraineeUnassignedTrainers_shouldExcludeTrainerAssignedViaTraining() {

        // Given

        // 1. Create Trainee
        CreateTraineeProfileRequest traineeRequest = new CreateTraineeProfileRequest(
                "John", "Doe", true, Optional.of(LocalDate.of(1995, 1, 1)), Optional.empty());
        Trainee trainee = gymFacade.createTraineeProfile(traineeRequest);

        // 2. Create Training Types
        TrainingType yoga = createOrGetTestTrainingType(TrainingTypeEnum.YOGA);

        // 3. Create Trainer 1 (The assigned one)
        CreateTrainerProfileRequest trainer1Request =
                new CreateTrainerProfileRequest("Alice", "Smith", true, TrainingTypeEnum.YOGA);
        Trainer trainer1 = gymFacade.createTrainerProfile(trainer1Request);

        // When

        // Create trainers 2 and 3
        createOrGetTestTrainingType(TrainingTypeEnum.BOXING);
        createOrGetTestTrainingType(TrainingTypeEnum.CARDIO);

        CreateTrainerProfileRequest trainer2Request =
                new CreateTrainerProfileRequest("Bob", "Jones", true, TrainingTypeEnum.BOXING);
        Trainer trainer2 = gymFacade.createTrainerProfile(trainer2Request);

        CreateTrainerProfileRequest trainer3Request =
                new CreateTrainerProfileRequest("Carol", "White", true, TrainingTypeEnum.CARDIO);
        Trainer trainer3 = gymFacade.createTrainerProfile(trainer3Request);

        // Assign trainer1 by creating a training
        CreateTrainingRequest trainingRequest = new CreateTrainingRequest(
                "Morning Yoga",
                LocalDateTime.now().minusDays(1),
                60,
                Optional.of(yoga.getTrainingTypeName()),
                trainee.getUsername(),
                trainer1.getUsername());
        gymFacade.createTraining(trainingRequest);

        // When
        List<Trainer> unassigned = gymFacade.getTraineeUnassignedTrainers(trainee.getUsername());

        // Then
        assertThat(unassigned)
                .hasSize(2)
                .extracting(Trainer::getUsername)
                .containsExactlyInAnyOrder(trainer2.getUsername(), trainer3.getUsername());
    }

    @Test
    void getUnassignedTrainers_shouldReturnAllTrainersWhenNoAssignedTrainers() {
        // Given
        // Create trainers
        createOrGetTestTrainingType(TrainingTypeEnum.YOGA);
        createOrGetTestTrainingType(TrainingTypeEnum.BOXING);
        createOrGetTestTrainingType(TrainingTypeEnum.CARDIO);

        CreateTrainerProfileRequest trainer1Request =
                new CreateTrainerProfileRequest("Alice", "Smith", true, TrainingTypeEnum.YOGA);
        Trainer trainer1 = gymFacade.createTrainerProfile(trainer1Request);

        CreateTrainerProfileRequest trainer2Request =
                new CreateTrainerProfileRequest("Bob", "Jones", true, TrainingTypeEnum.BOXING);
        Trainer trainer2 = gymFacade.createTrainerProfile(trainer2Request);

        CreateTrainerProfileRequest trainer3Request =
                new CreateTrainerProfileRequest("Carol", "White", true, TrainingTypeEnum.CARDIO);
        Trainer trainer3 = gymFacade.createTrainerProfile(trainer3Request);

        // Create new trainee with no trainers
        CreateTraineeProfileRequest traineeRequest =
                new CreateTraineeProfileRequest("Mike", "Taylor", true, Optional.empty(), Optional.empty());
        Trainee newTrainee = gymFacade.createTraineeProfile(traineeRequest);

        // When
        List<Trainer> unassigned = gymFacade.getTraineeUnassignedTrainers(newTrainee.getUsername());

        // Then
        assertThat(unassigned)
                .hasSize(3)
                .extracting(Trainer::getUsername)
                .containsExactlyInAnyOrder(trainer1.getUsername(), trainer2.getUsername(), trainer3.getUsername());
    }

    @Test
    void updateTrainersList_shouldUpdateTrainersForTrainee() {
        // Given
        CreateTraineeProfileRequest traineeRequest =
                new CreateTraineeProfileRequest("John", "Doe", true, Optional.empty(), Optional.empty());
        Trainee trainee = gymFacade.createTraineeProfile(traineeRequest);

        createOrGetTestTrainingType(TrainingTypeEnum.YOGA);
        createOrGetTestTrainingType(TrainingTypeEnum.BOXING);

        CreateTrainerProfileRequest trainer1Request =
                new CreateTrainerProfileRequest("Alice", "Smith", true, TrainingTypeEnum.YOGA);
        Trainer trainer1 = gymFacade.createTrainerProfile(trainer1Request);
        CreateTrainerProfileRequest trainer2Request =
                new CreateTrainerProfileRequest("Elon", "Musk", true, TrainingTypeEnum.BOXING);
        Trainer trainer2 = gymFacade.createTrainerProfile(trainer2Request);

        List<Trainer> trainers = List.of(trainer1, trainer2);
        List<String> trainerUsernames =
                trainers.stream().map(Trainer::getUsername).toList();

        gymFacade.updateTraineeTrainersList(trainee.getUsername(), trainerUsernames);

        String jpql = "SELECT t FROM TraineeDAO t WHERE t.userDAO.username = :username";

        List<TraineeDAO> traineeDAO = entityManager
                .createQuery(jpql, TraineeDAO.class)
                .setParameter("username", trainee.getUsername())
                .getResultList();
        List<TrainerDAO> trainerDAOS = traineeDAO.get(0).getTrainerDAOS();
        List<Trainer> retrievedTrainers =
                trainerDAOS.stream().map(TrainerMapper::toDomain).toList();

        assertThat(retrievedTrainers).containsExactlyInAnyOrderElementsOf(trainers);
    }

    private TrainingType createOrGetTestTrainingType(TrainingTypeEnum trainingTypeName) {
        String jpql = "SELECT t FROM TrainingTypeDAO t WHERE t.trainingTypeName = :name";
        List<TrainingTypeDAO> results = entityManager
                .createQuery(jpql, TrainingTypeDAO.class)
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

    // Add these test methods to your GymFacadeImplIntegrationTest class

    @Test
    void getTraineeTrainers_shouldReturnAllAssignedTrainers() {
        // Given
        CreateTraineeProfileRequest traineeRequest =
                new CreateTraineeProfileRequest("John", "Doe", true, Optional.empty(), Optional.empty());
        Trainee trainee = gymFacade.createTraineeProfile(traineeRequest);

        createOrGetTestTrainingType(TrainingTypeEnum.YOGA);
        createOrGetTestTrainingType(TrainingTypeEnum.BOXING);

        CreateTrainerProfileRequest trainer1Request =
                new CreateTrainerProfileRequest("Alice", "Smith", true, TrainingTypeEnum.YOGA);
        Trainer trainer1 = gymFacade.createTrainerProfile(trainer1Request);

        CreateTrainerProfileRequest trainer2Request =
                new CreateTrainerProfileRequest("Bob", "Jones", true, TrainingTypeEnum.BOXING);
        Trainer trainer2 = gymFacade.createTrainerProfile(trainer2Request);

        List<String> trainerUsernames = List.of(trainer1.getUsername(), trainer2.getUsername());

        gymFacade.updateTraineeTrainersList(trainee.getUsername(), trainerUsernames);

        // When
        List<Trainer> trainers = gymFacade.getTraineeTrainers(trainee.getUsername());

        // Then
        assertThat(trainers).hasSize(2);
        assertThat(trainers)
                .extracting(Trainer::getUsername)
                .containsExactlyInAnyOrder(trainer1.getUsername(), trainer2.getUsername());
    }

    @Test
    void getTraineeTrainers_shouldReturnEmptyListWhenNoTrainersAssigned() {
        // Given
        CreateTraineeProfileRequest traineeRequest =
                new CreateTraineeProfileRequest("John", "Doe", true, Optional.empty(), Optional.empty());
        Trainee trainee = gymFacade.createTraineeProfile(traineeRequest);

        // When
        List<Trainer> trainers = gymFacade.getTraineeTrainers(trainee.getUsername());

        // Then
        assertThat(trainers).isEmpty();
    }

    @Test
    void getTraineeTrainers_shouldIncludeTrainerAssignedViaTraining() {
        // Given
        TrainingType yoga = createOrGetTestTrainingType(TrainingTypeEnum.YOGA);

        CreateTraineeProfileRequest traineeRequest =
                new CreateTraineeProfileRequest("John", "Doe", true, Optional.empty(), Optional.empty());
        Trainee trainee = gymFacade.createTraineeProfile(traineeRequest);

        CreateTrainerProfileRequest trainerRequest =
                new CreateTrainerProfileRequest("Alice", "Smith", true, TrainingTypeEnum.YOGA);
        Trainer trainer = gymFacade.createTrainerProfile(trainerRequest);

        // Assign trainer by creating a training
        CreateTrainingRequest trainingRequest = new CreateTrainingRequest(
                "Morning Yoga",
                LocalDateTime.now(),
                60,
                Optional.of(yoga.getTrainingTypeName()),
                trainee.getUsername(),
                trainer.getUsername());
        gymFacade.createTraining(trainingRequest);

        // When
        List<Trainer> trainers = gymFacade.getTraineeTrainers(trainee.getUsername());

        // Then
        assertThat(trainers).hasSize(1);
        assertThat(trainers.get(0).getUsername()).isEqualTo(trainer.getUsername());
    }

    @Test
    void getTraineeTrainers_shouldThrowExceptionForInvalidCredentials() {
        // Given
        String invalidUsername = "nonexistent.user";

        // When & Then
        assertThatThrownBy(() -> gymFacade.getTraineeTrainers(invalidUsername))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getTrainerTrainees_shouldReturnAllAssignedTrainees() {
        // Given
        TrainingType yoga = createOrGetTestTrainingType(TrainingTypeEnum.YOGA);

        CreateTrainerProfileRequest trainerRequest =
                new CreateTrainerProfileRequest("Jane", "Trainer", true, TrainingTypeEnum.YOGA);
        Trainer trainer = gymFacade.createTrainerProfile(trainerRequest);

        CreateTraineeProfileRequest trainee1Request =
                new CreateTraineeProfileRequest("John", "Doe", true, Optional.empty(), Optional.empty());
        Trainee trainee1 = gymFacade.createTraineeProfile(trainee1Request);

        CreateTraineeProfileRequest trainee2Request =
                new CreateTraineeProfileRequest("Jane", "Smith", true, Optional.empty(), Optional.empty());
        Trainee trainee2 = gymFacade.createTraineeProfile(trainee2Request);

        // Assign trainees by creating trainings
        CreateTrainingRequest training1Request = new CreateTrainingRequest(
                "Morning Session",
                LocalDateTime.now(),
                60,
                Optional.of(yoga.getTrainingTypeName()),
                trainee1.getUsername(),
                trainer.getUsername());
        gymFacade.createTraining(training1Request);

        CreateTrainingRequest training2Request = new CreateTrainingRequest(
                "Evening Session",
                LocalDateTime.now().plusDays(1),
                45,
                Optional.of(yoga.getTrainingTypeName()),
                trainee2.getUsername(),
                trainer.getUsername());
        gymFacade.createTraining(training2Request);

        // When
        List<Trainee> trainees = gymFacade.getTrainerTrainees(trainer.getUsername());

        // Then
        assertThat(trainees).hasSize(2);
        assertThat(trainees)
                .extracting(Trainee::getUsername)
                .containsExactlyInAnyOrder(trainee1.getUsername(), trainee2.getUsername());
    }

    @Test
    void getTrainerTrainees_shouldReturnEmptyListWhenNoTraineesAssigned() {
        // Given
        createOrGetTestTrainingType(TrainingTypeEnum.YOGA);

        CreateTrainerProfileRequest trainerRequest =
                new CreateTrainerProfileRequest("Jane", "Trainer", true, TrainingTypeEnum.YOGA);
        Trainer trainer = gymFacade.createTrainerProfile(trainerRequest);

        // When
        List<Trainee> trainees = gymFacade.getTrainerTrainees(trainer.getUsername());

        // Then
        assertThat(trainees).isEmpty();
    }

    @Test
    void getTrainerTrainees_shouldNotReturnDuplicateTrainees() {
        // Given
        TrainingType yoga = createOrGetTestTrainingType(TrainingTypeEnum.YOGA);

        CreateTrainerProfileRequest trainerRequest =
                new CreateTrainerProfileRequest("Jane", "Trainer", true, TrainingTypeEnum.YOGA);
        Trainer trainer = gymFacade.createTrainerProfile(trainerRequest);

        CreateTraineeProfileRequest traineeRequest =
                new CreateTraineeProfileRequest("John", "Doe", true, Optional.empty(), Optional.empty());
        Trainee trainee = gymFacade.createTraineeProfile(traineeRequest);

        // Create multiple trainings with the same trainee
        CreateTrainingRequest training1Request = new CreateTrainingRequest(
                "Morning Session",
                LocalDateTime.now(),
                60,
                Optional.of(yoga.getTrainingTypeName()),
                trainee.getUsername(),
                trainer.getUsername());
        gymFacade.createTraining(training1Request);

        CreateTrainingRequest training2Request = new CreateTrainingRequest(
                "Evening Session",
                LocalDateTime.now().plusDays(1),
                45,
                Optional.of(yoga.getTrainingTypeName()),
                trainee.getUsername(),
                trainer.getUsername());
        gymFacade.createTraining(training2Request);

        // When
        List<Trainee> trainees = gymFacade.getTrainerTrainees(trainer.getUsername());

        // Then
        assertThat(trainees).hasSize(1);
        assertThat(trainees.get(0).getUsername()).isEqualTo(trainee.getUsername());
    }

    @Test
    void getTrainerTrainees_shouldThrowExceptionForInvalidCredentials() {
        // Given
        String invalidUsername = "nonexistent.trainer";
        // When & Then
        assertThatThrownBy(() -> gymFacade.getTrainerTrainees(invalidUsername))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getTrainingTypes_shouldReturnAllAvailableTrainingTypes() {
        // Given
        createOrGetTestTrainingType(TrainingTypeEnum.YOGA);
        createOrGetTestTrainingType(TrainingTypeEnum.BOXING);
        createOrGetTestTrainingType(TrainingTypeEnum.CARDIO);

        // When
        List<TrainingType> trainingTypes = gymFacade.getTrainingTypes();

        // Then
        assertThat(trainingTypes).hasSize(3);
        assertThat(trainingTypes)
                .extracting(TrainingType::getTrainingTypeName)
                .containsExactlyInAnyOrder(TrainingTypeEnum.YOGA, TrainingTypeEnum.BOXING, TrainingTypeEnum.CARDIO);
    }

    @Test
    void getTrainingTypes_shouldReturnEmptyListWhenNoTypesExist() {
        // Given - database is cleaned in @BeforeEach

        // When
        List<TrainingType> trainingTypes = gymFacade.getTrainingTypes();

        // Then
        assertThat(trainingTypes).isEmpty();
    }

    @Test
    void getTrainingTypes_shouldReturnTypesWithValidIds() {
        // Given
        createOrGetTestTrainingType(TrainingTypeEnum.YOGA);
        createOrGetTestTrainingType(TrainingTypeEnum.BOXING);

        // When
        List<TrainingType> trainingTypes = gymFacade.getTrainingTypes();

        // Then
        assertThat(trainingTypes).allMatch(type -> type.getTrainingTypeId() != null);
        assertThat(trainingTypes).allMatch(type -> type.getTrainingTypeId() > 0);
    }

    @Test
    void getTrainingTypes_shouldBeReadOnly() {
        // Given
        createOrGetTestTrainingType(TrainingTypeEnum.YOGA);

        // When
        List<TrainingType> trainingTypes1 = gymFacade.getTrainingTypes();
        List<TrainingType> trainingTypes2 = gymFacade.getTrainingTypes();

        // Then - should return consistent results
        assertThat(trainingTypes1).hasSize(trainingTypes2.size());
        assertThat(trainingTypes1)
                .extracting(TrainingType::getTrainingTypeName)
                .containsExactlyInAnyOrderElementsOf(trainingTypes2.stream()
                        .map(TrainingType::getTrainingTypeName)
                        .toList());
    }
}
