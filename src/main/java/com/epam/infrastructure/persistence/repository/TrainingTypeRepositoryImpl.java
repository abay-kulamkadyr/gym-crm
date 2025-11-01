package com.epam.infrastructure.persistence.repository;

import com.epam.domain.model.TrainingType;
import com.epam.domain.repository.TrainingTypeRepository;
import com.epam.infrastructure.persistence.dao.TrainingTypeDao;
import com.epam.infrastructure.persistence.mapper.TrainingTypeMapper;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

@Repository
public class TrainingTypeRepositoryImpl implements TrainingTypeRepository {

	private Map<Long, TrainingTypeDao> storage;

	private final AtomicLong idGenerator = new AtomicLong(1);

	@Autowired
	public void setStorage(Map<Long, TrainingTypeDao> storage) {
		this.storage = storage;
	}

	@Override
	public void save(@NonNull TrainingType trainingType) {
		if (trainingType.getTrainingTypeId() == null) {
			Long newId = idGenerator.getAndIncrement();
			trainingType.setTrainingTypeId(newId);
		}

		TrainingTypeDao entity = TrainingTypeMapper.toEntity(trainingType);
		storage.put(entity.getTrainingTypeId(), entity);
	}

	@Override
	public Optional<TrainingType> findById(Long id) {
		TrainingTypeDao entity = storage.get(id);
		if (entity == null) {
			return Optional.empty();
		}
		return Optional.of(TrainingTypeMapper.toDomain(entity));
	}

	@Override
	public void delete(Long id) {
		storage.remove(id);
	}

}
