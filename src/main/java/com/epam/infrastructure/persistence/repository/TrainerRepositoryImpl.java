package com.epam.infrastructure.persistence.repository;

import com.epam.domain.model.Trainer;
import com.epam.domain.repository.TrainerRepository;
import com.epam.infrastructure.persistence.dao.TrainerDAO;
import com.epam.infrastructure.persistence.dao.UserDAO;
import com.epam.infrastructure.persistence.mapper.TrainerMapper;
import com.epam.infrastructure.persistence.util.UsernameFinder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class TrainerRepositoryImpl implements TrainerRepository {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public Trainer save(@NonNull Trainer trainer) {
		TrainerDAO entity = TrainerMapper.toEntity(trainer);

		if (trainer.getTrainerId() == null) {
			entityManager.persist(entity);
		}
		else {
			entityManager.merge(entity);
		}

		return TrainerMapper.toDomain(entity);
	}

	@Override
	public Optional<Trainer> findById(@NonNull Long id) {
		TrainerDAO trainerDAO = entityManager.find(TrainerDAO.class, id);

		if (trainerDAO == null) {
			log.warn("Trainer with ID {} not found", id);
			return Optional.empty();
		}

		return Optional.of(TrainerMapper.toDomain(trainerDAO));
	}

	@Override
	public void delete(@NonNull Long id) {
		TrainerDAO trainerDAO = entityManager.find(TrainerDAO.class, id);

		if (trainerDAO == null) {
			throw new EntityNotFoundException(String.format("Trainer with ID %d not found", id));
		}

		entityManager.remove(trainerDAO);
	}

	@Override
	public Optional<Trainer> findByUsername(String username) {
		String jpql = "SELECT t FROM TrainerDAO t WHERE t.userDAO.username = :username";
		List<TrainerDAO> results = entityManager.createQuery(jpql, TrainerDAO.class)
			.setParameter("username", username)
			.getResultList();

		if (results.isEmpty()) {
			log.warn("Trainer with username '{}' not found", username);
			return Optional.empty();
		}

		return Optional.of(TrainerMapper.toDomain(results.get(0)));
	}

	@Override
	public Optional<String> findLatestUsername(String prefix) {
		String jpql = "SELECT u FROM UserDAO u WHERE u.username LIKE :prefix";
		List<UserDAO> userDAOS = entityManager.createQuery(jpql, UserDAO.class)
			.setParameter("prefix", prefix + "%")
			.getResultList();

		return UsernameFinder.findLatestUsername(userDAOS, prefix, UserDAO::getUsername);
	}

	@Override
	public void deleteByUsername(String username) {
		Trainer trainer = findByUsername(username).orElseThrow(
				() -> new EntityNotFoundException(String.format("Trainer with username '%s' not found", username)));

		delete(trainer.getTrainerId());
	}

}
