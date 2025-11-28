package com.epam.infrastructure.bootstrap;

import com.epam.domain.model.UserRole;
import com.epam.domain.model.TrainingTypeEnum;
import com.epam.infrastructure.bootstrap.dto.*;
import com.epam.infrastructure.persistence.dao.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.Map;

/**
 * Database bootstrapper for loading initial data from JSON when application context is
 * initialized
 */
@Component
@Slf4j
public class DatabaseBootstrapper implements ApplicationListener<ContextRefreshedEvent> {

	private final JsonDataLoader dataLoader;

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	public DatabaseBootstrapper(JsonDataLoader dataLoader) {
		this.dataLoader = dataLoader;
	}

	@Override
	@Transactional
	public void onApplicationEvent(ContextRefreshedEvent event) {

		try {
			// Check if already initialized
			Long userCount = entityManager.createQuery("SELECT COUNT(u) FROM UserDAO u", Long.class).getSingleResult();

			if (userCount > 0) {
				log.info("Database already initialized ({} users found), skipping bootstrap", userCount);
				return;
			}

			// Load bootstrap data
			InitialBootstrapData data = dataLoader.loadBootstrapData();

			if (data.getUsers().isEmpty()) {
				log.warn("No bootstrap data to load found");
				return;
			}

			// Execute bootstrap steps
			Map<String, TrainingTypeDAO> trainingTypes = persistTrainingTypes(data);
			Map<String, UserDAO> users = persistUsers(data);
			Map<String, TrainerDAO> trainers = persistTrainers(data, users, trainingTypes);
			Map<String, TraineeDAO> trainees = persistTrainees(data, users);
			createTraineeTrainerRelationships(data, trainees, trainers);
			persistTrainings(data, trainees, trainers, trainingTypes);

			log.info("Database initialization complete:");
			log.info("{} Training Types", trainingTypes.size());
			log.info("{} Users", users.size());
			log.info("{} Trainers", trainers.size());
			log.info("{} Trainees", trainees.size());

		}
		catch (Exception e) {
			log.error("Database initialization failed", e);
			throw new RuntimeException("Database bootstrap error", e);
		}
	}

	private Map<String, TrainingTypeDAO> persistTrainingTypes(InitialBootstrapData data) {
		log.info("Step 1/6: Persisting training types...");
		Map<String, TrainingTypeDAO> typeMap = new HashMap<>();

		for (TrainingTypeDTO typeData : data.getTrainingTypes()) {
			TrainingTypeDAO dao = new TrainingTypeDAO();
			dao.setTrainingTypeName(TrainingTypeEnum.valueOf(typeData.getName()));

			entityManager.persist(dao);
			entityManager.flush();

			typeMap.put(typeData.getName(), dao);
			log.debug("Created TrainingType: {} (ID: {})", dao.getTrainingTypeName(), dao.getTrainingTypeId());
		}

		log.info("Created {} training types", typeMap.size());
		return typeMap;
	}

	private Map<String, UserDAO> persistUsers(InitialBootstrapData data) {
		log.info("Step 2/6: Persisting users...");
		Map<String, UserDAO> userMap = new HashMap<>();

		for (UserDTO userDTO : data.getUsers()) {
			UserDAO dao = new UserDAO();
			dao.setFirstName(userDTO.getFirstName());
			dao.setLastName(userDTO.getLastName());
			dao.setUsername(userDTO.getUsername());
			dao.setPassword(userDTO.getPassword());
			dao.setActive(userDTO.isActive());
			dao.setUserRole(UserRole.valueOf(userDTO.getRole()));

			entityManager.persist(dao);
			entityManager.flush();

			userMap.put(userDTO.getUsername(), dao);
			log.debug("Created User: {} (ID: {})", dao.getUsername(), dao.getUserId());
		}

		log.info("Created {} users", userMap.size());
		return userMap;
	}

	private Map<String, TrainerDAO> persistTrainers(InitialBootstrapData data, Map<String, UserDAO> users,
			Map<String, TrainingTypeDAO> trainingTypes) {
		log.info("Step 3/6: Persisting trainers...");
		Map<String, TrainerDAO> trainerMap = new HashMap<>();

		for (TrainerDTO trainerDTO : data.getTrainers()) {
			UserDAO userDao = users.get(trainerDTO.getUsername());
			if (userDao == null) {
				log.error("User not found for trainer: {}", trainerDTO.getUsername());
				continue;
			}

			TrainingTypeDAO typeDao = trainingTypes.get(trainerDTO.getSpecialization());
			if (typeDao == null) {
				log.error("TrainingType not found: {}", trainerDTO.getSpecialization());
				continue;
			}

			TrainerDAO dao = new TrainerDAO();
			dao.setUserDAO(userDao);
			dao.setTrainingTypeDAO(typeDao);

			entityManager.persist(dao);
			entityManager.flush();

			trainerMap.put(trainerDTO.getUsername(), dao);
			log.debug("Created Trainer: {} (ID: {}, Type: {})", userDao.getUsername(), dao.getTrainerId(),
					typeDao.getTrainingTypeName());
		}

		log.info("Created {} trainers", trainerMap.size());
		return trainerMap;
	}

