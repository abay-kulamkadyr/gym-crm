package com.epam.application.service;

import com.epam.domain.model.Training;
import com.epam.domain.repository.TrainingRepository;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TrainingService implements CrudService<Training> {

	private TrainingRepository trainingRepository;

	@Autowired
	public void setTrainingRepository(TrainingRepository trainingRepository) {
		this.trainingRepository = trainingRepository;
	}

	@Override
	public void create(Training training) {
		if (training == null) {
			throw new IllegalArgumentException("training cannot be null");
		}
		if (trainingRepository.findById(training.getTrainingId()).isPresent()) {
			throw new IllegalArgumentException(
					"Training with the given id=" + training.getTrainingId() + " already exists");
		}

		trainingRepository.save(training);
		log.info("Created training: {}", training.getTrainingId());
	}

	@Override
	public void update(Training training) {
		if (training == null) {
			throw new IllegalArgumentException("training cannot be null");
		}

		Optional<Training> existing = trainingRepository.findById(training.getTrainingId());
		if (existing.isEmpty()) {
			throw new IllegalArgumentException("Training with id " + training.getTrainingId() + " does not exist");
		}

		trainingRepository.save(training);
		log.info("Updated training: {}", training.getTrainingId());
	}

	@Override
	public void delete(long id) {
		Optional<Training> existing = trainingRepository.findById(id);
		if (existing.isEmpty()) {
			log.warn("Attempted to delete non-existent training: {}", id);
			return;
		}

		trainingRepository.delete(id);
		log.info("Deleted training: {}", id);
	}

	@Override
	public Optional<Training> getById(long id) {
		return trainingRepository.findById(id);
	}

}
