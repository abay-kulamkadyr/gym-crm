package com.epam.infrastructure.bootstrap;

import java.util.List;
import java.util.Map;
import com.epam.domain.model.Training;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Role;
import org.springframework.stereotype.Component;

@Component
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@InitializableStorage(entityType = Training.class)
class TrainingStorageInitializer implements StorageInitializer<Training> {

	private final DataLoader dataLoader;

	@Autowired
	public TrainingStorageInitializer(DataLoader dataLoader) {
		this.dataLoader = dataLoader;
	}

	@Override
	public void initialize(Map<Long, Training> storage) {
		List<Training> trainings = dataLoader.loadTrainings();
		trainings.forEach(training -> storage.put(training.getTrainingId(), training));
	}

}