	private Map<String, TraineeDAO> persistTrainees(InitialBootstrapData data, Map<String, UserDAO> users) {
		log.info("Step 4/6: Persisting trainees...");
		Map<String, TraineeDAO> traineeMap = new HashMap<>();

		for (TraineeDTO traineeDTO : data.getTrainees()) {
			UserDAO userDao = users.get(traineeDTO.getUsername());
			if (userDao == null) {
				log.error("User not found for trainee: {}", traineeDTO.getUsername());
				continue;
			}

			TraineeDAO dao = new TraineeDAO();
			dao.setUserDAO(userDao);
			dao.setDob(traineeDTO.getDateOfBirth());
			dao.setAddress(traineeDTO.getAddress());

			entityManager.persist(dao);
			entityManager.flush();

			traineeMap.put(traineeDTO.getUsername(), dao);
			log.debug("Created Trainee: {} (ID: {})", userDao.getUsername(), dao.getTraineeId());
		}

		log.info("Created {} trainees", traineeMap.size());
		return traineeMap;
	}

	private void createTraineeTrainerRelationships(InitialBootstrapData data, Map<String, TraineeDAO> trainees,
			Map<String, TrainerDAO> trainers) {
		log.info("Step 5/6: Creating trainee-trainer relationships...");
		int relationshipCount = 0;

		for (TraineeDTO traineeDTO : data.getTrainees()) {
			TraineeDAO traineeDao = trainees.get(traineeDTO.getUsername());
			if (traineeDao == null)
				continue;

			for (String trainerUsername : traineeDTO.getTrainerUsernames()) {
				TrainerDAO trainerDao = trainers.get(trainerUsername);
				if (trainerDao != null) {
					traineeDao.getTrainerDAOS().add(trainerDao);
					trainerDao.getTraineeDAOS().add(traineeDao);
					relationshipCount++;
					log.debug("Linked: {} ↔ {}", traineeDTO.getUsername(), trainerUsername);
				}
				else {
					log.warn("Trainer not found: {}", trainerUsername);
				}
			}
		}

		entityManager.flush();
		log.info("Created {} trainee-trainer relationships", relationshipCount);
	}

	private void persistTrainings(InitialBootstrapData data, Map<String, TraineeDAO> trainees,
			Map<String, TrainerDAO> trainers, Map<String, TrainingTypeDAO> trainingTypes) {
		log.info("Step 6/6: Persisting trainings...");
		int trainingCount = 0;

		for (TrainingDTO trainingDTO : data.getTrainings()) {
			TraineeDAO traineeDao = trainees.get(trainingDTO.getTraineeUsername());
			TrainerDAO trainerDao = trainers.get(trainingDTO.getTrainerUsername());
			TrainingTypeDAO typeDao = trainingTypes.get(trainingDTO.getTrainingType());

			if (traineeDao == null) {
				log.warn("Trainee not found: {}", trainingDTO.getTraineeUsername());
				continue;
			}
			if (trainerDao == null) {
				log.warn("Trainer not found: {}", trainingDTO.getTrainerUsername());
				continue;
			}
			if (typeDao == null) {
				log.warn("TrainingType not found: {}", trainingDTO.getTrainingType());
				continue;
			}

			TrainingDAO dao = new TrainingDAO();
			dao.setTrainingName(trainingDTO.getName());
			dao.setTrainingDate(trainingDTO.getDate());
			dao.setTrainingDurationMin(trainingDTO.getDurationMinutes());
			dao.setTraineeDAO(traineeDao);
			dao.setTrainerDAO(trainerDao);
			dao.setTrainingTypeDAO(typeDao);

			entityManager.persist(dao);
			trainingCount++;
			log.debug("Created Training: {} ({} with {})", trainingDTO.getName(), trainingDTO.getTraineeUsername(),
					trainingDTO.getTrainerUsername());
		}

		entityManager.flush();
		log.info("Created {} trainings", trainingCount);
	}

}