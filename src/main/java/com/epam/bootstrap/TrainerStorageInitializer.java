package com.epam.bootstrap;

import java.util.List;
import java.util.Map;
import com.epam.domain.Trainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Role;
import org.springframework.stereotype.Component;

@Component
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@InitializableStorage(entityType = Trainer.class)
class TrainerStorageInitializer implements StorageInitializer<Trainer> {

	private final DataLoader dataLoader;

	@Autowired
	public TrainerStorageInitializer(DataLoader dataLoader) {
		this.dataLoader = dataLoader;
	}

	@Override
	public void initialize(Map<Long, Trainer> storage) {
		List<Trainer> trainers = dataLoader.loadTrainers();
		trainers.forEach(trainer -> storage.put(trainer.getUserId(), trainer));
	}

}
