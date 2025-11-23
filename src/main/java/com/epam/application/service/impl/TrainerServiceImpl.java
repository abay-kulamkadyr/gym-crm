package com.epam.application.service.impl;

import com.epam.application.Credentials;
import com.epam.application.exception.AuthenticationException;
import com.epam.application.exception.ValidationException;
import com.epam.application.request.CreateTrainerProfileRequest;
import com.epam.application.request.UpdateTrainerProfileRequest;
import com.epam.application.service.AuthenticationService;
import com.epam.application.service.TrainerService;
import com.epam.application.util.CredentialsUtil;
import com.epam.domain.model.Trainer;
import com.epam.domain.model.TrainingType;
import com.epam.domain.model.TrainingTypeEnum;
import com.epam.domain.repository.TrainerRepository;
import com.epam.domain.repository.TrainingTypeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@Slf4j
public class TrainerServiceImpl implements TrainerService {

	private TrainerRepository trainerRepository;

	private TrainingTypeRepository trainingTypeRepository;

	private AuthenticationService authenticationService;

	@Autowired
	void setTrainerRepository(TrainerRepository trainerRepository) {
		this.trainerRepository = trainerRepository;
	}

	@Autowired
	void setTrainingTypeRepository(TrainingTypeRepository trainingTypeRepository) {
		this.trainingTypeRepository = trainingTypeRepository;
	}

	@Autowired
	void setAuthenticationService(AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	@Override
	public Trainer createProfile(CreateTrainerProfileRequest request) {
		TrainingType specialization = findTrainingTypeOrThrow(request.specialization());

		Trainer trainer = new Trainer(request.firstName(), request.lastName(), request.active(), specialization);

		String username = CredentialsUtil.generateUniqueUsername(trainer.getFirstName(), trainer.getLastName(),
				trainerRepository::findLatestUsername);
		String password = CredentialsUtil.generateRandomPassword(10);

		trainer.setUsername(username);
		trainer.setPassword(password);

		return trainerRepository.save(trainer);
	}

	@Override
	public Trainer updateProfile(UpdateTrainerProfileRequest request) {
		authenticateOrThrow(request.credentials());
		Trainer trainer = findTrainerByUsernameOrThrow(request.credentials().username());

		request.firstName().ifPresent(newFirstName -> {
			CredentialsUtil.validateName(newFirstName, "First name");
			trainer.setFirstName(newFirstName);
		});

		request.lastName().ifPresent(newLastName -> {
			CredentialsUtil.validateName(newLastName, "Last name");
			trainer.setLastName(newLastName);
		});

		request.username().ifPresent(newUsername -> {
			validateUsernameChange(newUsername, trainer.getUsername());
			trainer.setUsername(newUsername);
		});

		request.password().ifPresent(newPassword -> {
			validateNewPassword(newPassword);
			trainer.setPassword(newPassword);
		});

		request.active().ifPresent(trainer::setActive);

		request.specialization().ifPresent(newSpecialization -> {
			TrainingType trainingType = trainingTypeRepository.findByTrainingTypeName(newSpecialization)
				.orElseThrow(() -> {
					log.error("TrainingType not found: {}", newSpecialization);
					return new EntityNotFoundException(
							String.format("TrainingType with name '%s' not found", newSpecialization));
				});

			trainer.setSpecialization(trainingType);
		});

		return trainerRepository.save(trainer);
	}

	@Override
	public void updatePassword(Credentials credentials, String newPassword) {
		validateNewPassword(newPassword);

		authenticateOrThrow(credentials);
		Trainer trainer = findTrainerByUsernameOrThrow(credentials.username());

		validateNewPassword(newPassword);

		trainer.setPassword(newPassword);
		trainerRepository.save(trainer);
	}

	@Override
	public void toggleActiveStatus(Credentials credentials) {
		authenticateOrThrow(credentials);
		Trainer trainer = findTrainerByUsernameOrThrow(credentials.username());

		boolean oldStatus = trainer.getActive();
		boolean newStatus = !oldStatus;
		trainer.setActive(newStatus);

		trainerRepository.save(trainer);
	}

	@Override
	public void deleteProfile(Credentials credentials) {
		authenticateOrThrow(credentials);
		trainerRepository.deleteByUsername(credentials.username());
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Trainer> findProfileByUsername(Credentials credentials) {
		authenticateOrThrow(credentials);
		return trainerRepository.findByUsername(credentials.username());
	}

	private void authenticateOrThrow(Credentials credentials) {
		if (!authenticationService.authenticateTrainer(credentials)) {
			log.warn("Authentication failed for trainer: {}", credentials.username());
			throw new AuthenticationException(
					String.format("Invalid credentials for trainer: %s", credentials.username()));
		}
	}

	private Trainer findTrainerByUsernameOrThrow(String username) {
		return trainerRepository.findByUsername(username).orElseThrow(() -> {
			log.error("Trainer not found with username: {}", username);
			return new EntityNotFoundException(String.format("Trainer not found with username: %s", username));
		});
	}

	private TrainingType findTrainingTypeOrThrow(TrainingTypeEnum trainingType) {
		return trainingTypeRepository.findByTrainingTypeName(trainingType).orElseThrow(() -> {
			log.error("TrainingType not found with type: {}", trainingType);
			return new EntityNotFoundException(String.format("TrainingType not found with type: %s", trainingType));
		});
	}

	private void validateNewPassword(String password) {
		if (password == null) {
			throw new ValidationException("New password cannot be null");
		}
		CredentialsUtil.validatePassword(password);
	}

	private void validateUsernameChange(String newUsername, String currentUsername) {
		CredentialsUtil.validateUsername(newUsername);

		if (newUsername.equals(currentUsername)) {
			log.warn("New username is the same as current username: {}", newUsername);
			throw new ValidationException("New username must be different from current username");
		}

		if (trainerRepository.findByUsername(newUsername).isPresent()) {
			log.error("Username already exists: {}", newUsername);
			throw new ValidationException(String.format("Username already exists: %s", newUsername));
		}
	}

}