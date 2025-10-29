package com.epam.infrastructure.persistence.repository;

import com.epam.domain.repository.TrainerRepository;
import com.epam.infrastructure.persistence.dao.TrainerDao;
import com.epam.infrastructure.persistence.mapper.TrainerMapper;
import java.util.Collection;
import java.util.Map;
import com.epam.domain.model.Trainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

@Repository
public class TrainerRepositoryImpl implements TrainerRepository {

	private Map<Long, TrainerDao> storage;

	@Autowired
	public void setStorage(Map<Long, TrainerDao> storage) {
		this.storage = storage;
	}

	@Override
	public void save(@NonNull Trainer trainer) {
		TrainerDao entity = TrainerMapper.toEntity(trainer);
		storage.put(entity.getUserId(), entity);
	}

	@Override
	public Trainer findById(long id) {
		TrainerDao entity = storage.get(id);
		return TrainerMapper.toDomain(entity);
	}

	@Override
	public Collection<Trainer> findAll() {
		return storage.values().stream().map(TrainerMapper::toDomain).toList();
	}

	@Override
	public void delete(long id) {
		storage.remove(id);
	}

}
