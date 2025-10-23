package com.epam.dao;

import java.util.Collection;
import java.util.Map;
import com.epam.domain.Training;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

@Repository
public class TrainingDao implements CrudRepository<Training> {

	private Map<Long, Training> storage;

	@Autowired
	public void setStorage(Map<Long, Training> storage) {
		this.storage = storage;
	}

	@Override
	public void save(@NonNull Training training) {
		storage.put(training.getTrainingId(), training);
	}

	@Override
	public Training findById(long id) {
		return storage.get(id);
	}

	@Override
	public Collection<Training> findAll() {
		return storage.values();
	}

	@Override
	public void delete(long id) {
		storage.remove(id);
	}

}
