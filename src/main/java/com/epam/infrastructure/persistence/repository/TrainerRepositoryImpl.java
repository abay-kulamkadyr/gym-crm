package com.epam.infrastructure.persistence.repository;

import com.epam.domain.model.Trainer;
import com.epam.domain.repository.TrainerRepository;
import com.epam.infrastructure.persistence.dao.TrainerDao;
import com.epam.infrastructure.persistence.mapper.TrainerMapper;
import java.util.Comparator;
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
		if (prefix == null) {
			return Optional.empty();
		}

		return storage.values()
			.stream()
			.filter(dao -> dao.getUsername() != null && dao.getUsername().startsWith(prefix))
			.max(Comparator.comparingLong(dao -> {
				String username = dao.getUsername();
				String serialPart = username.substring(prefix.length());

				if (serialPart.isEmpty()) {
					return 0L; // Base username has serial 0
				}

				try {
					return Long.parseLong(serialPart);
				}
				catch (NumberFormatException e) {
					return 0L; // Treat invalid as 0
				}
			}))
			.map(TrainerDao::getUsername);
	}

}
