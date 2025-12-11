package com.epam.infrastructure.persistence.repository;

import java.util.Optional;

import com.epam.domain.model.User;
import com.epam.domain.model.UserRole;
import com.epam.domain.port.UserRepository;
import com.epam.infrastructure.persistence.dao.UserDAO;
import com.epam.infrastructure.persistence.mapper.TraineeMapper;
import com.epam.infrastructure.persistence.mapper.TrainerMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryImpl implements UserRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<User> findByUsername(String username) {
        String query = """
                           SELECT u FROM UserDAO u
                           LEFT JOIN FETCH u.traineeDAO
                           LEFT JOIN FETCH u.trainerDAO
                           WHERE u.username = :username
                       """;

        Optional<UserDAO> userOpt = entityManager
                .createQuery(query, UserDAO.class)
                .setParameter("username", username)
                .getResultStream()
                .findFirst();

        return userOpt.map(this::mapToDomain);
    }

    private User mapToDomain(UserDAO userDAO) {
        if (userDAO.getUserRole() == UserRole.TRAINEE) {

            if (userDAO.getTraineeDAO() == null) {
                throw new IllegalStateException("Data Integrity Error: Role is TRAINEE but profile missing");
            }
            return TraineeMapper.toDomain(userDAO.getTraineeDAO());

        }
        else if (userDAO.getUserRole() == UserRole.TRAINER) {

            if (userDAO.getTrainerDAO() == null) {
                throw new IllegalStateException("Data Integrity Error: Role is TRAINER but profile missing");
            }
            return TrainerMapper.toDomain(userDAO.getTrainerDAO());
        }

        throw new IllegalStateException("Invalid Role");
    }

}
