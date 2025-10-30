package com.epam.application.service;

import com.epam.domain.model.TrainingType;
import com.epam.domain.repository.TrainingTypeRepository;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TrainingTypeService implements CrudService<TrainingType> {

	private TrainingTypeRepository trainingTypeRepository;

	@Autowired
	public void setTrainingTypeRepository(TrainingTypeRepository trainingTypeRepository) {
		this.trainingTypeRepository = trainingTypeRepository;
	}

	@Override
	public void create(TrainingType trainingType) {
		if (trainingType == null) {
			throw new IllegalArgumentException("TrainingType cannot be null");
		}

		if (trainingTypeRepository.findById(trainingType.getTrainingTypeId()).isPresent()) {
			throw new IllegalArgumentException(
					"TrainingType with the given id=" + trainingType.getTrainingTypeId() + " already exists");
		}

		trainingTypeRepository.save(trainingType);
		log.info("Created training type: {}", trainingType.getTrainingNameType());
	}

	@Override
	public void update(TrainingType trainingType) {
		if (trainingType == null) {
			throw new IllegalArgumentException("TrainingType cannot be null");
		}

		Optional<TrainingType> existing = trainingTypeRepository.findById(trainingType.getTrainingTypeId());
		if (existing.isEmpty()) {
			throw new IllegalArgumentException(
					"TrainingType with id " + trainingType.getTrainingTypeId() + " does not exist");
		}

		trainingTypeRepository.save(trainingType);
		log.info("Updated training type: {}", trainingType.getTrainingTypeId());
	}

	@Override
	public void delete(long id) {
		Optional<TrainingType> existing = trainingTypeRepository.findById(id);
		if (existing.isEmpty()) {
			log.warn("Attempted to delete non-existent training type: {}", id);
			return;
		}

		trainingTypeRepository.delete(id);
		log.info("Deleted training type: {}", id);
	}

	@Override
	public Optional<TrainingType> getById(long id) {
		return trainingTypeRepository.findById(id);
	}

}
