package com.epam.service;

import java.util.Collection;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import com.epam.dao.TrainingTypeDao;
import com.epam.domain.TrainingType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TrainingTypeService implements CrudService<TrainingType> {

	private TrainingTypeDao trainingTypeDao;

	@Autowired
	public void setTrainingTypeDao(TrainingTypeDao dao) {
		this.trainingTypeDao = dao;
	}

	@Override
	public void create(TrainingType trainingType) {
		Objects.requireNonNull(trainingType, "TrainingType cannot be null");
		if (trainingTypeDao.findById(trainingType.getId()) != null) {
			throw new IllegalArgumentException(
					"TrainingType with the given id=" + trainingType.getId() + " already exists");
		}

		validateTrainingType(trainingType);

		trainingTypeDao.save(trainingType);
		log.info("Created training type: {}", trainingType.getTrainingNameType());
	}

	@Override
	public void update(TrainingType trainingType) {
		Objects.requireNonNull(trainingType, "TrainingType cannot be null");
		validateTrainingType(trainingType);

		TrainingType existing = trainingTypeDao.findById(trainingType.getId());
		if (existing == null) {
			throw new IllegalArgumentException("TrainingType with id " + trainingType.getId() + " does not exist");
		}

		trainingTypeDao.save(trainingType);
		log.info("Updated training type: {}", trainingType.getId());
	}

	@Override
	public void delete(long id) {
		TrainingType existing = trainingTypeDao.findById(id);
		if (existing == null) {
			log.warn("Attempted to delete non-existent training type: {}", id);
			return;
		}

		trainingTypeDao.delete(id);
		log.info("Deleted training type: {}", id);
	}

	@Override
	public TrainingType getById(long id) {
		return trainingTypeDao.findById(id);
	}

	@Override
	public Collection<TrainingType> getAll() {
		return trainingTypeDao.findAll();
	}

	private void validateTrainingType(TrainingType trainingType) {
		if (trainingType.getTrainingNameType() == null || trainingType.getTrainingNameType().isBlank()) {
			throw new IllegalArgumentException("Training type name cannot be null or empty");
		}
		if (trainingType.getTrainerId() <= 0) {
			throw new IllegalArgumentException("TrainingType must have a valid trainer ID");
		}
		if (trainingType.getTrainingId() <= 0) {
			throw new IllegalArgumentException("TrainingType must have a valid training ID");
		}
	}

}
