package com.epam.infrastructure.config;

import com.epam.infrastructure.bootstrap.InitializableStorage;
import java.util.HashMap;
import java.util.Map;
import com.epam.domain.model.Trainee;
import com.epam.domain.model.Trainer;
import com.epam.domain.model.Training;
import com.epam.domain.model.TrainingType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@ComponentScan(basePackages = "com.epam")
@PropertySource("classpath:application.properties")
public class AppConfig {

	@Bean("traineesStorage")
	@InitializableStorage(entityType = Trainee.class)
	public Map<Long, Trainee> traineeStorage() {
		return new HashMap<>();
	}

	@Bean("trainersStorage")
	@InitializableStorage(entityType = Trainer.class)
	public Map<Long, Trainer> trainersStorage() {
		return new HashMap<>();
	}

	@Bean("trainingsStorage")
	@InitializableStorage(entityType = Training.class)
	public Map<Long, Training> trainingsStorage() {
		return new HashMap<>();
	}

	@Bean("trainingTypesStorage")
	@InitializableStorage(entityType = TrainingType.class)
	public Map<Long, TrainingType> trainingTypesStorage() {
		return new HashMap<>();
	}

	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}

}
