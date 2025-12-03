package com.epam.application.service.impl;

import com.epam.application.Credentials;
import com.epam.application.event.TraineeRegisteredEvent;
import com.epam.application.exception.ValidationException;
import com.epam.application.request.CreateTraineeProfileRequest;
import com.epam.application.request.UpdateTraineeProfileRequest;
import com.epam.application.service.AuthenticationService;
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

	private AuthenticationService authenticationService;

	private ApplicationEventPublisher applicationEventPublisher;

	@Autowired
	void setTraineeRepository(TraineeRepository traineeRepository) {
		this.traineeRepository = traineeRepository;
	}

	@Autowired
	void setAuthenticationService(AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
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
		authenticationService.authenticate(request.credentials());
		Trainee trainee = findTraineeByUsernameOrThrow(request.credentials().username());

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
	public void updatePassword(Credentials credentials, String newPassword) {
		validateNewPassword(newPassword);

		authenticationService.authenticate(credentials);
		Trainee trainee = findTraineeByUsernameOrThrow(credentials.username());

		trainee.setPassword(newPassword);
		traineeRepository.save(trainee);

	}

	@Override
	public void toggleActiveStatus(Credentials credentials) {
		authenticationService.authenticate(credentials);
		Trainee trainee = findTraineeByUsernameOrThrow(credentials.username());

		boolean oldStatus = trainee.getActive();
		boolean newStatus = !oldStatus;
		trainee.setActive(newStatus);

		traineeRepository.save(trainee);

	}

	@Override
	public void deleteProfile(Credentials credentials) {
		authenticationService.authenticate(credentials);
		traineeRepository.deleteByUsername(credentials.username());
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Trainee> findProfileByUsername(Credentials credentials) {
		authenticationService.authenticate(credentials);
		return traineeRepository.findByUsername(credentials.username());
	}

	@Override
	@Transactional(readOnly = true)
	public List<Trainer> getUnassignedTrainers(Credentials credentials) {
		authenticationService.authenticate(credentials);
		findTraineeByUsernameOrThrow(credentials.username());
		return traineeRepository.getUnassignedTrainers(credentials.username());
	}

	@Override
	@Transactional(readOnly = true)
	public List<Trainer> getTrainers(Credentials credentials) {
		authenticationService.authenticate(credentials);
		return traineeRepository.getTrainers(credentials.username());
	}

	@Override
	public void updateTrainersList(Credentials credentials, List<String> trainerUsernames) {
		authenticationService.authenticate(credentials);

		if (trainerUsernames.isEmpty()) {
			log.warn("Empty trainer usernames list provided for trainee: {} - will clear all trainers",
					credentials.username());
		}

		traineeRepository.updateTrainersList(credentials.username(), trainerUsernames);
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