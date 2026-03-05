package com.epam.interfaces.web.dto.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DeleteTrainingRequest(
        @NotBlank(message = "Trainee username is required") String traineeUsername,
        @NotBlank(message = "Trainer username is required") String trainerUsername,
        @NotNull(message = "Training date is required") LocalDateTime trainingDate) {}
