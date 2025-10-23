package com.epam.facade;

import com.epam.domain.Trainee;
import com.epam.domain.Trainer;
import com.epam.domain.Training;
import com.epam.domain.TrainingType;
import com.epam.service.TraineeService;
import com.epam.service.TrainerService;
import com.epam.service.TrainingService;
import com.epam.service.TrainingTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;

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

	public Trainee getTrainee(long id) {
		return traineeService.getById(id);
	}

	public Collection<Trainee> getAllTrainees() {
		return traineeService.getAll();
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

	public Trainer getTrainer(long id) {
		return trainerService.getById(id);
	}

	public Collection<Trainer> getAllTrainers() {
		return trainerService.getAll();
	}

	// --- Training operations ---
	public void createTraining(Training training) {
		trainingService.create(training);
	}

	public Training getTraining(long id) {
		return trainingService.getById(id);
	}

	public void updateTraining(Training training) {
		trainingService.update(training);
	}

	public void deleteTraining(long id) {
		trainingService.delete(id);
	}

	public Collection<Training> getAllTrainings() {
		return trainingService.getAll();
	}

	// --- TrainingType operations ---
	public void createTrainingType(TrainingType trainingType) {
		trainingTypeService.create(trainingType);
	}

	public TrainingType getTrainingType(long id) {
		return trainingTypeService.getById(id);
	}

	public void updateTrainingType(TrainingType trainingType) {
		trainingTypeService.update(trainingType);
	}

	public void deleteTrainingType(long id) {
		trainingTypeService.delete(id);
	}

	public Collection<TrainingType> getAllTrainingTypes() {
		return trainingTypeService.getAll();
	}

}
