package com.epam.infrastructure.bootstrap;

import com.epam.infrastructure.persistence.dao.TrainingTypeDao;
import com.epam.infrastructure.persistence.mapper.TrainingTypeMapper;
import java.util.List;
import java.util.Map;
import com.epam.domain.model.TrainingType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Role;
import org.springframework.stereotype.Component;

@Component
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@InitializableStorage(entityType = TrainingTypeDao.class)
class TrainingTypeStorageInitializer implements StorageInitializer<TrainingTypeDao> {

	private final DataLoader dataLoader;

	@Autowired
	public TrainingTypeStorageInitializer(DataLoader dataLoader) {
		this.dataLoader = dataLoader;
	}

	@Override
	public void initialize(Map<Long, TrainingTypeDao> storage) {
		List<TrainingType> trainingTypes = dataLoader.loadTrainingTypes();
		trainingTypes.forEach(trainingType -> {
			TrainingTypeDao entity = TrainingTypeMapper.toEntity(trainingType);
			storage.put(entity.getTrainingTypeId(), entity);
		});

	}

}
