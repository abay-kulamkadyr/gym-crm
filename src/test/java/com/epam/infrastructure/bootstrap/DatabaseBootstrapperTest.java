package com.epam.infrastructure.bootstrap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@TestPropertySource(properties = "spring.main.banner-mode=off")
@ActiveProfiles({ "test" })
class DatabaseBootstrapperTest {

    @PersistenceContext
    private EntityManager entityManager;

    private JsonDataLoader jsonDataLoader;

    private DatabaseBootstrapper bootstrapper;

    @BeforeEach
    void setUp() {
        // Clean database
        entityManager.createQuery("DELETE FROM TrainingDAO").executeUpdate();
        entityManager.createQuery("DELETE FROM TraineeDAO").executeUpdate();
        entityManager.createQuery("DELETE FROM TrainerDAO").executeUpdate();
        entityManager.createQuery("DELETE FROM UserDAO").executeUpdate();
        entityManager.createQuery("DELETE FROM TrainingTypeDAO").executeUpdate();
        entityManager.flush();

        // Create mock JsonDataLoader and DatabaseBootstrapper manually
        jsonDataLoader = mock(JsonDataLoader.class);
        bootstrapper = new DatabaseBootstrapper(jsonDataLoader);

        // Inject EntityManager via reflection (since it's package-private)
        try {
            var field = DatabaseBootstrapper.class.getDeclaredField("entityManager");
            field.setAccessible(true);
            field.set(bootstrapper, entityManager);
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to inject EntityManager", e);
        }
    }

    @Test
    void shouldSuccessfullyBootstrapWithValidData() {
        // Given
        InitialBootstrapData data = createValidBootstrapData();
        when(jsonDataLoader.loadBootstrapData()).thenReturn(data);

        ContextRefreshedEvent event = mock(ContextRefreshedEvent.class);

        // When
        bootstrapper.onApplicationEvent(event);

        // Then
        assertThat(bootstrapper.isInitialized()).isTrue();
        assertThat(bootstrapper.isSkipped()).isFalse();
        assertThat(bootstrapper.getLastError()).isNull();

        // Verify counts
        assertThat(bootstrapper.getTrainingTypeCount()).isEqualTo(2);
        assertThat(bootstrapper.getUserCount()).isEqualTo(3);
        assertThat(bootstrapper.getTrainerCount()).isEqualTo(1);
        assertThat(bootstrapper.getTraineeCount()).isEqualTo(2);

        // Verify data persisted
        Long userCount = entityManager.createQuery("SELECT COUNT(u) FROM UserDAO u", Long.class).getSingleResult();
        assertThat(userCount).isEqualTo(3);

        Long trainerCount =
                entityManager.createQuery("SELECT COUNT(t) FROM TrainerDAO t", Long.class).getSingleResult();
        assertThat(trainerCount).isEqualTo(1);

        Long traineeCount =
                entityManager.createQuery("SELECT COUNT(t) FROM TraineeDAO t", Long.class).getSingleResult();
        assertThat(traineeCount).isEqualTo(2);
    }

    @Test
    void shouldSkipBootstrapWhenDataAlreadyExists() {
        // Given - Create existing user
        UserDAO existingUser = new UserDAO();
        existingUser.setFirstName("Existing");
        existingUser.setLastName("User");
        existingUser.setUsername("Existing.User");
        existingUser.setPassword("password123");
        existingUser.setActive(true);
        existingUser.setUserRole(UserRole.TRAINEE);
        entityManager.persist(existingUser);
        entityManager.flush();

        InitialBootstrapData data = createValidBootstrapData();
        when(jsonDataLoader.loadBootstrapData()).thenReturn(data);

        ContextRefreshedEvent event = mock(ContextRefreshedEvent.class);

        // When
        bootstrapper.onApplicationEvent(event);

        // Then
        assertThat(bootstrapper.isInitialized()).isTrue();
        assertThat(bootstrapper.isSkipped()).isTrue();
        assertThat(bootstrapper.getUserCount()).isEqualTo(1);

        // Verify no additional data was created
        Long userCount = entityManager.createQuery("SELECT COUNT(u) FROM UserDAO u", Long.class).getSingleResult();
        assertThat(userCount).isEqualTo(1); // Only the existing user
    }

    @Test
    void shouldCreateTraineeTrainerRelationshipsCorrectly() {
        // Given
        InitialBootstrapData data = createValidBootstrapData();
        when(jsonDataLoader.loadBootstrapData()).thenReturn(data);

        ContextRefreshedEvent event = mock(ContextRefreshedEvent.class);

        // When
        bootstrapper.onApplicationEvent(event);

        // Then
        String jpql = "SELECT t FROM TraineeDAO t JOIN FETCH t.trainerDAOS WHERE t.userDAO.username = :username";
        TraineeDAO trainee = entityManager
                .createQuery(jpql, TraineeDAO.class)
                .setParameter("username", "John.Doe")
                .getSingleResult();

        assertThat(trainee.getTrainerDAOS()).hasSize(1);
        assertThat(trainee.getTrainerDAOS().get(0).getUserDAO().getUsername()).isEqualTo("Jane.Smith");
    }

