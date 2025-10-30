package com.epam.infrastructure.bootstrap;

import com.epam.domain.model.Trainee;
import com.epam.infrastructure.persistence.dao.TraineeDao;
import com.epam.infrastructure.persistence.mapper.TraineeMapper;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Role;
import org.springframework.stereotype.Component;

@Component
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@InitializableStorage(entityType = TraineeDao.class)
class TraineeStorageInitializer implements StorageInitializer<TraineeDao> {

	private final DataLoader dataLoader;

	@Autowired
	public TraineeStorageInitializer(DataLoader dataLoader) {
		this.dataLoader = dataLoader;
	}

	@Override
	public void initialize(Map<Long, TraineeDao> storage) {
		List<Trainee> trainees = dataLoader.loadTrainees();
		trainees.forEach(trainee -> {
			TraineeDao entity = TraineeMapper.toEntity(trainee);
			storage.put(entity.getUserId(), entity);
		});
	}

}
