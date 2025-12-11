package com.epam.interfaces.web.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

public record UpdateTraineeRequest(
        @NotBlank(message = "First name is required") @Size(
                min = 2,
                max = 50,
                message = "First name must be between 2 and 50 characters") String firstName,

        @NotBlank(message = "Last name is required") @Size(
                min = 2,
                max = 50,
                message = "Last name must be between 2 and 50 characters") String lastName,

        @Past(message = "Date of birth must be in the past") LocalDate dateOfBirth,

        @Size(max = 200, message = "Address must not exceed 200 characters") String address,

        @NotNull(message = "Active status is required") Boolean active) {}
