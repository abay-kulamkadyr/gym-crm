package com.epam.application.service.impl;

import com.epam.application.service.TraineeService;
import com.epam.domain.model.Trainee;
import com.epam.domain.repository.TraineeRepository;
import com.epam.application.util.CredentialsGenerator;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TraineeServiceImpl implements TraineeService {

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

		if (trainee.getUserId() != null) {
			throw new IllegalArgumentException("New trainee must not have an ID — it will be generated automatically");
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

		if (trainee.getUserId() == null) {
			throw new IllegalArgumentException("Cannot update trainee without an ID");
		}

		if (traineeRepository.findById(trainee.getUserId()).isEmpty()) {
			throw new IllegalArgumentException("Trainee with id " + trainee.getUserId() + " does not exist");
		}

		traineeRepository.save(trainee);
		log.info("Updated trainee: {}", trainee.getUserId());
	}

	@Override
	public void delete(Long id) {
		traineeRepository.delete(id);
		log.info("Deleted trainee: {}", id);
	}

	@Override
	public Optional<Trainee> getById(Long id) {
		return traineeRepository.findById(id);
	}

}
