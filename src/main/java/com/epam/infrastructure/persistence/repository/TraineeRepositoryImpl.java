package com.epam.infrastructure.persistence.repository;

import com.epam.domain.model.Trainee;
import com.epam.domain.repository.TraineeRepository;
import com.epam.infrastructure.persistence.dao.TraineeDao;
import com.epam.infrastructure.persistence.mapper.TraineeMapper;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
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
	public Optional<Trainee> findById(long id) {
		TraineeDao entity = storage.get(id);
		if (entity == null) {
			return Optional.empty();
		}
		return Optional.of(TraineeMapper.toDomain(entity));
	}

	@Override
	public void delete(long id) {
		storage.remove(id);
	}

	@Override
	public Optional<Trainee> findByUsername(String username) {
		return storage.values()
			.stream()
			.filter(traineeDao -> traineeDao.getUsername().equals(username))
			.map(TraineeMapper::toDomain)
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
			.map(TraineeDao::getUsername);
	}

}
