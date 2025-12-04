package com.epam.infrastructure.bootstrap;

import com.epam.infrastructure.bootstrap.dto.InitialBootstrapData;
import com.epam.infrastructure.bootstrap.dto.TraineeDTO;
import com.epam.infrastructure.bootstrap.dto.TrainerDTO;
import com.epam.infrastructure.bootstrap.dto.TrainingDTO;
import com.epam.infrastructure.bootstrap.dto.TrainingTypeDTO;
import com.epam.infrastructure.bootstrap.dto.UserDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

@Component
@Profile("local")
@Slf4j
public class JsonDataLoader {

	@Autowired
	private final ObjectMapper objectMapper;

	private final String usersFile;

	private final String traineesFile;

	private final String trainersFile;

	private final String trainingsFile;

	private final String trainingTypesFile;

	public JsonDataLoader(ObjectMapper objectMapper, @Value("${storage.init.users:/users.json}") String usersFile,
			@Value("${storage.init.trainees:/trainees.json}") String traineesFile,
			@Value("${storage.init.trainers:/trainers.json}") String trainersFile,
			@Value("${storage.init.trainings:/trainings.json}") String trainingsFile,
			@Value("${storage.init.trainingTypes:/trainingTypes.json}") String trainingTypesFile) {
		this.objectMapper = objectMapper;
		this.usersFile = usersFile;
		this.traineesFile = traineesFile;
		this.trainersFile = trainersFile;
		this.trainingsFile = trainingsFile;
		this.trainingTypesFile = trainingTypesFile;
	}

	public InitialBootstrapData loadBootstrapData() {
		InitialBootstrapData data = new InitialBootstrapData();

		data.setUsers(loadList(usersFile, new TypeReference<List<UserDTO>>() {
		}));
		data.setTrainees(loadList(traineesFile, new TypeReference<List<TraineeDTO>>() {
		}));
		data.setTrainers(loadList(trainersFile, new TypeReference<List<TrainerDTO>>() {
		}));
		data.setTrainings(loadList(trainingsFile, new TypeReference<List<TrainingDTO>>() {
		}));
		data.setTrainingTypes(loadList(trainingTypesFile, new TypeReference<List<TrainingTypeDTO>>() {
		}));

		log.info("Loaded bootstrap data: {} users, {} trainers, {} trainees, {} trainings, {} types",
				data.getUsers().size(), data.getTrainers().size(), data.getTrainees().size(),
				data.getTrainings().size(), data.getTrainingTypes().size());

		return data;
	}

	private <T> List<T> loadList(String path, TypeReference<List<T>> type) {
		try (InputStream inputStream = getClass().getResourceAsStream(path)) {
			if (inputStream == null) {
				log.warn("Bootstrap file not found: {}. Returning empty list.", path);
				return Collections.emptyList();
			}
			return objectMapper.readValue(inputStream, type);
		}
		catch (IOException e) {
			log.error("Failed to load bootstrap data from {}: {}", path, e.getMessage());
			throw new RuntimeException("Failed to load bootstrap data from " + path, e);
		}
	}

}
