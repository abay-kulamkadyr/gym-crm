package com.epam.bootstrap;

import java.util.List;
import java.util.Map;
import com.epam.domain.Trainee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Role;
import org.springframework.stereotype.Component;

@Component
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class TraineeStorageInitializer implements StorageInitializer<Trainee> {

	private final DataLoader dataLoader;

	@Autowired
	public TraineeStorageInitializer(DataLoader dataLoader) {
		this.dataLoader = dataLoader;
	}

	@Override
	public void initialize(Map<Long, Trainee> storage) {
		List<Trainee> trainees = dataLoader.loadTrainees();
		trainees.forEach(trainee -> storage.put(trainee.getUserId(), trainee));
	}

	@Override
	public String getStorageBeanName() {
		return "traineesStorage";
	}

}
