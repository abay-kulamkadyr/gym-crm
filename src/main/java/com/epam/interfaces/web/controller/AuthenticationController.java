package com.epam.interfaces.web.controller;

import com.epam.application.Credentials;
import com.epam.application.facade.GymFacade;
import com.epam.application.service.AuthenticationService;
import com.epam.domain.model.Trainee;
import com.epam.domain.model.Trainer;
import com.epam.domain.model.User;
import com.epam.interfaces.web.dto.request.ChangeLoginRequest;
import com.epam.interfaces.web.dto.request.LoginRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication operations")
public class AuthenticationController {

	private final AuthenticationService authService;

	private final GymFacade gymFacade;

	@Autowired
	public AuthenticationController(AuthenticationService authService, GymFacade gymFacade) {
		this.authService = authService;
		this.gymFacade = gymFacade;
	}

	@PostMapping("/login")
	@Operation(summary = "Login", description = "Authenticate user with username and password")
	public ResponseEntity<Void> login(@Valid @RequestBody LoginRequest request) {
		authService.authenticate(new Credentials(request.username(), request.password()));
		return ResponseEntity.ok().build();
	}

	@PutMapping("/password")
	@Operation(summary = "Change Password", description = "Change user password")
	public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangeLoginRequest request) {
		Credentials credentials = new Credentials(request.username(), request.oldPassword());
		User user = authService.authenticate(credentials);

		if (user instanceof Trainee) {
			gymFacade.updateTraineePassword(credentials, request.newPassword());
		}
		else if (user instanceof Trainer) {
			gymFacade.updateTrainerPassword(credentials, request.newPassword());
		}
		else {
			throw new IllegalStateException("Unsupported user type: " + user.getClass().getSimpleName());
		}

		return ResponseEntity.ok().build();
	}

}
