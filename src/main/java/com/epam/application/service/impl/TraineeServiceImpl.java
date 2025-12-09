package com.epam.application.service.impl;

import com.epam.application.event.TraineeRegisteredEvent;
import com.epam.application.exception.ValidationException;
import com.epam.application.request.CreateTraineeProfileRequest;
import com.epam.application.request.UpdateTraineeProfileRequest;
import com.epam.application.service.TraineeService;
import com.epam.application.util.CredentialsUtil;
import com.epam.domain.model.Trainee;
import com.epam.domain.model.Trainer;
import com.epam.domain.port.TraineeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@Slf4j
public class TraineeServiceImpl implements TraineeService {

	private TraineeRepository traineeRepository;

	private ApplicationEventPublisher applicationEventPublisher;

	@Autowired
	void setTraineeRepository(TraineeRepository traineeRepository) {
		this.traineeRepository = traineeRepository;
	}

	@Autowired
	void setApplicationEventPublisher(ApplicationEventPublisher publisher) {
		this.applicationEventPublisher = publisher;
	}

	@Override
	public Trainee createProfile(CreateTraineeProfileRequest request) {
		CredentialsUtil.validateFullName(request.firstName(), request.lastName());

		Trainee trainee = new Trainee(request.firstName(), request.lastName(), request.active());

		String username = CredentialsUtil.generateUniqueUsername(trainee.getFirstName(), trainee.getLastName(),
				traineeRepository::findLatestUsername);
		String password = CredentialsUtil.generateRandomPassword(10);

		trainee.setUsername(username);
		trainee.setPassword(password);

		request.dob().ifPresent(trainee::setDob);

		request.address().ifPresent(trainee::setAddress);

		applicationEventPublisher.publishEvent(new TraineeRegisteredEvent(trainee.getTraineeId()));
		return traineeRepository.save(trainee);
	}

	@Override
	public Trainee updateProfile(UpdateTraineeProfileRequest request) {
		Trainee trainee = findTraineeByUsernameOrThrow(request.username());

		request.firstName().ifPresent(newFirstName -> {
			CredentialsUtil.validateName(newFirstName, "First name");
			trainee.setFirstName(newFirstName);
		});

		request.lastName().ifPresent(newLastName -> {
			CredentialsUtil.validateName(newLastName, "Last name");
			trainee.setLastName(newLastName);
		});

		request.password().ifPresent((password) -> {
			validateNewPassword(password);
			trainee.setPassword(password);
		});

		request.active().ifPresent(trainee::setActive);

		request.dob().ifPresent(trainee::setDob);

		request.address().ifPresent(trainee::setAddress);

		return traineeRepository.save(trainee);
	}

	@Override
	public void updatePassword(String username, String newPassword) {
		validateNewPassword(newPassword);

		Trainee trainee = findTraineeByUsernameOrThrow(username);

		trainee.setPassword(newPassword);
		traineeRepository.save(trainee);

	}

	@Override
	public void toggleActiveStatus(String username) {
		Trainee trainee = findTraineeByUsernameOrThrow(username);

		boolean oldStatus = trainee.getActive();
		boolean newStatus = !oldStatus;
		trainee.setActive(newStatus);

		traineeRepository.save(trainee);

	}

	@Override
	public void deleteProfile(String username) {
		findTraineeByUsernameOrThrow(username);
		traineeRepository.deleteByUsername(username);
	}

	@Override
	@Transactional(readOnly = true)
	public Trainee getProfileByUsername(String username) {
		return findTraineeByUsernameOrThrow(username);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Trainer> getUnassignedTrainers(String username) {
		findTraineeByUsernameOrThrow(username);
		return traineeRepository.getUnassignedTrainers(username);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Trainer> getTrainers(String username) {
		findTraineeByUsernameOrThrow(username);
		return traineeRepository.getTrainers(username);
	}

	@Override
	public void updateTrainersList(String username, List<String> trainerUsernames) {
		if (trainerUsernames.isEmpty()) {
			log.warn("Empty trainer usernames list provided for trainee: {} - will clear all trainers", username);
		}

		findTraineeByUsernameOrThrow(username);
		traineeRepository.updateTrainersList(username, trainerUsernames);
	}

	private Trainee findTraineeByUsernameOrThrow(String username) {
		return traineeRepository.findByUsername(username).orElseThrow(() -> {
			log.error("Trainee not found with username: {}", username);
			return new EntityNotFoundException(String.format("Trainee not found with username: %s", username));
		});
	}

	private void validateNewPassword(String password) {
		if (password == null) {
			throw new ValidationException("New password cannot be null");
		}
		CredentialsUtil.validatePassword(password);
	}

}