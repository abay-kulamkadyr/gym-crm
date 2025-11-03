package com.epam.infrastructure.persistence.repository;

import com.epam.domain.model.Training;
import com.epam.domain.repository.TrainingRepository;
import com.epam.infrastructure.persistence.dao.TrainingDao;
import com.epam.infrastructure.persistence.mapper.TrainingMapper;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

@Repository
public class TrainingRepositoryImpl implements TrainingRepository {

	private Map<Long, TrainingDao> storage;

	private final AtomicLong idGenerator = new AtomicLong(1);

	@Autowired
	public void setStorage(Map<Long, TrainingDao> storage) {
		this.storage = storage;
	}

	@Override
	public void save(@NonNull Training training) {
		if (training.getTrainingId() == null) {
			Long newId = idGenerator.getAndIncrement();
			training.setTrainingId(newId);
		}

		TrainingDao entity = TrainingMapper.toEntity(training);
		storage.put(entity.getTrainingId(), entity);
	}

	@Override
	public Optional<Training> findById(Long id) {
		TrainingDao trainingDao = storage.get(id);
		if (trainingDao == null) {
			return Optional.empty();
		}
		return Optional.of(TrainingMapper.toDomain(trainingDao));
	}

	@Override
	public void delete(Long id) {
		storage.remove(id);
	}

}
