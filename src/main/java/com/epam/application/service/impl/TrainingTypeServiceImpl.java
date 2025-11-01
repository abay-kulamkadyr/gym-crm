package com.epam.application.service.impl;

import com.epam.application.service.TrainingTypeService;
import com.epam.domain.model.TrainingType;
import com.epam.domain.repository.TrainingTypeRepository;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TrainingTypeServiceImpl implements TrainingTypeService {

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

		if (trainingType.getTrainingTypeId() != null) {
			throw new IllegalArgumentException(
					"New training type must not have an ID — it will be generated automatically");
		}

		trainingTypeRepository.save(trainingType);
		log.info("Created training type: {}", trainingType.getTrainingNameType());
	}

	@Override
	public void update(TrainingType trainingType) {
		if (trainingType == null) {
			throw new IllegalArgumentException("TrainingType cannot be null");
		}

		if (trainingType.getTrainingTypeId() == null) {
			throw new IllegalArgumentException("Cannot update training type without an ID");
		}

		if (trainingTypeRepository.findById(trainingType.getTrainingTypeId()).isEmpty()) {
			throw new IllegalArgumentException(
					"TrainingType with id " + trainingType.getTrainingTypeId() + " does not exist");
		}

		trainingTypeRepository.save(trainingType);
		log.info("Updated training type: {}", trainingType.getTrainingTypeId());
	}

	@Override
	public void delete(Long id) {
		trainingTypeRepository.delete(id);
		log.info("Deleted training type: {}", id);
	}

	@Override
	public Optional<TrainingType> getById(Long id) {
		return trainingTypeRepository.findById(id);
	}

}
