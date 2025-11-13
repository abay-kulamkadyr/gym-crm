package com.epam.infrastructure.persistence.repository;

import com.epam.domain.TrainingFilter;
import com.epam.domain.model.Training;
import com.epam.domain.repository.TrainingRepository;
import com.epam.infrastructure.persistence.dao.TraineeDAO;
import com.epam.infrastructure.persistence.dao.TrainerDAO;
import com.epam.infrastructure.persistence.dao.TrainingDAO;
import com.epam.infrastructure.persistence.dao.TrainingTypeDAO;
import com.epam.infrastructure.persistence.mapper.TrainingMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class TrainingRepositoryImpl implements TrainingRepository {

	@PersistenceContext
	private EntityManager entityManager;

	private final TrainingMapper trainingMapper;

	@Autowired
	public TrainingRepositoryImpl(TrainingMapper trainingMapper) {
		this.trainingMapper = trainingMapper;
	}

	@Override
	public Training save(@NonNull Training training) {
		TrainingDAO entity = trainingMapper.toEntity(training);

		if (training.getTrainingId() == null) {
			entityManager.persist(entity);
		}
		else {
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
		return getTrainings(traineeUsername, TrainingFilter.forTrainee(filter.fromDate(), filter.toDate(),
				filter.trainerName(), filter.trainingType()), UserType.TRAINEE);
	}

	@Override
	public List<Training> getTrainerTrainings(String trainerUsername, TrainingFilter filter) {
		return getTrainings(trainerUsername,
				TrainingFilter.forTrainer(filter.fromDate(), filter.toDate(), filter.traineeName()), UserType.TRAINER);
	}

	private List<Training> getTrainings(String requestedUsername, TrainingFilter trainingFilter, UserType userType) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<TrainingDAO> cq = cb.createQuery(TrainingDAO.class);
		Root<TrainingDAO> trainingRoot = cq.from(TrainingDAO.class);

		Join<TrainingDAO, TraineeDAO> traineeJoin = trainingRoot.join("traineeDAO");
		Join<TrainingDAO, TrainerDAO> trainerJoin = trainingRoot.join("trainerDAO");
		Join<TrainingDAO, TrainingTypeDAO> typeJoin = trainingRoot.join("trainingTypeDAO");

		List<Predicate> predicates = new ArrayList<>();

		// --- Dynamic Primary Predicate ---
		if (userType == UserType.TRAINEE) {
			predicates.add(cb.equal(traineeJoin.get("userDAO").get("username"), requestedUsername));
		}
		else {
			predicates.add(cb.equal(trainerJoin.get("userDAO").get("username"), requestedUsername));
		}

		trainingFilter.fromDate()
			.ifPresent(from -> predicates.add(cb.greaterThanOrEqualTo(trainingRoot.get("trainingDate"), from)));

		trainingFilter.toDate()
			.ifPresent(to -> predicates.add(cb.lessThanOrEqualTo(trainingRoot.get("trainingDate"), to)));

		trainingFilter.trainerName()
			.ifPresent(username -> predicates.add(cb.equal(trainerJoin.get("userDAO").get("username"), username)));

		trainingFilter.traineeName()
			.ifPresent(username -> predicates.add(cb.equal(traineeJoin.get("userDAO").get("username"), username)));

		trainingFilter.trainingType()
			.ifPresent(typeName -> predicates.add(cb.equal(typeJoin.get("trainingTypeName"), typeName)));

		cq.select(trainingRoot)
			.where(cb.and(predicates.toArray(new Predicate[0])))
			.orderBy(cb.asc(trainingRoot.get("trainingDate")));

		List<TrainingDAO> result = entityManager.createQuery(cq).getResultList();

		return result.stream().map(trainingMapper::toDomain).toList();
	}

	private enum UserType {

		TRAINEE, TRAINER

	}

}
