package com.epam.infrastructure.persistence.repository;

import com.epam.domain.repository.TraineeRepository;
import com.epam.infrastructure.persistence.dao.TraineeDao;
import com.epam.infrastructure.persistence.mapper.TraineeMapper;
import java.util.Collection;
import java.util.Map;
import com.epam.domain.model.Trainee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

@Repository
public class TraineeRepositoryImpl implements TraineeRepository {

	private Map<Long, TraineeDao> storage;

	@Autowired
	public void setStorage(Map<Long, TraineeDao> storage) {
		this.storage = storage;
	}

	@Override
	public void save(@NonNull Trainee trainee) {
		TraineeDao entity = TraineeMapper.toEntity(trainee);
		storage.put(entity.getUserId(), entity);
	}

	@Override
	public Trainee findById(long id) {
		TraineeDao entity = storage.get(id);
		return TraineeMapper.toDomain(entity);
	}

	@Override
	public Collection<Trainee> findAll() {
		return storage.values().stream().map(TraineeMapper::toDomain).toList();
	}

	@Override
	public void delete(long id) {
		storage.remove(id);
	}

}
