package com.epam.application.request;

import java.time.LocalDateTime;
import java.util.Optional;

import com.epam.domain.model.TrainingTypeEnum;

public record CreateTrainingRequest(
        String trainingName,
        LocalDateTime trainingDate,
        Integer trainingDurationMin,
        Optional<TrainingTypeEnum> trainingType,
        String traineeUsername,
        String trainerUsername) {}
