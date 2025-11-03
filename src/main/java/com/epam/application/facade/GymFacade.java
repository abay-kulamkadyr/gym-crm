package com.epam.application.facade;

import com.epam.application.service.impl.TraineeServiceImpl;
import com.epam.application.service.impl.TrainerServiceImpl;
import com.epam.application.service.impl.TrainingServiceImpl;
import com.epam.application.service.impl.TrainingTypeServiceImpl;
import com.epam.domain.model.Trainee;
import com.epam.domain.model.Trainer;
import com.epam.domain.model.Training;
import com.epam.domain.model.TrainingType;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GymFacade {

	private final TraineeServiceImpl traineeServiceImpl;

	private final TrainerServiceImpl trainerServiceImpl;

	private final TrainingServiceImpl trainingServiceImpl;

	private final TrainingTypeServiceImpl trainingTypeServiceImpl;

	@Autowired
	public GymFacade(TraineeServiceImpl traineeServiceImpl, TrainerServiceImpl trainerServiceImpl,
			TrainingServiceImpl trainingServiceImpl, TrainingTypeServiceImpl trainingTypeServiceImpl) {
		this.traineeServiceImpl = traineeServiceImpl;
		this.trainerServiceImpl = trainerServiceImpl;
		this.trainingServiceImpl = trainingServiceImpl;
		this.trainingTypeServiceImpl = trainingTypeServiceImpl;
	}

	// --- Trainee operations ---
	public void createTrainee(Trainee trainee) {
		traineeServiceImpl.create(trainee);
	}

	public void updateTrainee(Trainee trainee) {
		traineeServiceImpl.update(trainee);
	}

	public void deleteTrainee(long id) {
		traineeServiceImpl.delete(id);
	}

	public Optional<Trainee> getTrainee(long id) {
		return traineeServiceImpl.getById(id);
	}

	// --- Trainer operations ---
	public void createTrainer(Trainer trainer) {
		trainerServiceImpl.create(trainer);
	}

	public void updateTrainer(Trainer trainer) {
		trainerServiceImpl.update(trainer);
	}

	public void deleteTrainer(long id) {
		trainerServiceImpl.delete(id);
	}

	public Optional<Trainer> getTrainer(long id) {
		return trainerServiceImpl.getById(id);
	}

	// --- Training operations ---
	public void createTraining(Training training) {
		trainingServiceImpl.create(training);
	}

	public Optional<Training> getTraining(long id) {
		return trainingServiceImpl.getById(id);
	}

	public void updateTraining(Training training) {
		trainingServiceImpl.update(training);
	}

	public void deleteTraining(long id) {
		trainingServiceImpl.delete(id);
	}

	// --- TrainingType operations ---
	public void createTrainingType(TrainingType trainingType) {
		trainingTypeServiceImpl.create(trainingType);
	}

	public Optional<TrainingType> getTrainingType(long id) {
		return trainingTypeServiceImpl.getById(id);
	}

	public void updateTrainingType(TrainingType trainingType) {
		trainingTypeServiceImpl.update(trainingType);
	}

	public void deleteTrainingType(long id) {
		trainingTypeServiceImpl.delete(id);
	}

}
