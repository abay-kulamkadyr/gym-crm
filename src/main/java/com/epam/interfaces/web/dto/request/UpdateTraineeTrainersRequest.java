package com.epam.interfaces.web.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record UpdateTraineeTrainersRequest(
        @NotEmpty(message = "Trainer list cannot be empty") @NotNull(message = "Trainer list is required")
                List<@NotNull(message = "Trainer username cannot be null") String> trainerUsernames) {}
