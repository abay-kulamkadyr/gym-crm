package com.epam.application.service;

import java.time.LocalDateTime;
import java.util.List;

import com.epam.application.request.CreateTrainingRequest;
import com.epam.domain.TrainingFilter;
import com.epam.domain.model.Training;

public interface TrainingService {

    Training create(CreateTrainingRequest request);

    List<Training> getTraineeTrainings(String username, TrainingFilter filter);

    List<Training> getTrainerTrainings(String username, TrainingFilter filter);

    void deleteTraining(String traineeUsername, String trainerUsername, LocalDateTime date);
}
