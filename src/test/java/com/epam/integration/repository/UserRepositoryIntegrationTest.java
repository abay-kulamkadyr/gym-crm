package com.epam.integration.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Optional;

import com.epam.domain.model.Trainee;
import com.epam.domain.model.Trainer;
import com.epam.domain.model.User;
import com.epam.domain.port.UserRepository;
import com.epam.integration.base.SeededIntegrationTestBase;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.InvalidDataAccessApiUsageException;

class UserRepositoryIntegrationTest extends SeededIntegrationTestBase {

    private static final String TRAINEE_USERNAME = "David.Davis";
    private static final String TRAINER_USERNAME = "John.Smith";
    private static final String GHOST_USERNAME = "Ghost.User";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void findByUsername_withExistingTrainee_returnsTraineeInstance() {
        Optional<User> result = userRepository.findByUsername(TRAINEE_USERNAME);

        assertThat(result).isPresent();
        assertThat(result.get()).isInstanceOf(Trainee.class);
        assertThat(result.get().getUsername()).isEqualTo(TRAINEE_USERNAME);
    }

    @Test
    void findByUsername_withExistingTrainer_returnsTrainerInstance() {
        Optional<User> result = userRepository.findByUsername(TRAINER_USERNAME);

        assertThat(result).isPresent();
        assertThat(result.get()).isInstanceOf(Trainer.class);
        assertThat(result.get().getUsername()).isEqualTo(TRAINER_USERNAME);
    }

    @Test
    void findByUsername_withNonExistentUsername_returnsEmpty() {
        Optional<User> result = userRepository.findByUsername(GHOST_USERNAME);

        assertThat(result).isEmpty();
    }

    @Test
    void findByUsername_trainee_mapsAllFieldsCorrectly() {
        Trainee trainee =
                (Trainee) userRepository.findByUsername(TRAINEE_USERNAME).orElseThrow();

        assertThat(trainee.getFirstName()).isEqualTo("David");
        assertThat(trainee.getLastName()).isEqualTo("Davis");
        assertThat(trainee.getActive()).isTrue();
        assertThat(trainee.getTraineeId()).isNotNull();
        assertThat(trainee.getUserId()).isNotNull();
    }

    @Test
    void findByUsername_trainer_mapsAllFieldsCorrectly() {
        Trainer trainer =
                (Trainer) userRepository.findByUsername(TRAINER_USERNAME).orElseThrow();

        assertThat(trainer.getFirstName()).isEqualTo("John");
        assertThat(trainer.getLastName()).isEqualTo("Smith");
        assertThat(trainer.getActive()).isTrue();
        assertThat(trainer.getTrainerId()).isNotNull();
        assertThat(trainer.getSpecialization()).isNotNull();
    }

    @Test
    void findByUsername_whenTraineeRoleButMissingProfile_throwsInvalidAccessApiUsageException() {
        // Insert a user with TRAINEE role but no corresponding trainee row
        entityManager
                .createNativeQuery(
                        "INSERT INTO users (user_id, first_name, last_name, username, password, is_active, role) "
                                + "VALUES (99, 'Broken', 'Trainee', 'Broken.Trainee', 'pass', true, 'TRAINEE')")
                .executeUpdate();
        entityManager.flush();
        entityManager.clear(); // force re-fetch so LEFT JOIN returns null traineeDAO

        assertThatThrownBy(() -> userRepository.findByUsername("Broken.Trainee"))
                .isInstanceOf(InvalidDataAccessApiUsageException.class)
                .hasMessageContaining("TRAINEE");
    }

    @Test
    void findByUsername_whenTrainerRoleButMissingProfile_throwsInvalidAccessApiUsageException() {
        entityManager
                .createNativeQuery(
                        "INSERT INTO users (user_id, first_name, last_name, username, password, is_active, role) "
                                + "VALUES (98, 'Broken', 'Trainer', 'Broken.Trainer', 'pass', true, 'TRAINER')")
                .executeUpdate();
        entityManager.flush();
        entityManager.clear();

        assertThatThrownBy(() -> userRepository.findByUsername("Broken.Trainer"))
                .isInstanceOf(InvalidDataAccessApiUsageException.class)
                .hasMessageContaining("TRAINER");
    }

    @Test
    void findByUsername_whenInvalidRole_throwsInvalidAccessApiUsageException() {
        entityManager
                .createNativeQuery(
                        "INSERT INTO users (user_id, first_name, last_name, username, password, is_active, role) "
                                + "VALUES (97, 'No', 'Role', 'No.Role', 'pass', true, NULL)")
                .executeUpdate();
        entityManager.flush();
        entityManager.clear();

        assertThatThrownBy(() -> userRepository.findByUsername("No.Role"))
                .isInstanceOf(InvalidDataAccessApiUsageException.class);
    }
}
