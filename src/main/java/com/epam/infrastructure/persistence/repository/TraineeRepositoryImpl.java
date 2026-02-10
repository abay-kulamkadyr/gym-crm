package com.epam.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import com.epam.domain.model.Trainee;
import com.epam.domain.model.Trainer;
import com.epam.domain.port.TraineeRepository;
import com.epam.domain.port.TrainerRepository;
import com.epam.infrastructure.persistence.dao.TraineeDAO;
import com.epam.infrastructure.persistence.dao.TrainerDAO;
import com.epam.infrastructure.persistence.dao.UserDAO;
import com.epam.infrastructure.persistence.mapper.TraineeMapper;
import com.epam.infrastructure.persistence.mapper.TrainerMapper;
import com.epam.infrastructure.persistence.util.UsernameFinder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class TraineeRepositoryImpl implements TraineeRepository {

    @PersistenceContext
    private EntityManager entityManager;

    private TrainerRepository trainerRepository;

    @Autowired
    public void setTrainerRepository(TrainerRepository trainerRepository) {
        this.trainerRepository = trainerRepository;
    }

    @Override
    public Trainee save(@NonNull Trainee trainee) {
        TraineeDAO entity = TraineeMapper.toEntity(trainee);

        if (trainee.getTraineeId() == null) {
            entityManager.persist(entity);
        } else {
            TraineeDAO existing = entityManager.find(TraineeDAO.class, entity.getTraineeId());
            TraineeMapper.updateEntity(existing, trainee);
        }

        return TraineeMapper.toDomain(entity);
    }

    @Override
    public Optional<Trainee> findById(@NonNull Long id) {
        TraineeDAO traineeDAO = entityManager.find(TraineeDAO.class, id);

        if (traineeDAO == null) {
            log.warn("Trainee with ID {} not found", id);
            return Optional.empty();
        }

        return Optional.of(TraineeMapper.toDomain(traineeDAO));
    }

    @Override
    public void delete(@NonNull Long id) {
        TraineeDAO traineeDAO = entityManager.find(TraineeDAO.class, id);

        if (traineeDAO == null) {
            throw new EntityNotFoundException(String.format("Trainee with ID %d not found", id));
        }

        entityManager.remove(traineeDAO);
    }

    @Override
    public Optional<Trainee> findByUsername(String username) {
        String jpql = "SELECT t FROM TraineeDAO t WHERE t.userDAO.username = :username";
        List<TraineeDAO> results = entityManager
                .createQuery(jpql, TraineeDAO.class)
                .setParameter("username", username)
                .getResultList();

        if (results.isEmpty()) {
            log.warn("Trainee with username '{}' not found", username);
            return Optional.empty();
        }

        return Optional.of(TraineeMapper.toDomain(results.get(0)));
    }

    @Override
    public Optional<String> findLatestUsername(String prefix) {
        String jpql = "SELECT u FROM UserDAO u WHERE u.username LIKE :prefix";
        List<UserDAO> userDAOS = entityManager
                .createQuery(jpql, UserDAO.class)
                .setParameter("prefix", prefix + "%")
                .getResultList();

        return UsernameFinder.findLatestUsername(userDAOS, prefix, UserDAO::getUsername);
    }

    @Override
    public List<Trainer> getTrainers(String traineeUsername) {
        if (findByUsername(traineeUsername).isEmpty()) {
            throw new EntityNotFoundException(String.format("Trainee with username '%s' not found", traineeUsername));
        }
        String jpql =
                """
                      SELECT t
                      FROM TraineeDAO t
                      LEFT JOIN FETCH t.trainerDAOS
                      WHERE t.userDAO.username = :username
                      """;
        TraineeDAO traineeDAO = entityManager
                .createQuery(jpql, TraineeDAO.class)
                .setParameter("username", traineeUsername)
                .getSingleResult();

        return traineeDAO.getTrainerDAOS().stream().map(TrainerMapper::toDomain).toList();
    }

    @Override
    public List<Trainer> getUnassignedTrainers(String traineeUsername) {
        if (findByUsername(traineeUsername).isEmpty()) {
            throw new EntityNotFoundException(String.format("Trainee with username '%s' not found", traineeUsername));
        }

        String jpql =
                """
                          SELECT tr
                          FROM TrainerDAO tr
                          WHERE tr.trainerId NOT IN (
                              SELECT t2.trainerId
                              FROM TraineeDAO t
                              JOIN t.trainerDAOS t2
                              WHERE t.userDAO.username = :username
                          ) AND tr.userDAO.active=true
                      """;

        List<TrainerDAO> unassignedTrainers = entityManager
                .createQuery(jpql, TrainerDAO.class)
                .setParameter("username", traineeUsername)
                .getResultList();

        return unassignedTrainers.stream().map(TrainerMapper::toDomain).toList();
    }

    @Override
    public void deleteByUsername(String username) {
        Trainee trainee = findByUsername(username)
                .orElseThrow(() ->
                        new EntityNotFoundException(String.format("Trainee with username '%s' not found", username)));

        delete(trainee.getTraineeId());
    }

    @Override
    public void updateTrainersList(String traineeUsername, List<String> trainerUsernames) {
        // Fetch trainee with trainers collection
        String jpql =
                """
                          SELECT t FROM TraineeDAO t
                          LEFT JOIN FETCH t.trainerDAOS
                          WHERE t.userDAO.username = :username
                      """;

        List<TraineeDAO> results = entityManager
                .createQuery(jpql, TraineeDAO.class)
                .setParameter("username", traineeUsername)
                .getResultList();

        if (results.isEmpty()) {
            throw new EntityNotFoundException(String.format("Trainee with username '%s' not found", traineeUsername));
        }

        TraineeDAO traineeDAO = results.get(0);
        List<TrainerDAO> trainerDAOS = traineeDAO.getTrainerDAOS();

        // Clear existing associations
        trainerDAOS.clear();

        // Add new trainers
        for (String trainerUsername : trainerUsernames) {
            Long trainerId = trainerRepository
                    .findByUsername(trainerUsername)
                    .map(Trainer::getTrainerId)
                    .orElseThrow(() -> new EntityNotFoundException(
                            String.format("Trainer with username '%s' not found", trainerUsername)));

            TrainerDAO trainerDAO = entityManager.find(TrainerDAO.class, trainerId);

            if (trainerDAO == null) {
                throw new EntityNotFoundException(String.format("Trainer with ID %d not found", trainerId));
            }

            trainerDAOS.add(trainerDAO);
        }
    }
}
