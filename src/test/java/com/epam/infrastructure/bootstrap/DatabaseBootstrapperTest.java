package com.epam.infrastructure.bootstrap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.epam.IntegrationTestBase;
import com.epam.application.messaging.publisher.TrainingEventPublisher;
import com.epam.domain.model.TrainingTypeEnum;
import com.epam.domain.model.UserRole;
import com.epam.infrastructure.bootstrap.dto.InitialBootstrapData;
import com.epam.infrastructure.bootstrap.dto.TraineeDTO;
import com.epam.infrastructure.bootstrap.dto.TrainerDTO;
import com.epam.infrastructure.bootstrap.dto.TrainingDTO;
import com.epam.infrastructure.bootstrap.dto.TrainingTypeDTO;
import com.epam.infrastructure.bootstrap.dto.UserDTO;
import com.epam.infrastructure.persistence.dao.TraineeDAO;
import com.epam.infrastructure.persistence.dao.TrainerDAO;
import com.epam.infrastructure.persistence.dao.TrainingDAO;
import com.epam.infrastructure.persistence.dao.TrainingTypeDAO;
import com.epam.infrastructure.persistence.dao.UserDAO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class DatabaseBootstrapperTest extends IntegrationTestBase {

    @PersistenceContext
    private EntityManager entityManager;

    @MockitoBean
    private TrainingEventPublisher trainingEventPublisher;

    private JsonDataLoader jsonDataLoader;

    private DatabaseBootstrapper bootstrapper;

    private static final DefaultApplicationArguments ARGS = new DefaultApplicationArguments();

    @BeforeEach
    void setUp() {
        entityManager.createQuery("DELETE FROM TrainingDAO").executeUpdate();
        entityManager.createQuery("DELETE FROM TraineeDAO").executeUpdate();
        entityManager.createQuery("DELETE FROM TrainerDAO").executeUpdate();
        entityManager.createQuery("DELETE FROM UserDAO").executeUpdate();
        entityManager.createQuery("DELETE FROM TrainingTypeDAO").executeUpdate();
        entityManager.flush();

        jsonDataLoader = mock(JsonDataLoader.class);

        bootstrapper = new DatabaseBootstrapper(jsonDataLoader, entityManager);
    }

    @Test
    void shouldSuccessfullyBootstrapWithValidData() throws Exception {
        // Given
        InitialBootstrapData data = createValidBootstrapData();
        when(jsonDataLoader.loadBootstrapData()).thenReturn(data);

        // When
        bootstrapper.run(ARGS);

        // Then
        assertThat(bootstrapper.isInitialized()).isTrue();
        assertThat(bootstrapper.isSkipped()).isFalse();
        assertThat(bootstrapper.getLastError()).isNull();

        assertThat(bootstrapper.getTrainingTypeCount()).isEqualTo(2);
        assertThat(bootstrapper.getUserCount()).isEqualTo(3);
        assertThat(bootstrapper.getTrainerCount()).isEqualTo(1);
        assertThat(bootstrapper.getTraineeCount()).isEqualTo(2);

        Long userCount = entityManager
                .createQuery("SELECT COUNT(u) FROM UserDAO u", Long.class)
                .getSingleResult();
        assertThat(userCount).isEqualTo(3);

        Long trainerCount = entityManager
                .createQuery("SELECT COUNT(t) FROM TrainerDAO t", Long.class)
                .getSingleResult();
        assertThat(trainerCount).isEqualTo(1);

        Long traineeCount = entityManager
                .createQuery("SELECT COUNT(t) FROM TraineeDAO t", Long.class)
                .getSingleResult();
        assertThat(traineeCount).isEqualTo(2);
    }

    @Test
    void shouldSkipBootstrapWhenDataAlreadyExists() throws Exception {
        // Given
        UserDAO existingUser = new UserDAO();
        existingUser.setFirstName("Existing");
        existingUser.setLastName("User");
        existingUser.setUsername("Existing.User");
        existingUser.setPassword("password123");
        existingUser.setActive(true);
        existingUser.setUserRole(UserRole.TRAINEE);
        entityManager.persist(existingUser);
        entityManager.flush();

        when(jsonDataLoader.loadBootstrapData()).thenReturn(createValidBootstrapData());

        // When
        bootstrapper.run(ARGS);

        // Then
        assertThat(bootstrapper.isInitialized()).isTrue();
        assertThat(bootstrapper.isSkipped()).isTrue();
        assertThat(bootstrapper.getUserCount()).isEqualTo(1);

        Long userCount = entityManager
                .createQuery("SELECT COUNT(u) FROM UserDAO u", Long.class)
                .getSingleResult();
        assertThat(userCount).isEqualTo(1);
    }

    @Test
    void shouldCreateTraineeTrainerRelationshipsCorrectly() throws Exception {
        // Given
        when(jsonDataLoader.loadBootstrapData()).thenReturn(createValidBootstrapData());

        // When
        bootstrapper.run(ARGS);

        // Then
        TraineeDAO trainee = entityManager
                .createQuery(
                        "SELECT t FROM TraineeDAO t JOIN FETCH t.trainerDAOS WHERE t.userDAO.username = :username",
                        TraineeDAO.class)
                .setParameter("username", "John.Doe")
                .getSingleResult();

        assertThat(trainee.getTrainerDAOS()).hasSize(1);
        assertThat(trainee.getTrainerDAOS().get(0).getUserDAO().getUsername()).isEqualTo("Jane.Smith");
    }

    @Test
    void shouldHandleMissingTrainerReference() throws Exception {
        // Given
        InitialBootstrapData data = new InitialBootstrapData();
        data.setTrainingTypes(List.of(new TrainingTypeDTO("YOGA")));
        data.setUsers(List.of(new UserDTO("John", "Doe", "John.Doe", "password123", true, "TRAINEE")));

        TraineeDTO trainee = new TraineeDTO();
        trainee.setUsername("John.Doe");
        trainee.setDateOfBirth(LocalDate.of(1990, 1, 1));
        trainee.setAddress("123 Main St");
        trainee.setTrainerUsernames(List.of("NonExistent.Trainer"));
        data.setTrainees(List.of(trainee));
        data.setTrainers(List.of());
        data.setTrainings(List.of());

        when(jsonDataLoader.loadBootstrapData()).thenReturn(data);

        // When
        bootstrapper.run(ARGS);

        // Then
        assertThat(bootstrapper.isInitialized()).isTrue();
        assertThat(bootstrapper.getTraineeCount()).isEqualTo(1);

        TraineeDAO persistedTrainee = entityManager
                .createQuery("SELECT t FROM TraineeDAO t WHERE t.userDAO.username = :username", TraineeDAO.class)
                .setParameter("username", "John.Doe")
                .getSingleResult();

        assertThat(persistedTrainee.getTrainerDAOS()).isEmpty();
    }

    @Test
    void shouldHandleMissingTraineeInTraining() throws Exception {
        // Given
        InitialBootstrapData data = new InitialBootstrapData();
        data.setTrainingTypes(List.of(new TrainingTypeDTO("YOGA")));
        data.setUsers(List.of(new UserDTO("Jane", "Smith", "Jane.Smith", "password123", true, "TRAINER")));

        TrainerDTO trainer = new TrainerDTO();
        trainer.setUsername("Jane.Smith");
        trainer.setSpecialization("YOGA");
        data.setTrainers(List.of(trainer));
        data.setTrainees(List.of());

        TrainingDTO training = new TrainingDTO();
        training.setName("Morning Yoga");
        training.setDate(LocalDateTime.now());
        training.setDurationMinutes(60);
        training.setTraineeUsername("NonExistent.Trainee");
        training.setTrainerUsername("Jane.Smith");
        training.setTrainingType("YOGA");
        data.setTrainings(List.of(training));

        when(jsonDataLoader.loadBootstrapData()).thenReturn(data);

        // When
        bootstrapper.run(ARGS);

        // Then
        assertThat(bootstrapper.isInitialized()).isTrue();

        Long trainingCount = entityManager
                .createQuery("SELECT COUNT(t) FROM TrainingDAO t", Long.class)
                .getSingleResult();
        assertThat(trainingCount).isEqualTo(0);
    }

    @Test
    void shouldPersistTrainingsWithAllValidReferences() throws Exception {
        // Given
        InitialBootstrapData data = createValidBootstrapData();

        TrainingDTO training = new TrainingDTO();
        training.setName("Morning Yoga");
        training.setDate(LocalDateTime.now());
        training.setDurationMinutes(60);
        training.setTraineeUsername("John.Doe");
        training.setTrainerUsername("Jane.Smith");
        training.setTrainingType("YOGA");
        data.setTrainings(List.of(training));

        when(jsonDataLoader.loadBootstrapData()).thenReturn(data);

        // When
        bootstrapper.run(ARGS);

        // Then
        List<TrainingDAO> trainings = entityManager
                .createQuery("SELECT t FROM TrainingDAO t", TrainingDAO.class)
                .getResultList();

        assertThat(trainings).hasSize(1);
        assertThat(trainings.get(0).getTrainingName()).isEqualTo("Morning Yoga");
        assertThat(trainings.get(0).getTraineeDAO().getUserDAO().getUsername()).isEqualTo("John.Doe");
        assertThat(trainings.get(0).getTrainerDAO().getUserDAO().getUsername()).isEqualTo("Jane.Smith");
        assertThat(trainings.get(0).getTrainingTypeDAO().getTrainingTypeName()).isEqualTo(TrainingTypeEnum.YOGA);
    }

    @Test
    void shouldFailStartupWhenJsonLoadingFails() {
        // Given
        RuntimeException loadError = new RuntimeException("Failed to load JSON");
        when(jsonDataLoader.loadBootstrapData()).thenThrow(loadError);

        // When & Then
        assertThatThrownBy(() -> bootstrapper.run(ARGS))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Database bootstrap failed")
                .hasCauseInstanceOf(RuntimeException.class)
                .getCause()
                .hasMessage("Failed to load JSON");

        assertThat(bootstrapper.isInitialized()).isFalse();
        assertThat(bootstrapper.getLastError()).isNotNull();
        assertThat(bootstrapper.getLastError().getMessage()).contains("Failed to load JSON");
    }

    @Test
    void shouldHandleEmptyBootstrapData() throws Exception {
        // Given
        InitialBootstrapData emptyData = new InitialBootstrapData();
        emptyData.setUsers(List.of());
        emptyData.setTrainers(List.of());
        emptyData.setTrainees(List.of());
        emptyData.setTrainingTypes(List.of());
        emptyData.setTrainings(List.of());

        when(jsonDataLoader.loadBootstrapData()).thenReturn(emptyData);

        // When
        bootstrapper.run(ARGS);

        // Then
        assertThat(bootstrapper.isInitialized()).isFalse();
        assertThat(bootstrapper.getUserCount()).isEqualTo(0);
        assertThat(bootstrapper.getTrainerCount()).isEqualTo(0);
        assertThat(bootstrapper.getTraineeCount()).isEqualTo(0);
    }

    @Test
    void shouldCreateAllTrainingTypes() throws Exception {
        // Given
        InitialBootstrapData data = new InitialBootstrapData();
        data.setTrainingTypes(
                List.of(new TrainingTypeDTO("YOGA"), new TrainingTypeDTO("CARDIO"), new TrainingTypeDTO("BOXING")));
        data.setUsers(List.of(new UserDTO("Test", "User", "Test.User", "password", true, "TRAINEE")));
        data.setTrainers(List.of());
        data.setTrainees(List.of());
        data.setTrainings(List.of());

        when(jsonDataLoader.loadBootstrapData()).thenReturn(data);

        // When
        bootstrapper.run(ARGS);

        // Then
        List<TrainingTypeDAO> types = entityManager
                .createQuery("SELECT t FROM TrainingTypeDAO t", TrainingTypeDAO.class)
                .getResultList();

        assertThat(types).hasSize(3);
        assertThat(types)
                .extracting(TrainingTypeDAO::getTrainingTypeName)
                .containsExactlyInAnyOrder(TrainingTypeEnum.YOGA, TrainingTypeEnum.CARDIO, TrainingTypeEnum.BOXING);
    }

    @Test
    void shouldPersistUserWithAllFields() throws Exception {
        // Given
        InitialBootstrapData data = new InitialBootstrapData();
        data.setTrainingTypes(List.of());
        data.setUsers(List.of(new UserDTO("John", "Doe", "John.Doe", "securePass123", true, "TRAINER")));
        data.setTrainers(List.of());
        data.setTrainees(List.of());
        data.setTrainings(List.of());

        when(jsonDataLoader.loadBootstrapData()).thenReturn(data);

        // When
        bootstrapper.run(ARGS);

        // Then
        UserDAO user = entityManager
                .createQuery("SELECT u FROM UserDAO u WHERE u.username = :username", UserDAO.class)
                .setParameter("username", "John.Doe")
                .getSingleResult();

        assertThat(user.getFirstName()).isEqualTo("John");
        assertThat(user.getLastName()).isEqualTo("Doe");
        assertThat(user.getUsername()).isEqualTo("John.Doe");
        assertThat(user.getPassword()).isEqualTo("securePass123");
        assertThat(user.getActive()).isTrue();
        assertThat(user.getUserRole()).isEqualTo(UserRole.TRAINER);
    }

    @Test
    void shouldCreateBidirectionalTraineeTrainerRelationship() throws Exception {
        // Given
        when(jsonDataLoader.loadBootstrapData()).thenReturn(createValidBootstrapData());

        // When
        bootstrapper.run(ARGS);

        // Then — trainee side
        TraineeDAO trainee = entityManager
                .createQuery(
                        "SELECT t FROM TraineeDAO t JOIN FETCH t.trainerDAOS WHERE t.userDAO.username = :username",
                        TraineeDAO.class)
                .setParameter("username", "John.Doe")
                .getSingleResult();

        assertThat(trainee.getTrainerDAOS()).hasSize(1);

        // Then — trainer side
        TrainerDAO trainer = entityManager
                .createQuery(
                        "SELECT t FROM TrainerDAO t JOIN FETCH t.traineeDAOS WHERE t.userDAO.username = :username",
                        TrainerDAO.class)
                .setParameter("username", "Jane.Smith")
                .getSingleResult();

        assertThat(trainer.getTraineeDAOS()).hasSize(1);
        assertThat(trainer.getTraineeDAOS().get(0).getUserDAO().getUsername()).isEqualTo("John.Doe");
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    private InitialBootstrapData createValidBootstrapData() {
        InitialBootstrapData data = new InitialBootstrapData();

        data.setTrainingTypes(List.of(new TrainingTypeDTO("YOGA"), new TrainingTypeDTO("CARDIO")));

        data.setUsers(List.of(
                new UserDTO("John", "Doe", "John.Doe", "password123", true, "TRAINEE"),
                new UserDTO("Jane", "Smith", "Jane.Smith", "password456", true, "TRAINER"),
                new UserDTO("Bob", "Jones", "Bob.Jones", "password789", true, "TRAINEE")));

        TrainerDTO trainer = new TrainerDTO();
        trainer.setUsername("Jane.Smith");
        trainer.setSpecialization("YOGA");
        data.setTrainers(List.of(trainer));

        TraineeDTO trainee1 = new TraineeDTO();
        trainee1.setUsername("John.Doe");
        trainee1.setDateOfBirth(LocalDate.of(1990, 1, 1));
        trainee1.setAddress("123 Main St");
        trainee1.setTrainerUsernames(List.of("Jane.Smith"));

        TraineeDTO trainee2 = new TraineeDTO();
        trainee2.setUsername("Bob.Jones");
        trainee2.setDateOfBirth(LocalDate.of(1992, 5, 15));
        trainee2.setAddress("456 Oak Ave");
        trainee2.setTrainerUsernames(List.of());

        data.setTrainees(List.of(trainee1, trainee2));
        data.setTrainings(List.of());

        return data;
    }
}
