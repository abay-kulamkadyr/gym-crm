package com.epam.infrastructure.bootstrap;

import com.epam.domain.model.Trainer;
import com.epam.infrastructure.persistence.dao.TrainerDao;
import com.epam.infrastructure.persistence.mapper.TrainerMapper;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Role;
import org.springframework.stereotype.Component;

@Component
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@InitializableStorage(entityType = TrainerDao.class)
class TrainerStorageInitializer implements StorageInitializer<TrainerDao> {

	private final DataLoader dataLoader;

	@Autowired
	public TrainerStorageInitializer(DataLoader dataLoader) {
		this.dataLoader = dataLoader;
	}

	@Override
	public void initialize(Map<Long, TrainerDao> storage) {
		List<Trainer> trainers = dataLoader.loadTrainers();
		trainers.forEach(trainer -> {
			TrainerDao entity = TrainerMapper.toEntity(trainer);
			storage.put(entity.getUserId(), entity);
		});
	}

}
