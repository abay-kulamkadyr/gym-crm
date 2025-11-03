package com.epam.infrastructure.config;

import com.epam.infrastructure.bootstrap.InitializableStorage;
import com.epam.infrastructure.persistence.dao.TraineeDao;
import com.epam.infrastructure.persistence.dao.TrainerDao;
import com.epam.infrastructure.persistence.dao.TrainingDao;
import com.epam.infrastructure.persistence.dao.TrainingTypeDao;
import java.util.HashMap;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@ComponentScan(basePackages = "com.epam")
@PropertySource("classpath:application.properties")
public class AppConfig {

	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}

	@Bean("traineesStorage")
	@InitializableStorage(entityType = TraineeDao.class)
	public Map<Long, TraineeDao> traineeStorage() {
		return new HashMap<>();
	}

	@Bean("trainersStorage")
	@InitializableStorage(entityType = TrainerDao.class)
	public Map<Long, TrainerDao> trainersStorage() {
		return new HashMap<>();
	}

	@Bean("trainingsStorage")
	@InitializableStorage(entityType = TrainingDao.class)
	public Map<Long, TrainingDao> trainingsStorage() {
		return new HashMap<>();
	}

	@Bean("trainingTypesStorage")
	@InitializableStorage(entityType = TrainingTypeDao.class)
	public Map<Long, TrainingTypeDao> trainingTypesStorage() {
		return new HashMap<>();
	}

}