    @Test
    void shouldHandleMissingTrainerReference() {
        // Given
        InitialBootstrapData data = new InitialBootstrapData();

        // Create training types
        data.setTrainingTypes(List.of(new TrainingTypeDTO("YOGA")));

        // Create users
        data.setUsers(List.of(new UserDTO("John", "Doe", "John.Doe", "password123", true, "TRAINEE")));

        // Create trainee with reference to non-existent trainer
        TraineeDTO trainee = new TraineeDTO();
        trainee.setUsername("John.Doe");
        trainee.setDateOfBirth(LocalDate.of(1990, 1, 1));
        trainee.setAddress("123 Main St");
        trainee.setTrainerUsernames(List.of("NonExistent.Trainer")); // This trainer
        // doesn't exist
        data.setTrainees(List.of(trainee));

        data.setTrainers(List.of()); // No trainers
        data.setTrainings(List.of());

        when(jsonDataLoader.loadBootstrapData()).thenReturn(data);
        ContextRefreshedEvent event = mock(ContextRefreshedEvent.class);

        // When
        bootstrapper.onApplicationEvent(event);

        // Then - Should complete successfully without the invalid relationship
        assertThat(bootstrapper.isInitialized()).isTrue();
        assertThat(bootstrapper.getTraineeCount()).isEqualTo(1);

        TraineeDAO persistedTrainee = entityManager
                .createQuery("SELECT t FROM TraineeDAO t WHERE t.userDAO.username = :username", TraineeDAO.class)
                .setParameter("username", "John.Doe")
                .getSingleResult();

        assertThat(persistedTrainee.getTrainerDAOS()).isEmpty(); // No relationship
        // created
    }

    @Test
    void shouldHandleMissingTraineeInTraining() {
        // Given
        InitialBootstrapData data = new InitialBootstrapData();

        data.setTrainingTypes(List.of(new TrainingTypeDTO("YOGA")));
        data.setUsers(List.of(new UserDTO("Jane", "Smith", "Jane.Smith", "password123", true, "TRAINER")));

        TrainerDTO trainer = new TrainerDTO();
        trainer.setUsername("Jane.Smith");
        trainer.setSpecialization("YOGA");
        data.setTrainers(List.of(trainer));

        data.setTrainees(List.of());

        // Create training with non-existent trainee
        TrainingDTO training = new TrainingDTO();
        training.setName("Morning Yoga");
        training.setDate(LocalDateTime.now());
        training.setDurationMinutes(60);
        training.setTraineeUsername("NonExistent.Trainee"); // This trainee doesn't exist
        training.setTrainerUsername("Jane.Smith");
        training.setTrainingType("YOGA");
        data.setTrainings(List.of(training));

        when(jsonDataLoader.loadBootstrapData()).thenReturn(data);
        ContextRefreshedEvent event = mock(ContextRefreshedEvent.class);

        // When
        bootstrapper.onApplicationEvent(event);

        // Then - Should complete without creating the invalid training
        assertThat(bootstrapper.isInitialized()).isTrue();

        Long trainingCount =
                entityManager.createQuery("SELECT COUNT(t) FROM TrainingDAO t", Long.class).getSingleResult();
        assertThat(trainingCount).isEqualTo(0); // Training not created
    }

    @Test
    void shouldPersistTrainingsWithAllValidReferences() {
        // Given
        InitialBootstrapData data = createValidBootstrapData();

        // Add training
        TrainingDTO training = new TrainingDTO();
        training.setName("Morning Yoga");
        training.setDate(LocalDateTime.now());
        training.setDurationMinutes(60);
        training.setTraineeUsername("John.Doe");
        training.setTrainerUsername("Jane.Smith");
        training.setTrainingType("YOGA");
        data.setTrainings(List.of(training));

        when(jsonDataLoader.loadBootstrapData()).thenReturn(data);
        ContextRefreshedEvent event = mock(ContextRefreshedEvent.class);

        // When
        bootstrapper.onApplicationEvent(event);

        // Then
        List<TrainingDAO> trainings =
                entityManager.createQuery("SELECT t FROM TrainingDAO t", TrainingDAO.class).getResultList();

        assertThat(trainings).hasSize(1);
        assertThat(trainings.get(0).getTrainingName()).isEqualTo("Morning Yoga");
        assertThat(trainings.get(0).getTraineeDAO().getUserDAO().getUsername()).isEqualTo("John.Doe");
        assertThat(trainings.get(0).getTrainerDAO().getUserDAO().getUsername()).isEqualTo("Jane.Smith");
        assertThat(trainings.get(0).getTrainingTypeDAO().getTrainingTypeName()).isEqualTo(TrainingTypeEnum.YOGA);
    }

