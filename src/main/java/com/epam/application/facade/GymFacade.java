package com.epam.application.facade;

import com.epam.application.service.TraineeService;
import com.epam.application.service.TrainerService;
import com.epam.application.service.TrainingService;
import com.epam.application.service.TrainingTypeService;
import com.epam.domain.model.Trainee;
import com.epam.domain.model.Trainer;
import com.epam.domain.model.Training;
import com.epam.domain.model.TrainingType;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GymFacade {

	private final TraineeService traineeService;

	private final TrainerService trainerService;

	private final TrainingService trainingService;

	private final TrainingTypeService trainingTypeService;

	@Autowired
	public GymFacade(TraineeService traineeService, TrainerService trainerService, TrainingService trainingService,
			TrainingTypeService trainingTypeService) {
		this.traineeService = traineeService;
		this.trainerService = trainerService;
		this.trainingService = trainingService;
		this.trainingTypeService = trainingTypeService;
	}

	// --- Trainee operations ---
	public void createTrainee(Trainee trainee) {
		traineeService.create(trainee);
	}

	public void updateTrainee(Trainee trainee) {
		traineeService.update(trainee);
	}

	public void deleteTrainee(long id) {
		traineeService.delete(id);
	}

	public Optional<Trainee> getTrainee(long id) {
		return traineeService.getById(id);
	}

	// --- Trainer operations ---
	public void createTrainer(Trainer trainer) {
		trainerService.create(trainer);
	}

	public void updateTrainer(Trainer trainer) {
		trainerService.update(trainer);
	}

	public void deleteTrainer(long id) {
		trainerService.delete(id);
	}

	public Optional<Trainer> getTrainer(long id) {
		return trainerService.getById(id);
	}

	// --- Training operations ---
	public void createTraining(Training training) {
		trainingService.create(training);
	}

	public Optional<Training> getTraining(long id) {
		return trainingService.getById(id);
	}

	public void updateTraining(Training training) {
		trainingService.update(training);
	}

	public void deleteTraining(long id) {
		trainingService.delete(id);
	}

	// --- TrainingType operations ---
	public void createTrainingType(TrainingType trainingType) {
		trainingTypeService.create(trainingType);
	}

	public Optional<TrainingType> getTrainingType(long id) {
		return trainingTypeService.getById(id);
	}

	public void updateTrainingType(TrainingType trainingType) {
		trainingTypeService.update(trainingType);
	}

	public void deleteTrainingType(long id) {
		trainingTypeService.delete(id);
	}

}
