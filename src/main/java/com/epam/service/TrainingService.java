package com.epam.service;

import lombok.extern.slf4j.Slf4j;
import com.epam.dao.TrainingDao;
import com.epam.domain.Training;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Objects;

@Service
@Slf4j
public class TrainingService implements CrudService<Training> {

	private TrainingDao trainingDao;

	@Autowired
	public void setTrainingDao(TrainingDao trainingDao) {
		this.trainingDao = trainingDao;
	}

	@Override
	public void create(Training training) {
		Objects.requireNonNull(training, "Training cannot be null");
		if (trainingDao.findById(training.getTrainingId()) != null) {
			throw new IllegalArgumentException(
					"Training with the given id=" + training.getTrainingId() + " already exists");
		}

		validateTraining(training);

		trainingDao.save(training);
		log.info("Created training: {}", training.getTrainingId());
	}

	@Override
	public void update(Training training) {
		Objects.requireNonNull(training, "Training cannot be null");
		validateTraining(training);

		Training existing = trainingDao.findById(training.getTrainingId());
		if (existing == null) {
			throw new IllegalArgumentException("Training with id " + training.getTrainingId() + " does not exist");
		}

		trainingDao.save(training);
		log.info("Updated training: {}", training.getTrainingId());
	}

	@Override
	public void delete(long id) {
		Training existing = trainingDao.findById(id);
		if (existing == null) {
			log.warn("Attempted to delete non-existent training: {}", id);
			return;
		}

		trainingDao.delete(id);
		log.info("Deleted training: {}", id);
	}

	@Override
	public Training getById(long id) {
		return trainingDao.findById(id);
	}

	@Override
	public Collection<Training> getAll() {
		return trainingDao.findAll();
	}

	private void validateTraining(Training training) {
		if (training.getTrainerId() <= 0) {
			throw new IllegalArgumentException("Training must have a valid trainer ID");
		}
		if (training.getTraineeId() <= 0) {
			throw new IllegalArgumentException("Training must have a valid trainee ID");
		}
		if (training.getTrainingDate() == null) {
			throw new IllegalArgumentException("Training date cannot be null");
		}
		if (training.getTrainingDuration() == null || training.getTrainingDuration().isZero()
				|| training.getTrainingDuration().isNegative()) {
			throw new IllegalArgumentException("Training duration must be positive");
		}
	}

}
