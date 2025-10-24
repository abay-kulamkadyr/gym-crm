package com.epam.domain.validation;

import com.epam.domain.repository.TraineeRepository;
import com.epam.domain.repository.TrainerRepository;
import com.epam.domain.repository.TrainingRepository;
import com.epam.domain.repository.TrainingTypeRepository;
import lombok.extern.slf4j.Slf4j;
import com.epam.domain.model.Trainee;
import com.epam.domain.model.Trainer;
import com.epam.domain.model.Training;
import com.epam.domain.model.TrainingType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RelationshipValidator implements ApplicationListener<ContextRefreshedEvent> {

	private final TraineeRepository traineeRepository;

	private final TrainerRepository trainerRepository;

	private final TrainingRepository trainingRepository;

	private final TrainingTypeRepository trainingTypeRepository;

	@Autowired
	public RelationshipValidator(TraineeRepository traineeRepository, TrainerRepository trainerRepository,
			TrainingRepository trainingRepository, TrainingTypeRepository trainingTypeRepository) {
		this.traineeRepository = traineeRepository;
		this.trainerRepository = trainerRepository;
		this.trainingRepository = trainingRepository;
		this.trainingTypeRepository = trainingTypeRepository;
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
		for (Trainer trainer : trainerRepository.findAll()) {
			if (trainer.getTrainingId() > 0 && trainingRepository.findById(trainer.getTrainingId()) == null) {
				log.warn("Trainer {} references non-existent training {}", trainer.getUserId(),
						trainer.getTrainingId());
				issues++;
			}
			if (trainer.getTrainingTypeId() > 0
					&& trainingTypeRepository.findById(trainer.getTrainingTypeId()) == null) {
				log.warn("Trainer {} references non-existent training type {}", trainer.getUserId(),
						trainer.getTrainingTypeId());
				issues++;
			}
		}

		// Check trainees
		for (Trainee trainee : traineeRepository.findAll()) {
			if (trainee.getTrainingId() > 0 && trainingRepository.findById(trainee.getTrainingId()) == null) {
				log.warn("Trainee {} references non-existent training {}", trainee.getUserId(),
						trainee.getTrainingId());
				issues++;
			}
		}

		// Check trainings
		for (Training training : trainingRepository.findAll()) {
			if (training.getTrainerId() > 0 && trainerRepository.findById(training.getTrainerId()) == null) {
				log.warn("Training {} references non-existent trainer {}", training.getTrainingId(),
						training.getTrainerId());
				issues++;
			}
			if (training.getTraineeId() > 0 && traineeRepository.findById(training.getTraineeId()) == null) {
				log.warn("Training {} references non-existent trainee {}", training.getTrainingId(),
						training.getTraineeId());
				issues++;
			}
			if (training.getTrainingTypeId() > 0
					&& trainingTypeRepository.findById(training.getTrainingTypeId()) == null) {
				log.warn("Training {} references non-existent training type {}", training.getTrainingId(),
						training.getTrainingTypeId());
				issues++;
			}
		}

		// Check training types
		for (TrainingType type : trainingTypeRepository.findAll()) {
			if (type.getTrainerId() > 0 && trainerRepository.findById(type.getTrainerId()) == null) {
				log.warn("TrainingType {} references non-existent trainer {}", type.getId(), type.getTrainerId());
				issues++;
			}
			if (type.getTrainingId() > 0 && trainingRepository.findById(type.getTrainingId()) == null) {
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