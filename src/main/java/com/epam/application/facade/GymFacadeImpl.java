package com.epam.application.facade;

import com.epam.application.Credentials;
import com.epam.application.request.CreateTraineeProfileRequest;
import com.epam.application.request.CreateTrainerProfileRequest;
import com.epam.application.request.CreateTrainingRequest;
import com.epam.application.request.UpdateTraineeProfileRequest;
import com.epam.application.request.UpdateTrainerProfileRequest;
import com.epam.application.service.TraineeService;
import com.epam.application.service.TrainerService;
import com.epam.application.service.TrainingService;
import com.epam.domain.TrainingFilter;
import com.epam.domain.model.Trainee;
import com.epam.domain.model.Trainer;
import com.epam.domain.model.Training;
import com.epam.domain.model.TrainingType;
import com.epam.domain.port.TrainingTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
public class GymFacadeImpl implements GymFacade {

	private final TraineeService traineeService;

	private final TrainerService trainerService;

	private final TrainingService trainingService;

	private final TrainingTypeRepository trainingTypeRepository;

	@Autowired
	public GymFacadeImpl(TraineeService traineeService, TrainerService trainerService, TrainingService trainingService,
			TrainingTypeRepository trainingTypeRepository) {
		this.traineeService = traineeService;
		this.trainerService = trainerService;
		this.trainingService = trainingService;
		this.trainingTypeRepository = trainingTypeRepository;
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
	public Training createTraining(CreateTrainingRequest request) {
		return trainingService.create(request);
	}

	@Override
	public List<Trainer> getTraineeTrainers(Credentials credentials) {
		return traineeService.getTrainers(credentials);
	}

	@Override
	public List<Trainee> getTrainerTrainees(Credentials credentials) {
		return trainerService.getTrainees(credentials);
	}

	@Override
	@Transactional(readOnly = true)
	public List<TrainingType> getTrainingTypes() {
		return trainingTypeRepository.getTrainingTypes();
	}

}
