package com.epam.application.service;

import com.epam.domain.repository.TraineeRepository;
import lombok.extern.slf4j.Slf4j;
import com.epam.domain.model.Trainee;
import com.epam.domain.util.PasswordGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Objects;

@Service
@Slf4j
public class TraineeService implements CrudService<Trainee> {

	private TraineeRepository traineeRepository;

	@Autowired
	public void setTraineeDao(TraineeRepository traineeRepository) {
		this.traineeRepository = traineeRepository;
	}

	@Override
	public void create(Trainee trainee) {
		Objects.requireNonNull(trainee, "Trainee cannot be null");
		if (traineeRepository.findById(trainee.getUserId()) != null) {
			throw new IllegalArgumentException("Trainee with the given id=" + trainee.getUserId() + " already exists");
		}
		validateTrainee(trainee);

		String username = generateUniqueUsername(trainee);
		trainee.setUsername(username);
		trainee.setPassword(PasswordGenerator.generate(10));
		traineeRepository.save(trainee);
		log.info("Created trainee: {}", username);
	}

	@Override
	public void update(Trainee trainee) {
		Objects.requireNonNull(trainee, "Trainee cannot be null");
		validateTrainee(trainee);

		Trainee existing = traineeRepository.findById(trainee.getUserId());
		if (existing == null) {
			throw new IllegalArgumentException("Trainee with id " + trainee.getUserId() + " does not exist");
		}

		traineeRepository.save(trainee);
		log.info("Updated trainee: {}", trainee.getUserId());
	}

	@Override
	public void delete(long id) {
		Trainee existing = traineeRepository.findById(id);
		if (existing == null) {
			log.warn("Attempted to delete non-existent trainee: {}", id);
			return;
		}

		traineeRepository.delete(id);
		log.info("Deleted trainee: {}", id);
	}

	@Override
	public Trainee getById(long id) {
		return traineeRepository.findById(id);
	}

	@Override
	public Collection<Trainee> getAll() {
		return traineeRepository.findAll();
	}

	private String generateUniqueUsername(Trainee trainee) {
		String base = trainee.getFirstName() + "." + trainee.getLastName();
		long duplicates = traineeRepository.findAll().stream().filter(t -> t.getUsername().startsWith(base)).count();
		return duplicates > 0 ? base + (duplicates + 1) : base;
	}

	private void validateTrainee(Trainee trainee) {
		if (trainee.getFirstName() == null || trainee.getFirstName().isBlank()) {
			throw new IllegalArgumentException("Trainee first name cannot be null or empty");
		}
		if (trainee.getLastName() == null || trainee.getLastName().isBlank()) {
			throw new IllegalArgumentException("Trainee last name cannot be null or empty");
		}
		if (trainee.getDob() == null) {
			throw new IllegalArgumentException("Trainee date of birth cannot be null");
		}
	}

}