    @Test
    void shouldSetErrorStateWhenJsonLoadingFails() {
        // Given
        RuntimeException loadError = new RuntimeException("Failed to load JSON");
        when(jsonDataLoader.loadBootstrapData()).thenThrow(loadError);

        ContextRefreshedEvent event = mock(ContextRefreshedEvent.class);

        // When & Then
        assertThatThrownBy(() -> bootstrapper.onApplicationEvent(event))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Database bootstrap error");

        assertThat(bootstrapper.isInitialized()).isFalse();
        assertThat(bootstrapper.getLastError()).isNotNull();
        assertThat(bootstrapper.getLastError().getMessage()).contains("Failed to load JSON");
    }

    @Test
    void shouldHandleEmptyBootstrapData() {
        // Given
        InitialBootstrapData emptyData = new InitialBootstrapData();
        emptyData.setUsers(List.of());
        emptyData.setTrainers(List.of());
        emptyData.setTrainees(List.of());
        emptyData.setTrainingTypes(List.of());
        emptyData.setTrainings(List.of());

        when(jsonDataLoader.loadBootstrapData()).thenReturn(emptyData);
        ContextRefreshedEvent event = mock(ContextRefreshedEvent.class);

        // When
        bootstrapper.onApplicationEvent(event);

        // Then - Should complete without errors but not set initialized
        assertThat(bootstrapper.getUserCount()).isEqualTo(0);
        assertThat(bootstrapper.getTrainerCount()).isEqualTo(0);
        assertThat(bootstrapper.getTraineeCount()).isEqualTo(0);
    }

    @Test
    void shouldCreateAllTrainingTypes() {
        // Given
        InitialBootstrapData data = new InitialBootstrapData();
        data
                .setTrainingTypes(
                    List.of(new TrainingTypeDTO("YOGA"), new TrainingTypeDTO("CARDIO"), new TrainingTypeDTO("BOXING")));
        data.setUsers(List.of(new UserDTO("Test", "User", "Test.User", "password", true, "TRAINEE")));
        data.setTrainers(List.of());
        data.setTrainees(List.of());
        data.setTrainings(List.of());

        when(jsonDataLoader.loadBootstrapData()).thenReturn(data);
        ContextRefreshedEvent event = mock(ContextRefreshedEvent.class);

        // When
        bootstrapper.onApplicationEvent(event);

        // Then
        List<TrainingTypeDAO> types =
                entityManager.createQuery("SELECT t FROM TrainingTypeDAO t", TrainingTypeDAO.class).getResultList();

        assertThat(types).hasSize(3);
        assertThat(types)
                .extracting(TrainingTypeDAO::getTrainingTypeName)
                .containsExactlyInAnyOrder(TrainingTypeEnum.YOGA, TrainingTypeEnum.CARDIO, TrainingTypeEnum.BOXING);
    }

    @Test
    void shouldPersistUserWithAllFields() {
        // Given
        InitialBootstrapData data = new InitialBootstrapData();
        data.setTrainingTypes(List.of());
        data.setUsers(List.of(new UserDTO("John", "Doe", "John.Doe", "securePass123", true, "TRAINER")));
        data.setTrainers(List.of());
        data.setTrainees(List.of());
        data.setTrainings(List.of());

        when(jsonDataLoader.loadBootstrapData()).thenReturn(data);
        ContextRefreshedEvent event = mock(ContextRefreshedEvent.class);

        // When
        bootstrapper.onApplicationEvent(event);

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
    void shouldCreateBidirectionalTraineeTrainerRelationship() {
        // Given
        InitialBootstrapData data = createValidBootstrapData();
        when(jsonDataLoader.loadBootstrapData()).thenReturn(data);

        ContextRefreshedEvent event = mock(ContextRefreshedEvent.class);

        // When
        bootstrapper.onApplicationEvent(event);

        // Then - Check trainee side
        TraineeDAO trainee = entityManager
                .createQuery(
                    "SELECT t FROM TraineeDAO t JOIN FETCH t.trainerDAOS WHERE t.userDAO.username = :username",
                    TraineeDAO.class)
                .setParameter("username", "John.Doe")
                .getSingleResult();

        assertThat(trainee.getTrainerDAOS()).hasSize(1);

        // Check trainer side
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

        // Training types
        data.setTrainingTypes(List.of(new TrainingTypeDTO("YOGA"), new TrainingTypeDTO("CARDIO")));

        // Users
        data
                .setUsers(
                    List
                            .of(
                                new UserDTO("John", "Doe", "John.Doe", "password123", true, "TRAINEE"),
                                new UserDTO("Jane", "Smith", "Jane.Smith", "password456", true, "TRAINER"),
                                new UserDTO("Bob", "Jones", "Bob.Jones", "password789", true, "TRAINEE")));

        // Trainers
        TrainerDTO trainer = new TrainerDTO();
        trainer.setUsername("Jane.Smith");
        trainer.setSpecialization("YOGA");
        data.setTrainers(List.of(trainer));

        // Trainees
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

        // Trainings (initially empty, can be added per test)
        data.setTrainings(List.of());

        return data;
    }

}
