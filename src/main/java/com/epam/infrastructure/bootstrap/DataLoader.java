package com.epam.infrastructure.bootstrap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import com.epam.domain.model.Trainee;
import com.epam.domain.model.Trainer;
import com.epam.domain.model.Training;
import com.epam.domain.model.TrainingType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Role;
import org.springframework.stereotype.Component;

@Component
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@Slf4j
class DataLoader {

	private final ObjectMapper objectMapper;

	private final String trainersFile;

	private final String traineesFile;

	private final String trainingsFile;

	private final String trainingTypesFile;

	public DataLoader(@Value("${storage.init.trainers:/trainers.json}") String trainersFile,
			@Value("${storage.init.trainees:/trainees.json}") String traineesFile,
			@Value("${storage.init.trainings:/trainings.json}") String trainingsFile,
			@Value("${storage.init.trainingTypes:/trainingsTypes.json}") String trainingTypesFile) {
		this.objectMapper = new ObjectMapper();
		this.objectMapper.registerModule(new JavaTimeModule());
		this.trainersFile = trainersFile;
		this.traineesFile = traineesFile;
		this.trainingsFile = trainingsFile;
		this.trainingTypesFile = trainingTypesFile;
	}

	public List<Trainer> loadTrainers() {
		return loadData(trainersFile, Trainer.class, "trainers");
	}

	public List<Trainee> loadTrainees() {
		return loadData(traineesFile, Trainee.class, "trainees");
	}

	public List<Training> loadTrainings() {
		return loadData(trainingsFile, Training.class, "trainings");
	}

	public List<TrainingType> loadTrainingTypes() {
		return loadData(trainingTypesFile, TrainingType.class, "training types");
	}

	private <T> List<T> loadData(String resourcePath, Class<T> clazz, String dataType) {
		try (InputStream inputStream = getClass().getResourceAsStream(resourcePath)) {
			if (inputStream == null) {
				log.warn("Resource not found: {}. Returning empty list for {}", resourcePath, dataType);
				return Collections.emptyList();
			}

			List<T> data = objectMapper.readValue(inputStream,
					objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));

			log.info("Successfully loaded {} {} from {}", data.size(), dataType, resourcePath);
			return data;

		}
		catch (IOException e) {
			log.error("Failed to load {} from {}: {}", dataType, resourcePath, e.getMessage(), e);
			return Collections.emptyList();
		}
	}

}
