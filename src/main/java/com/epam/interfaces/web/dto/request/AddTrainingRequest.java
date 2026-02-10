package com.epam.interfaces.web.dto.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AddTrainingRequest(
        @NotBlank(message = "Trainee username is required") String traineeUsername,
        @NotBlank(message = "Trainer username is required") String trainerUsername,
        @NotBlank(message = "Training name is required")
                @Size(min = 3, max = 100, message = "Training name must be between 3 and 100 characters")
                String trainingName,
        @NotNull(message = "Training date is required")
                @FutureOrPresent(message = "Training date must be in present or future")
                LocalDateTime trainingDate,
        @NotNull(message = "Training duration is required")
                @Min(value = 1, message = "Training duration must be at least 1 minute")
                @Max(value = 480, message = "Training duration must not exceed 480 minutes (8 hours)")
                Integer trainingDurationMin) {}
