package com.epam.interfaces.web.dto.response;

import com.epam.domain.model.Trainer;
import com.epam.domain.model.TrainingTypeEnum;

public record EmbeddedTrainerResponse(
        String username, String firstName, String lastName, TrainingTypeEnum specialization) {

    public static EmbeddedTrainerResponse toEmbeddedTrainer(Trainer trainer) {
        return new EmbeddedTrainerResponse(
                trainer.getUsername(),
                trainer.getFirstName(),
                trainer.getLastName(),
                trainer.getSpecialization().getTrainingTypeName());
    }
}
