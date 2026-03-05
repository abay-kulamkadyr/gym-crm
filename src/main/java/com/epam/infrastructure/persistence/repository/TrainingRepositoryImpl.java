package com.epam.infrastructure.persistence.repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.epam.domain.TrainingFilter;
import com.epam.domain.model.Training;
import com.epam.domain.port.TrainingRepository;
import com.epam.infrastructure.persistence.dao.TraineeDAO;
import com.epam.infrastructure.persistence.dao.TrainerDAO;
import com.epam.infrastructure.persistence.dao.TrainingDAO;
import com.epam.infrastructure.persistence.mapper.TrainingMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class TrainingRepositoryImpl implements TrainingRepository {

    private final TrainingMapper trainingMapper;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public TrainingRepositoryImpl(TrainingMapper trainingMapper) {
        this.trainingMapper = trainingMapper;
    }

    @Override
    public Training save(@NonNull Training training) {
        TrainingDAO entity = trainingMapper.toEntity(training);

        if (training.getTrainingId() == null) {
            entityManager.persist(entity);
        } else {
            entityManager.merge(entity);
        }

        return trainingMapper.toDomain(entity);
    }

    @Override
    public Optional<Training> findById(@NonNull Long id) {
        TrainingDAO trainingDAO = entityManager.find(TrainingDAO.class, id);

        if (trainingDAO == null) {
            log.debug("Training with ID {} not found", id);
            return Optional.empty();
        }
        return Optional.of(trainingMapper.toDomain(trainingDAO));
    }

    @Override
    public void delete(@NonNull Long id) {
        TrainingDAO trainingDAO = entityManager.find(TrainingDAO.class, id);

        if (trainingDAO == null) {
            throw new EntityNotFoundException(String.format("Training with ID %d not found", id));
        }

        entityManager.remove(trainingDAO);
    }

    @Override
    public List<Training> getTraineeTrainings(String traineeUsername, TrainingFilter filter) {
        return getTrainings(
                traineeUsername,
                TrainingFilter.forTrainee(
                        filter.fromDate(), filter.toDate(), filter.trainerName(), filter.trainingType()),
                UserType.TRAINEE);
    }

    @Override
    public List<Training> getTrainerTrainings(String trainerUsername, TrainingFilter filter) {
        return getTrainings(
                trainerUsername,
                TrainingFilter.forTrainer(filter.fromDate(), filter.toDate(), filter.traineeName()),
                UserType.TRAINER);
    }

    @Override
    public void deleteByTraineeTrainerAndDate(String traineeUsername, String trainerUsername, LocalDateTime date) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaDelete<TrainingDAO> delete = cb.createCriteriaDelete(TrainingDAO.class);
        Root<TrainingDAO> root = delete.from(TrainingDAO.class);

        // Join paths to reach the usernames in the UserDAO
        Predicate traineeMatch = cb.equal(root.get("traineeDAO").get("userDAO").get("username"), traineeUsername);
        Predicate trainerMatch = cb.equal(root.get("trainerDAO").get("userDAO").get("username"), trainerUsername);
        Predicate dateMatch = cb.equal(root.get("trainingDate"), date);

        delete.where(cb.and(traineeMatch, trainerMatch, dateMatch));

        int deletedCount = entityManager.createQuery(delete).executeUpdate();

        if (deletedCount == 0) {
            log.warn(
                    "No training found to delete for Trainee: {}, Trainer: {}, Date: {}",
                    traineeUsername,
                    trainerUsername,
                    date);
        } else {
            log.info("Successfully deleted {} training record(s)", deletedCount);
        }
    }

    @Override
    public Optional<Training> findByTrainerUsernameAndTraineeUsernameAndDate(
            String trainerUsername, String traineeUsername, LocalDateTime date) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<TrainingDAO> cq = cb.createQuery(TrainingDAO.class);
        Root<TrainingDAO> root = cq.from(TrainingDAO.class);

        Predicate trainerMatch = cb.equal(root.get("trainerDAO").get("userDAO").get("username"), trainerUsername);
        Predicate traineeMatch = cb.equal(root.get("traineeDAO").get("userDAO").get("username"), traineeUsername);
        Predicate dateMatch = cb.equal(root.get("trainingDate"), date);

        cq.select(root).where(cb.and(trainerMatch, traineeMatch, dateMatch));

        List<TrainingDAO> results = entityManager.createQuery(cq).getResultList();

        if (results.isEmpty()) {
            log.debug(
                    "No training found for Trainer: {}, Trainee: {}, Date: {}", trainerUsername, traineeUsername, date);
            return Optional.empty();
        }

        if (results.size() > 1) {
            log.warn(
                    "Multiple trainings found for Trainer: {}, Trainee: {}, Date: {}. Returning first.",
                    trainerUsername,
                    traineeUsername,
                    date);
        }

        return Optional.of(trainingMapper.toDomain(results.get(0)));
    }

    private List<Training> getTrainings(String requestedUsername, TrainingFilter trainingFilter, UserType userType) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<TrainingDAO> cq = cb.createQuery(TrainingDAO.class);
        Root<TrainingDAO> trainingRoot = cq.from(TrainingDAO.class);

        // Eagerly loads associated data in a single SQL query via JOINs, preventing per-row SELECT statements
        Fetch<TrainingDAO, TraineeDAO> traineeFetch = trainingRoot.fetch("traineeDAO", JoinType.INNER);
        traineeFetch.fetch("userDAO", JoinType.INNER); // ✅ also fetch the nested userDAO

        Fetch<TrainingDAO, TrainerDAO> trainerFetch = trainingRoot.fetch("trainerDAO", JoinType.INNER);
        trainerFetch.fetch("userDAO", JoinType.INNER); // ✅ same here

        trainingRoot.fetch("trainingTypeDAO", JoinType.INNER);

        // Criteria API requires casting Fetch -> Join to use in predicates
        Join<TrainingDAO, TraineeDAO> traineeJoin = (Join<TrainingDAO, TraineeDAO>) traineeFetch;
        Join<TrainingDAO, TrainerDAO> trainerJoin = (Join<TrainingDAO, TrainerDAO>) trainerFetch;

        List<Predicate> predicates = new ArrayList<>();

        if (userType == UserType.TRAINEE) {
            predicates.add(cb.equal(traineeJoin.get("userDAO").get("username"), requestedUsername));
        } else {
            predicates.add(cb.equal(trainerJoin.get("userDAO").get("username"), requestedUsername));
        }

        trainingFilter
                .fromDate()
                .ifPresent(from -> predicates.add(cb.greaterThanOrEqualTo(trainingRoot.get("trainingDate"), from)));

        trainingFilter
                .toDate()
                .ifPresent(to -> predicates.add(cb.lessThanOrEqualTo(trainingRoot.get("trainingDate"), to)));

        trainingFilter
                .trainerName()
                .ifPresent(username ->
                        predicates.add(cb.equal(trainerJoin.get("userDAO").get("username"), username)));

        trainingFilter
                .traineeName()
                .ifPresent(username ->
                        predicates.add(cb.equal(traineeJoin.get("userDAO").get("username"), username)));

        trainingFilter
                .trainingType()
                .ifPresent(typeName -> predicates.add(
                        cb.equal(trainingRoot.get("trainingTypeDAO").get("trainingTypeName"), typeName)));

        cq.select(trainingRoot)
                .where(cb.and(predicates.toArray(new Predicate[0])))
                .orderBy(cb.asc(trainingRoot.get("trainingDate")))
                .distinct(true);

        return entityManager.createQuery(cq).getResultList().stream()
                .map(trainingMapper::toDomain)
                .toList();
    }

    private enum UserType {
        TRAINEE,
        TRAINER
    }
}
