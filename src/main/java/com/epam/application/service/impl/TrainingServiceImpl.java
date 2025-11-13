package com.epam.application.service.impl;

import com.epam.application.Credentials;
import com.epam.application.exception.AuthenticationException;
import com.epam.application.request.CreateTrainingRequest;
import com.epam.application.service.AuthenticationService;
import com.epam.application.service.TrainingService;
import com.epam.domain.TrainingFilter;
import com.epam.domain.model.Trainee;
import com.epam.domain.model.Trainer;
import com.epam.domain.model.Training;
import com.epam.domain.model.TrainingType;
import com.epam.domain.repository.TraineeRepository;
import com.epam.domain.repository.TrainerRepository;
import com.epam.domain.repository.TrainingRepository;
import com.epam.domain.repository.TrainingTypeRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
public class TrainingServiceImpl implements TrainingService {

	private final TrainingRepository trainingRepository;

	private final AuthenticationService authenticationService;

	private final TrainerRepository trainerRepository;

	private final TraineeRepository traineeRepository;

	private final TrainingTypeRepository trainingTypeRepository;

	@Autowired
	public TrainingServiceImpl(TrainingRepository trainingRepository, AuthenticationService authenticationService,
			TrainerRepository trainerRepository, TraineeRepository traineeRepository,
			TrainingTypeRepository trainingTypeRepository) {
		this.trainingRepository = trainingRepository;
		this.authenticationService = authenticationService;
		this.trainerRepository = trainerRepository;
		this.traineeRepository = traineeRepository;
		this.trainingTypeRepository = trainingTypeRepository;
	}

	@Override
	public Training create(CreateTrainingRequest request) {
		authenticateOrThrow(request.credentials());

		Trainee trainee = findTrainee(request.traineeUsername());
		Trainer trainer = findTrainer(request.trainerUsername());
		TrainingType trainingType = findTrainingType(request.trainingType());

		Training training = Training.builder()
			.trainingName(request.trainingName())
			.trainingDate(request.trainingDate())
			.trainingDurationMin(request.trainingDurationMin())
			.trainee(trainee)
			.trainer(trainer)
			.trainingType(trainingType)
			.build();

		return trainingRepository.save(training);
	}

	@Override
	public List<Training> getTraineeTrainings(Credentials credentials, TrainingFilter filter) {
		authenticateOrThrow(credentials);
		return trainingRepository.getTraineeTrainings(credentials.username(), filter);
	}

	@Override
	public List<Training> getTrainerTrainings(Credentials credentials, TrainingFilter filter) {
		authenticateOrThrow(credentials);
		return trainingRepository.getTrainerTrainings(credentials.username(), filter);
	}

	private void authenticateOrThrow(Credentials credentials) {
		boolean isAuthenticated = authenticationService.authenticateTrainee(credentials)
				|| authenticationService.authenticateTrainer(credentials);

		if (!isAuthenticated) {
			log.warn("Authentication failed for user: {}", credentials.username());
			throw new AuthenticationException(
					String.format("Invalid credentials for user: %s", credentials.username()));
		}

	}

	private Trainee findTrainee(String username) {
		return traineeRepository.findByUsername(username).orElseThrow(() -> {
			log.warn("Trainee not found with username: {}", username);
			return new EntityNotFoundException("Trainee not found: " + username);
		});
	}

	private Trainer findTrainer(String username) {
		return trainerRepository.findByUsername(username).orElseThrow(() -> {
			log.warn("Trainer not found with username: {}", username);
			return new EntityNotFoundException("Trainer not found: " + username);
		});
	}

	private TrainingType findTrainingType(String name) {
		return trainingTypeRepository.findByTrainingTypeName(name).orElseThrow(() -> {
			log.warn("TrainingType not found with name: {}", name);
			return new EntityNotFoundException("TrainingType not found: " + name);
		});
	}

}