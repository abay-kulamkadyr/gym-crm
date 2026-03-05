package com.epam.application.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import com.epam.application.messaging.event.TrainerWorkloadEvent;
import com.epam.application.messaging.publisher.TrainingEventPublisher;
import com.epam.application.request.CreateTrainingRequest;
import com.epam.application.service.TrainingService;
import com.epam.domain.TrainingFilter;
import com.epam.domain.model.Trainee;
import com.epam.domain.model.Trainer;
import com.epam.domain.model.Training;
import com.epam.domain.model.TrainingType;
import com.epam.domain.model.TrainingTypeEnum;
import com.epam.domain.port.TraineeRepository;
import com.epam.domain.port.TrainerRepository;
import com.epam.domain.port.TrainingRepository;
import com.epam.domain.port.TrainingTypeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
public class TrainingServiceImpl implements TrainingService {

    private final TrainingRepository trainingRepository;

    private final TrainerRepository trainerRepository;

    private final TraineeRepository traineeRepository;

    private final TrainingTypeRepository trainingTypeRepository;

    private final TrainingEventPublisher eventPublisher;

    @Autowired
    public TrainingServiceImpl(
            TrainingRepository trainingRepository,
            TrainerRepository trainerRepository,
            TraineeRepository traineeRepository,
            TrainingTypeRepository trainingTypeRepository,
            TrainingEventPublisher eventPublisher) {
        this.trainingRepository = trainingRepository;
        this.trainerRepository = trainerRepository;
        this.traineeRepository = traineeRepository;
        this.trainingTypeRepository = trainingTypeRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Training create(CreateTrainingRequest request) {

        Trainee trainee = findTraineeOrThrow(request.traineeUsername());
        Trainer trainer = findTrainerOrThrow(request.trainerUsername());

        TrainingType trainingType;
        if (request.trainingType().isEmpty()) {
            trainingType = trainer.getSpecialization();
        } else {
            trainingType = findTrainingType(request.trainingType().get());
        }

        Training training = Training.builder()
                .trainingName(request.trainingName())
                .trainingDate(request.trainingDate())
                .trainingDurationMin(request.trainingDurationMin())
                .trainee(trainee)
                .trainer(trainer)
                .trainingType(trainingType)
                .build();

        // This transaction commits before the Kafka send completes
        Training savedTraining = trainingRepository.save(training);
        TrainerWorkloadEvent event = createTrainerWorkloadEvent(training, TrainerWorkloadEvent.ActionType.ADD);
        eventPublisher.publishTrainingCreated(event); // fire and forget

        return savedTraining;
    }

    @Override
    public List<Training> getTraineeTrainings(String username, TrainingFilter filter) {
        findTraineeOrThrow(username);
        return trainingRepository.getTraineeTrainings(username, filter);
    }

    @Override
    public List<Training> getTrainerTrainings(String username, TrainingFilter filter) {
        findTrainerOrThrow(username);
        return trainingRepository.getTrainerTrainings(username, filter);
    }

    @Override
    public void deleteTraining(String traineeUsername, String trainerUsername, LocalDateTime date) {
        log.debug("Deleting training for trainee: {}, trainer: {} on date: {}", traineeUsername, trainerUsername, date);

        trainingRepository
                .findByTrainerUsernameAndTraineeUsernameAndDate(trainerUsername, traineeUsername, date)
                .ifPresentOrElse(
                        training -> {
                            trainingRepository.deleteByTraineeTrainerAndDate(traineeUsername, trainerUsername, date);

                            TrainerWorkloadEvent event =
                                    createTrainerWorkloadEvent(training, TrainerWorkloadEvent.ActionType.DELETE);
                            eventPublisher.publishTrainingDeleted(event);

                            log.info("Successfully deleted training and published deletion event");
                        },
                        () -> log.warn(
                                "Delete skipped: No training found for trainee: {}, trainer: {} on date: {}",
                                traineeUsername,
                                trainerUsername,
                                date));
    }

    private Trainee findTraineeOrThrow(String username) {
        return traineeRepository.findByUsername(username).orElseThrow(() -> {
            log.warn("Trainee not found with username: {}", username);
            return new EntityNotFoundException("Trainee not found: " + username);
        });
    }

    private Trainer findTrainerOrThrow(String username) {
        return trainerRepository.findByUsername(username).orElseThrow(() -> {
            log.warn("Trainer not found with username: {}", username);
            return new EntityNotFoundException("Trainer not found: " + username);
        });
    }

    private TrainingType findTrainingType(TrainingTypeEnum name) {
        return trainingTypeRepository.findByTrainingTypeName(name).orElseThrow(() -> {
            log.warn("TrainingType not found with name: {}", name);
            return new EntityNotFoundException("TrainingType not found: " + name);
        });
    }

    private TrainerWorkloadEvent createTrainerWorkloadEvent(
            Training training, TrainerWorkloadEvent.ActionType actionType) {
        return TrainerWorkloadEvent.builder()
                .trainerUsername(training.getTrainer().getUsername())
                .trainerFirstname(training.getTrainer().getFirstName())
                .trainerLastname(training.getTrainer().getLastName())
                .isActive(training.getTrainer().getActive())
                .trainingDate(training.getTrainingDate())
                .trainingDurationMinutes(training.getTrainingDurationMin())
                .actionType(actionType)
                .build();
    }
}
