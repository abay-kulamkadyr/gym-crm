package com.epam.interfaces.web.controller.api;

import com.epam.interfaces.web.dto.request.ChangePasswordRequest;
import com.epam.interfaces.web.dto.request.LoginRequest;
import com.epam.interfaces.web.dto.response.JwtResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@Tag(name = "Authentication", description = "Authentication operations")
public interface UserControllerApi {

    @Operation(summary = "Login", description = "Authenticate user with username and password")
    ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request);

    @Operation(summary = "Change Password", description = "Change user password")
    ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal UserDetails userDetails, @Valid @RequestBody ChangePasswordRequest request);

    @Operation(
            summary = "Logout",
            description = "Revoke the current JWT token. Token will be blacklisted and cannot be used again.")
    ResponseEntity<Void> logout(@RequestHeader(value = "Authorization", required = false) String authHeader);
}
