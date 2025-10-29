package com.epam.infrastructure.persistence.repository;

import com.epam.domain.repository.TrainingTypeRepository;
import com.epam.infrastructure.persistence.dao.TrainingTypeDao;
import com.epam.infrastructure.persistence.mapper.TrainingTypeMapper;
import java.util.Collection;
import java.util.Map;
import com.epam.domain.model.TrainingType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

@Repository
public class TrainingTypeRepositoryImpl implements TrainingTypeRepository {

	private Map<Long, TrainingTypeDao> storage;

	@Autowired
	public void setStorage(Map<Long, TrainingTypeDao> storage) {
		this.storage = storage;
	}

	@Override
	public void save(@NonNull TrainingType trainingType) {
		TrainingTypeDao entity = TrainingTypeMapper.toEntity(trainingType);
		storage.put(entity.getTrainingTypeId(), entity);
	}

	@Override
	public TrainingType findById(long id) {
		TrainingTypeDao entity = storage.get(id);
		return TrainingTypeMapper.toDomain(entity);
	}

	@Override
	public Collection<TrainingType> findAll() {
		return storage.values().stream().map(TrainingTypeMapper::toDomain).toList();
	}

	@Override
	public void delete(long id) {
		storage.remove(id);
	}

}
