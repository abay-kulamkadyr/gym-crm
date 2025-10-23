package com.epam.validation;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import com.epam.domain.Trainee;
import com.epam.domain.Trainer;
import com.epam.domain.Training;
import com.epam.domain.TrainingType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RelationshipValidator implements ApplicationListener<ContextRefreshedEvent> {

	private final Map<Long, Training> trainingsStorage;

	private final Map<Long, Trainer> trainersStorage;

	private final Map<Long, Trainee> traineesStorage;

	private final Map<Long, TrainingType> trainingTypesStorage;

	@Autowired
	public RelationshipValidator(Map<Long, Training> trainingsStorage, Map<Long, Trainer> trainersStorage,
			Map<Long, Trainee> traineesStorage, Map<Long, TrainingType> trainingTypesStorage) {
		this.trainingsStorage = trainingsStorage;
		this.trainersStorage = trainersStorage;
		this.traineesStorage = traineesStorage;
		this.trainingTypesStorage = trainingTypesStorage;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		log.info("Context refreshed - validating relationships");
		validateRelationships();
	}

	/**
	 * Validate that all relationships are properly linked
	 */
	private void validateRelationships() {
		log.info("Validating relationships...");

		int issues = 0;

		// Check trainers
		for (Trainer trainer : trainersStorage.values()) {
			if (trainer.getTrainingId() > 0 && !trainingsStorage.containsKey(trainer.getTrainingId())) {
				log.warn("Trainer {} references non-existent training {}", trainer.getUserId(),
						trainer.getTrainingId());
				issues++;
			}
			if (trainer.getTrainingTypeId() > 0 && !trainingTypesStorage.containsKey(trainer.getTrainingTypeId())) {
				log.warn("Trainer {} references non-existent training type {}", trainer.getUserId(),
						trainer.getTrainingTypeId());
				issues++;
			}
		}

		// Check trainees
		for (Trainee trainee : traineesStorage.values()) {
			if (trainee.getTrainingId() > 0 && !trainingsStorage.containsKey(trainee.getTrainingId())) {
				log.warn("Trainee {} references non-existent training {}", trainee.getUserId(),
						trainee.getTrainingId());
				issues++;
			}
		}

		// Check trainings
		for (Training training : trainingsStorage.values()) {
			if (training.getTrainerId() > 0 && !trainersStorage.containsKey(training.getTrainerId())) {
				log.warn("Training {} references non-existent trainer {}", training.getTrainingId(),
						training.getTrainerId());
				issues++;
			}
			if (training.getTraineeId() > 0 && !traineesStorage.containsKey(training.getTraineeId())) {
				log.warn("Training {} references non-existent trainee {}", training.getTrainingId(),
						training.getTraineeId());
				issues++;
			}
			if (training.getTrainingTypeId() > 0 && !trainingTypesStorage.containsKey(training.getTrainingTypeId())) {
				log.warn("Training {} references non-existent training type {}", training.getTrainingId(),
						training.getTrainingTypeId());
				issues++;
			}
		}

		// Check training types
		for (TrainingType type : trainingTypesStorage.values()) {
			if (type.getTrainerId() > 0 && !trainersStorage.containsKey(type.getTrainerId())) {
				log.warn("TrainingType {} references non-existent trainer {}", type.getId(), type.getTrainerId());
				issues++;
			}
			if (type.getTrainingId() > 0 && !trainingsStorage.containsKey(type.getTrainingId())) {
				log.warn("TrainingType {} references non-existent training {}", type.getId(), type.getTrainingId());
				issues++;
			}
		}

		if (issues == 0) {
			log.info("All relationships are valid!");
		}
		else {
			log.warn("Found {} relationship issues", issues);
		}
	}

}