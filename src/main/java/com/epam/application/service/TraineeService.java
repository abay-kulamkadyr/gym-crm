package com.epam.application.service;

import com.epam.domain.model.Trainee;
import com.epam.domain.repository.TraineeRepository;
import com.epam.application.util.CredentialsGenerator;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TraineeService implements CrudService<Trainee> {

	private TraineeRepository traineeRepository;

	@Autowired
	public void setTraineeRepository(TraineeRepository traineeRepository) {
		this.traineeRepository = traineeRepository;
	}

	@Override
	public void create(Trainee trainee) {
		if (trainee == null) {
			throw new IllegalArgumentException("Trainee cannot be null");
		}
		if (traineeRepository.findById(trainee.getUserId()).isPresent()) {
			throw new IllegalArgumentException("Trainee with the given id=" + trainee.getUserId() + " already exists");
		}

		trainee.setUsername(CredentialsGenerator.generateUniqueUsername(trainee.getFirstName(), trainee.getLastName(),
				traineeRepository::findLatestUsername));
		trainee.setPassword(CredentialsGenerator.generateRandomPassword(10));
		traineeRepository.save(trainee);
		log.info("Created trainee: {}", trainee.getUsername());
	}

	@Override
	public void update(Trainee trainee) {
		if (trainee == null) {
			throw new IllegalArgumentException("Trainee cannot be null");
		}

		Optional<Trainee> existing = traineeRepository.findById(trainee.getUserId());
		if (existing.isEmpty()) {
			throw new IllegalArgumentException("Trainee with id " + trainee.getUserId() + " does not exist");
		}

		traineeRepository.save(trainee);
		log.info("Updated trainee: {}", trainee.getUserId());
	}

	@Override
	public void delete(long id) {
		Optional<Trainee> existing = traineeRepository.findById(id);
		if (existing.isEmpty()) {
			log.warn("Attempted to delete non-existent trainee: {}", id);
			return;
		}

		traineeRepository.delete(id);
		log.info("Deleted trainee: {}", id);
	}

	@Override
	public Optional<Trainee> getById(long id) {
		return traineeRepository.findById(id);
	}

}
