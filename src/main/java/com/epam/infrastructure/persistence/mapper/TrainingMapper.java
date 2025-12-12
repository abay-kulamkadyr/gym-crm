package com.epam.infrastructure.persistence.mapper;

import com.epam.domain.model.Trainee;
import com.epam.domain.model.Trainer;
import com.epam.domain.model.Training;
import com.epam.domain.model.TrainingType;
import com.epam.infrastructure.persistence.dao.TraineeDAO;
import com.epam.infrastructure.persistence.dao.TrainerDAO;
import com.epam.infrastructure.persistence.dao.TrainingDAO;
import com.epam.infrastructure.persistence.dao.TrainingTypeDAO;
import com.epam.infrastructure.persistence.exception.MappingException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class TrainingMapper {

	@PersistenceContext
	private EntityManager entityManager;

	public TrainingDAO toEntity(@NonNull Training training) {
		validateTraining(training);

		// Get managed entity references
		TraineeDAO traineeDAO = getTraineeReference(training);
		TrainerDAO trainerDAO = getTrainerReference(training);
		TrainingTypeDAO trainingTypeDAO = getTrainingTypeReference(training);

		// Create and populate training entity
		TrainingDAO trainingDAO = new TrainingDAO();
		trainingDAO.setTrainingId(training.getTrainingId());
		trainingDAO.setTrainingName(training.getTrainingName());
		trainingDAO.setTrainingDate(training.getTrainingDate());
		trainingDAO.setTrainingDurationMin(training.getTrainingDurationMin());
		trainingDAO.setTrainingTypeDAO(trainingTypeDAO);

		// Manage bidirectional relationships
		traineeDAO.addTraining(trainingDAO);
		trainerDAO.addTraining(trainingDAO);
		traineeDAO.addTrainer(trainerDAO);
		trainerDAO.addTrainee(traineeDAO);
		trainingTypeDAO.addTraining(trainingDAO);

		return trainingDAO;
	}

	public Training toDomain(@NonNull TrainingDAO trainingDAO) {
		validateTrainingDAO(trainingDAO);

		Trainee trainee = TraineeMapper.toDomain(trainingDAO.getTraineeDAO());
		Trainer trainer = TrainerMapper.toDomain(trainingDAO.getTrainerDAO());
		TrainingType trainingType = TrainingTypeMapper.toDomain(trainingDAO.getTrainingTypeDAO());

		Training training = Training.builder()
			.trainingName(trainingDAO.getTrainingName())
			.trainingDate(trainingDAO.getTrainingDate())
			.trainingDurationMin(trainingDAO.getTrainingDurationMin())
			.trainee(trainee)
			.trainer(trainer)
			.trainingType(trainingType)
			.build();

		training.setTrainingId(trainingDAO.getTrainingId());

		return training;
	}

	private void validateTraining(Training training) {
		if (training.getTrainingName() == null || training.getTrainingName().isBlank()) {
			throw new MappingException("Training name cannot be null or blank");
		}
		if (training.getTrainee() == null || training.getTrainee().getTraineeId() == null) {
			throw new MappingException("Training must have a valid trainee with ID");
		}
		if (training.getTrainer() == null || training.getTrainer().getTrainerId() == null) {
			throw new MappingException("Training must have a valid trainer with ID");
		}
		if (training.getTrainingType() == null || training.getTrainingType().getTrainingTypeId() == null) {
			throw new MappingException("Training must have a valid training type with ID");
		}
	}

	private void validateTrainingDAO(TrainingDAO trainingDAO) {
		if (trainingDAO.getTraineeDAO() == null) {
			throw new MappingException("Cannot map TrainingDAO to Training: TraineeDAO is null");
		}
		if (trainingDAO.getTrainerDAO() == null) {
			throw new MappingException("Cannot map TrainingDAO to Training: TrainerDAO is null");
		}
		if (trainingDAO.getTrainingTypeDAO() == null) {
			throw new MappingException("Cannot map TrainingDAO to Training: TrainingTypeDAO is null");
		}
	}

	private TraineeDAO getTraineeReference(Training training) {
		try {
			return entityManager.getReference(TraineeDAO.class, training.getTrainee().getTraineeId());
		}
		catch (EntityNotFoundException e) {
			throw new EntityNotFoundException(
					String.format("Trainee with ID %d not found", training.getTrainee().getTraineeId()));
		}
	}

	private TrainerDAO getTrainerReference(Training training) {
		try {
			return entityManager.getReference(TrainerDAO.class, training.getTrainer().getTrainerId());
		}
		catch (EntityNotFoundException e) {
			throw new EntityNotFoundException(
					String.format("Trainer with ID %d not found", training.getTrainer().getTrainerId()));
		}
	}

	private TrainingTypeDAO getTrainingTypeReference(Training training) {
		try {
			return entityManager.getReference(TrainingTypeDAO.class, training.getTrainingType().getTrainingTypeId());
		}
		catch (EntityNotFoundException e) {
			throw new EntityNotFoundException(String.format("Training type with ID %d not found",
					training.getTrainingType().getTrainingTypeId()));
		}
	}

}
