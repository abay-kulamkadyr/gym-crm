package com.epam.service;

import lombok.extern.slf4j.Slf4j;
import com.epam.dao.TraineeDao;
import com.epam.domain.Trainee;
import com.epam.util.PasswordGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Objects;

@Service
@Slf4j
public class TraineeService implements CrudService<Trainee> {

	private TraineeDao traineeDao;

	@Autowired
	public void setTraineeDao(TraineeDao traineeDao) {
		this.traineeDao = traineeDao;
	}

	@Override
	public void create(Trainee trainee) {
		Objects.requireNonNull(trainee, "Trainee cannot be null");
		if (traineeDao.findById(trainee.getUserId()) != null) {
			throw new IllegalArgumentException("Trainee with the given id=" + trainee.getUserId() + " already exists");
		}
		validateTrainee(trainee);

		String username = generateUniqueUsername(trainee);
		trainee.setUsername(username);
		trainee.setPassword(PasswordGenerator.generate(10));
		traineeDao.save(trainee);
		log.info("Created trainee: {}", username);
	}

	@Override
	public void update(Trainee trainee) {
		Objects.requireNonNull(trainee, "Trainee cannot be null");
		validateTrainee(trainee);

		Trainee existing = traineeDao.findById(trainee.getUserId());
		if (existing == null) {
			throw new IllegalArgumentException("Trainee with id " + trainee.getUserId() + " does not exist");
		}

		traineeDao.save(trainee);
		log.info("Updated trainee: {}", trainee.getUserId());
	}

	@Override
	public void delete(long id) {
		Trainee existing = traineeDao.findById(id);
		if (existing == null) {
			log.warn("Attempted to delete non-existent trainee: {}", id);
			return;
		}

		traineeDao.delete(id);
		log.info("Deleted trainee: {}", id);
	}

	@Override
	public Trainee getById(long id) {
		return traineeDao.findById(id);
	}

	@Override
	public Collection<Trainee> getAll() {
		return traineeDao.findAll();
	}

	private String generateUniqueUsername(Trainee trainee) {
		String base = trainee.getFirstName() + "." + trainee.getLastName();
		long duplicates = traineeDao.findAll().stream().filter(t -> t.getUsername().startsWith(base)).count();
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
