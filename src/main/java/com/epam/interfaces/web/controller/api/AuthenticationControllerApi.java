package com.epam.interfaces.web.controller.api;

import com.epam.interfaces.web.dto.request.ChangeLoginRequest;
import com.epam.interfaces.web.dto.request.LoginRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Authentication", description = "Authentication operations")
public interface AuthenticationControllerApi {

	@Operation(summary = "Login", description = "Authenticate user with username and password")
	ResponseEntity<Void> login(@Valid @RequestBody LoginRequest request);

	@Operation(summary = "Change Password", description = "Change user password")
	ResponseEntity<Void> changePassword(@Valid @RequestBody ChangeLoginRequest request);

}