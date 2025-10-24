package com.epam.application.service;

import com.epam.domain.repository.TrainingRepository;
import lombok.extern.slf4j.Slf4j;
import com.epam.domain.model.Training;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@Slf4j
public class TrainingService implements CrudService<Training> {

	private TrainingRepository trainingRepository;

	@Autowired
	public void setTrainingDao(TrainingRepository trainingRepository) {
		this.trainingRepository = trainingRepository;
	}

	@Override
	public void create(Training training) {
		if (training == null) {
			throw new IllegalArgumentException("training cannot be null");
		}
		if (trainingRepository.findById(training.getTrainingId()) != null) {
			throw new IllegalArgumentException(
					"Training with the given id=" + training.getTrainingId() + " already exists");
		}

		validateTraining(training);

		trainingRepository.save(training);
		log.info("Created training: {}", training.getTrainingId());
	}

	@Override
	public void update(Training training) {
		if (training == null) {
			throw new IllegalArgumentException("training cannot be null");
		}

		validateTraining(training);

		Training existing = trainingRepository.findById(training.getTrainingId());
		if (existing == null) {
			throw new IllegalArgumentException("Training with id " + training.getTrainingId() + " does not exist");
		}

		trainingRepository.save(training);
		log.info("Updated training: {}", training.getTrainingId());
	}

	@Override
	public void delete(long id) {
		Training existing = trainingRepository.findById(id);
		if (existing == null) {
			log.warn("Attempted to delete non-existent training: {}", id);
			return;
		}

		trainingRepository.delete(id);
		log.info("Deleted training: {}", id);
	}

	@Override
	public Training getById(long id) {
		return trainingRepository.findById(id);
	}

	@Override
	public Collection<Training> getAll() {
		return trainingRepository.findAll();
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
