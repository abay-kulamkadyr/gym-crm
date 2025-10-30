package com.epam.infrastructure.persistence.repository;

import com.epam.domain.model.Trainer;
import com.epam.domain.repository.TrainerRepository;
import com.epam.infrastructure.persistence.dao.TrainerDao;
import com.epam.infrastructure.persistence.mapper.TrainerMapper;
import com.epam.infrastructure.persistence.util.UsernameFinder;
import java.util.Map;
import java.util.Optional;
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
	public Optional<Trainer> findById(long id) {
		TrainerDao entity = storage.get(id);
		if (entity == null) {
			return Optional.empty();
		}
		return Optional.of(TrainerMapper.toDomain(entity));
	}

	@Override
	public void delete(long id) {
		storage.remove(id);
	}

	@Override
	public Optional<Trainer> findByUsername(String username) {
		return storage.values()
			.stream()
			.filter(trainerDao -> trainerDao.getUsername().equals(username))
			.map(TrainerMapper::toDomain)
			.findAny();
	}

	@Override
	public Optional<String> findLatestUsername(String prefix) {
		return UsernameFinder.findLatestUsername(storage.values(), prefix, TrainerDao::getUsername);
	}

}
