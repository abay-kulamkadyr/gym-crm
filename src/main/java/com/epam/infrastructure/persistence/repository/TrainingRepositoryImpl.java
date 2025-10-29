package com.epam.infrastructure.persistence.repository;

import com.epam.domain.repository.TrainingRepository;
import com.epam.infrastructure.persistence.dao.TrainingDao;
import com.epam.infrastructure.persistence.mapper.TrainingMapper;
import java.util.Collection;
import java.util.Map;
import com.epam.domain.model.Training;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

@Repository
public class TrainingRepositoryImpl implements TrainingRepository {

	private Map<Long, TrainingDao> storage;

	@Autowired
	public void setStorage(Map<Long, TrainingDao> storage) {
		this.storage = storage;
	}

	@Override
	public void save(@NonNull Training training) {
		TrainingDao entity = TrainingMapper.toEntity(training);
		storage.put(entity.getTrainingId(), entity);
	}

	@Override
	public Training findById(long id) {
		TrainingDao trainingDao = storage.get(id);
		return TrainingMapper.toDomain(trainingDao);
	}

	@Override
	public Collection<Training> findAll() {
		return storage.values().stream().map(TrainingMapper::toDomain).toList();
	}

	@Override
	public void delete(long id) {
		storage.remove(id);
	}

}
