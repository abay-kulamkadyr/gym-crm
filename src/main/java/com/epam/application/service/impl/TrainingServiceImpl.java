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
import com.epam.domain.model.TrainingTypeEnum;
import com.epam.domain.port.TraineeRepository;
import com.epam.domain.port.TrainerRepository;
import com.epam.domain.port.TrainingRepository;
import com.epam.domain.port.TrainingTypeRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
public class TrainingServiceImpl implements TrainingService {

	private final TrainingRepository trainingRepository;

	private final TrainerRepository trainerRepository;

	private final TraineeRepository traineeRepository;

	private final TrainingTypeRepository trainingTypeRepository;

	@Autowired
	public TrainingServiceImpl(TrainingRepository trainingRepository, TrainerRepository trainerRepository,
			TraineeRepository traineeRepository, TrainingTypeRepository trainingTypeRepository) {
		this.trainingRepository = trainingRepository;
		this.trainerRepository = trainerRepository;
		this.traineeRepository = traineeRepository;
		this.trainingTypeRepository = trainingTypeRepository;
	}

	@Override
	public Training create(CreateTrainingRequest request) {

		Trainee trainee = findTrainee(request.traineeUsername());
		Trainer trainer = findTrainer(request.trainerUsername());

		TrainingType trainingType;
		if (request.trainingType().isEmpty()) {
			trainingType = trainer.getSpecialization();
		}
		else {
			trainingType = findTrainingType(request.trainingType().get());
		}

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
		return trainingRepository.getTraineeTrainings(credentials.username(), filter);
	}

	@Override
	public List<Training> getTrainerTrainings(Credentials credentials, TrainingFilter filter) {
		return trainingRepository.getTrainerTrainings(credentials.username(), filter);
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

	private TrainingType findTrainingType(TrainingTypeEnum name) {
		return trainingTypeRepository.findByTrainingTypeName(name).orElseThrow(() -> {
			log.warn("TrainingType not found with name: {}", name);
			return new EntityNotFoundException("TrainingType not found: " + name);
		});
	}

}