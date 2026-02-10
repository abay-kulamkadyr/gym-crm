package com.epam.domain;

import java.time.LocalDateTime;
import java.util.Optional;

import com.epam.domain.model.TrainingTypeEnum;

public record TrainingFilter(
        Optional<LocalDateTime> fromDate,
        Optional<LocalDateTime> toDate,
        Optional<String> trainerName,
        Optional<String> traineeName,
        Optional<TrainingTypeEnum> trainingType) {

    public static TrainingFilter empty() {
        return new TrainingFilter(
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    }

    public static TrainingFilter forTrainee(
            Optional<LocalDateTime> fromDate,
            Optional<LocalDateTime> toDate,
            Optional<String> trainerName,
            Optional<TrainingTypeEnum> trainingType) {
        return new TrainingFilter(fromDate, toDate, trainerName, Optional.empty(), trainingType);
    }

    public static TrainingFilter forTrainer(
            Optional<LocalDateTime> fromDate, Optional<LocalDateTime> toDate, Optional<String> traineeName) {
        return new TrainingFilter(fromDate, toDate, Optional.empty(), traineeName, Optional.empty());
    }
}
