package com.epam.infrastructure.dao;

import com.epam.domain.repository.TraineeRepository;
import java.util.Collection;
import java.util.Map;
import com.epam.domain.model.Trainee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

@Repository
public class TraineeRepositoryImpl implements TraineeRepository {

	private Map<Long, Trainee> storage;

	@Autowired
	public void setStorage(Map<Long, Trainee> storage) {
		this.storage = storage;
	}

	@Override
	public void save(@NonNull Trainee trainee) {
		storage.put(trainee.getUserId(), trainee);
	}

	@Override
	public Trainee findById(long id) {
		return storage.get(id);
	}

	@Override
	public Collection<Trainee> findAll() {
		return storage.values();
	}

	@Override
	public void delete(long id) {
		storage.remove(id);
	}

}
