package com.epam.application.service.impl;

import com.epam.application.Credentials;
import com.epam.application.service.AuthenticationService;
import com.epam.domain.model.Trainee;
import com.epam.domain.model.Trainer;
import com.epam.domain.repository.TraineeRepository;
import com.epam.domain.repository.TrainerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class AuthenticationServiceImpl implements AuthenticationService {

	private final TraineeRepository traineeRepository;

	private final TrainerRepository trainerRepository;

	@Autowired
	public AuthenticationServiceImpl(TraineeRepository traineeRepository, TrainerRepository trainerRepository) {
		this.traineeRepository = traineeRepository;
		this.trainerRepository = trainerRepository;
	}

	@Override
	public Boolean authenticateTrainee(Credentials credentials) {
		Optional<Trainee> foundTrainee = traineeRepository.findByUsername(credentials.username());

		return foundTrainee.filter(trainee -> credentials.password().equals(trainee.getPassword())).isPresent();
	}

	@Override
	public Boolean authenticateTrainer(Credentials credentials) {
		Optional<Trainer> foundTrainer = trainerRepository.findByUsername(credentials.username());

		return foundTrainer.filter(trainee -> credentials.password().equals(trainee.getPassword())).isPresent();
	}

}