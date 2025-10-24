package com.epam.bootstrap;

import java.util.List;
import java.util.Map;
import com.epam.domain.TrainingType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Role;
import org.springframework.stereotype.Component;

@Component
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@InitializableStorage(entityType = TrainingType.class)
class TrainingTypeStorageInitializer implements StorageInitializer<TrainingType> {

	private final DataLoader dataLoader;

	@Autowired
	public TrainingTypeStorageInitializer(DataLoader dataLoader) {
		this.dataLoader = dataLoader;
	}

	@Override
	public void initialize(Map<Long, TrainingType> storage) {
		List<TrainingType> trainingTypes = dataLoader.loadTrainingTypes();
		trainingTypes.forEach(training -> storage.put(training.getId(), training));

	}

}
