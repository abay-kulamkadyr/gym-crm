package com.epam.config;

import java.util.HashMap;
import java.util.Map;
import com.epam.domain.Trainee;
import com.epam.domain.Trainer;
import com.epam.domain.Training;
import com.epam.domain.TrainingType;
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
	public Map<Long, Trainee> traineeStorage() {
		return new HashMap<>();
	}

	@Bean("trainersStorage")
	public Map<Long, Trainer> trainersStorage() {
		return new HashMap<>();
	}

	@Bean("trainingsStorage")
	public Map<Long, Training> trainingsStorage() {
		return new HashMap<>();
	}

	@Bean("trainingTypesStorage")
	public Map<Long, TrainingType> trainingTypesStorage() {
		return new HashMap<>();
	}

	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}

}
