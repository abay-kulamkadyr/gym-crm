package com.epam.application.facade;

import com.epam.application.Credentials;
import com.epam.application.request.CreateTraineeProfileRequest;
import com.epam.application.request.CreateTrainerProfileRequest;
import com.epam.application.request.CreateTrainingRequest;
import com.epam.application.request.UpdateTraineeProfileRequest;
import com.epam.application.request.UpdateTrainerProfileRequest;
import com.epam.domain.TrainingFilter;
import com.epam.application.service.TraineeService;
import com.epam.application.service.TrainerService;
import com.epam.application.service.TrainingService;
import com.epam.domain.model.Trainee;
import com.epam.domain.model.Trainer;
import com.epam.domain.model.Training;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GymFacadeImpl implements GymFacade {

	private final TraineeService traineeService;

	private final TrainerService trainerService;

	private final TrainingService trainingService;

	@Autowired
	public GymFacadeImpl(TraineeService traineeService, TrainerService trainerService,
			TrainingService trainingService) {
		this.traineeService = traineeService;
		this.trainerService = trainerService;
		this.trainingService = trainingService;
	}

	@Override
	public Trainee createTraineeProfile(CreateTraineeProfileRequest request) {
		return traineeService.createProfile(request);
	}

	@Override
	public Trainee updateTraineeProfile(UpdateTraineeProfileRequest request) {
		return traineeService.updateProfile(request);
	}

	@Override
	public void updateTraineePassword(Credentials credentials, String newPassword) {
		traineeService.updatePassword(credentials, newPassword);
	}

	@Override
	public void toggleTraineeActiveStatus(Credentials credentials) {
		traineeService.toggleActiveStatus(credentials);
	}

	@Override
	public void deleteTraineeProfile(Credentials credentials) {
		traineeService.deleteProfile(credentials);
	}

	@Override
	public Optional<Trainee> findTraineeByUsername(Credentials credentials) {
		return traineeService.findProfileByUsername(credentials);
	}

	@Override
	public void updateTraineeTrainersList(Credentials credentials, List<String> usernames) {
		traineeService.updateTrainersList(credentials, usernames);
	}

	@Override
	public List<Trainer> getTraineeUnassignedTrainers(Credentials credentials) {
		return traineeService.getUnassignedTrainers(credentials);
	}

	@Override
	public List<Training> getTraineeTrainings(Credentials credentials, TrainingFilter filter) {
		return trainingService.getTraineeTrainings(credentials, filter);
	}

	@Override
	public Trainer createTrainerProfile(CreateTrainerProfileRequest request) {
		return trainerService.createProfile(request);
	}

	@Override
	public Trainer updateTrainerProfile(UpdateTrainerProfileRequest request) {
		return trainerService.updateProfile(request);
	}

	@Override
	public void updateTrainerPassword(Credentials credentials, String newPassword) {
		trainerService.updatePassword(credentials, newPassword);
	}

	@Override
	public void toggleTrainerActiveStatus(Credentials credentials) {
		trainerService.toggleActiveStatus(credentials);
	}

	@Override
	public void deleteTrainerProfile(Credentials credentials) {
		trainerService.deleteProfile(credentials);
	}

	@Override
	public Optional<Trainer> findTrainerByUsername(Credentials credentials) {
		return trainerService.findProfileByUsername(credentials);
	}

	@Override
	public List<Training> getTrainerTrainings(Credentials credentials, TrainingFilter filter) {
		return trainingService.getTrainerTrainings(credentials, filter);
	}

	@Override
	public void createTraining(CreateTrainingRequest request) {
		trainingService.create(request);
	}

}
