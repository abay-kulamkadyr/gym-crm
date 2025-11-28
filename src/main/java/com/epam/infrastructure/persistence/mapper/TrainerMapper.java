package com.epam.infrastructure.persistence.mapper;

import com.epam.domain.model.UserRole;
import com.epam.domain.model.Trainer;
import com.epam.domain.model.TrainingType;
import com.epam.infrastructure.persistence.dao.TrainerDAO;
import com.epam.infrastructure.persistence.dao.UserDAO;
import com.epam.infrastructure.persistence.exception.MappingException;
import org.springframework.lang.NonNull;

public final class TrainerMapper {

	private TrainerMapper() {
		throw new UnsupportedOperationException("Utility class");
	}

	public static TrainerDAO toEntity(@NonNull Trainer trainer) {
		UserDAO userDAO = new UserDAO();
		userDAO.setUserId(trainer.getUserId());
		userDAO.setFirstName(trainer.getFirstName());
		userDAO.setLastName(trainer.getLastName());
		userDAO.setUsername(trainer.getUsername());
		userDAO.setPassword(trainer.getPassword());
		userDAO.setUserRole(UserRole.TRAINER);
		userDAO.setActive(trainer.getActive());

		TrainerDAO trainerDAO = new TrainerDAO();
		trainerDAO.setTrainerId(trainer.getTrainerId());
		trainerDAO.setUserDAO(userDAO);
		trainerDAO.setTrainingTypeDAO(TrainingTypeMapper.toEntity(trainer.getSpecialization()));

		userDAO.setTrainerDAO(trainerDAO);

		return trainerDAO;
	}

	public static Trainer toDomain(@NonNull TrainerDAO trainerDAO) {
		UserDAO userDAO = trainerDAO.getUserDAO();
		if (userDAO == null) {
			throw new MappingException("Cannot map TrainerDAO to Trainer: UserDAO is null");
		}

		if (trainerDAO.getTrainingTypeDAO() == null) {
			throw new MappingException("Cannot map TrainerDAO to Trainer: TrainingTypeDAO is null");
		}

		TrainingType specialization = TrainingTypeMapper.toDomain(trainerDAO.getTrainingTypeDAO());

		Trainer trainer = new Trainer(userDAO.getFirstName(), userDAO.getLastName(), userDAO.getActive(),
				specialization);

		trainer.setTrainerId(trainerDAO.getTrainerId());
		trainer.setUserId(userDAO.getUserId());
		trainer.setUsername(userDAO.getUsername());
		trainer.setPassword(userDAO.getPassword());

		return trainer;
	}

}