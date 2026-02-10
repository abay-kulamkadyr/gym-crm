package com.epam.interfaces.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank(message = "Username is required") String username,
        @NotBlank(message = "Old password is required") String oldPassword,
        @NotBlank(message = "New password is required")
                @Size(min = 10, max = 100, message = "Password must be between 10 and 100 characters")
                String newPassword) {}
