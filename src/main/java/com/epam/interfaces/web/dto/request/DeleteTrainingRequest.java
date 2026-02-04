package com.epam.interfaces.web.dto.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DeleteTrainingRequest(@NotBlank(message = "Trainee username is required") String traineeUsername,

        @NotBlank(message = "Trainer username is required") String trainerUsername,

        @NotNull(message = "Training date is required") LocalDateTime trainingDate,

        @NotNull(message = "Training duration is required") @Min(
                value = 1,
                message = "Training duration must be at least 1 minute") @Max(
                        value = 480,
                        message = "Training duration must not exceed 480 minutes (8 hours)") Integer trainingDurationMin) {}
