package com.epam.infrastructure.bootstrap;

import com.epam.domain.model.Training;
import com.epam.infrastructure.persistence.dao.TrainingDao;
import com.epam.infrastructure.persistence.mapper.TrainingMapper;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Role;
import org.springframework.stereotype.Component;

@Component
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@InitializableStorage(entityType = TrainingDao.class)
class TrainingStorageInitializer implements StorageInitializer<TrainingDao> {

	private final DataLoader dataLoader;

	@Autowired
	public TrainingStorageInitializer(DataLoader dataLoader) {
		this.dataLoader = dataLoader;
	}

	@Override
	public void initialize(Map<Long, TrainingDao> storage) {
		List<Training> trainings = dataLoader.loadTrainings();
		trainings.forEach(training -> {
			TrainingDao entity = TrainingMapper.toEntity(training);
			storage.put(entity.getTrainingId(), entity);
		});
	}

}
