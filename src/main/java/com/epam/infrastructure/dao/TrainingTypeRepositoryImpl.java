package com.epam.infrastructure.dao;

import com.epam.domain.repository.TrainingTypeRepository;
import java.util.Collection;
import java.util.Map;
import com.epam.domain.model.TrainingType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

@Repository
public class TrainingTypeRepositoryImpl implements TrainingTypeRepository {

	private Map<Long, TrainingType> storage;

	@Autowired
	public void setStorage(Map<Long, TrainingType> storage) {
		this.storage = storage;
	}

	@Override
	public void save(@NonNull TrainingType trainingType) {
		storage.put(trainingType.getId(), trainingType);
	}

	@Override
	public TrainingType findById(long id) {
		return storage.get(id);
	}

	@Override
	public Collection<TrainingType> findAll() {
		return storage.values();
	}

	@Override
	public void delete(long id) {
		storage.remove(id);
	}

}
