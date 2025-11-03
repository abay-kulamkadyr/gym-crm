package com.epam.application.service.impl;

import com.epam.application.service.TrainingService;
import com.epam.domain.model.Training;
import com.epam.domain.repository.TrainingRepository;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TrainingServiceImpl implements TrainingService {

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
		if (training.getTrainingId() != null) {
			throw new IllegalArgumentException("New training must not have an ID — it will be generated automatically");
		}

		trainingRepository.save(training);
		log.info("Created training: {}", training.getTrainingId());
	}

	@Override
	public void update(Training training) {
		if (training == null) {
			throw new IllegalArgumentException("training cannot be null");
		}

		if (training.getTrainingId() == null) {
			throw new IllegalArgumentException("Cannot update training without an ID");
		}

		if (trainingRepository.findById(training.getTrainingId()).isEmpty()) {
			throw new IllegalArgumentException("Training with id " + training.getTrainingId() + " does not exist");
		}

		trainingRepository.save(training);
		log.info("Updated training: {}", training.getTrainingId());
	}

	@Override
	public void delete(Long id) {
		trainingRepository.delete(id);
		log.info("Deleted training: {}", id);
	}

	@Override
	public Optional<Training> getById(Long id) {
		return trainingRepository.findById(id);
	}

}
