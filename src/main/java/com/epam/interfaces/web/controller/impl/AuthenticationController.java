package com.epam.interfaces.web.controller.impl;

import com.epam.application.Credentials;
import com.epam.application.facade.GymFacade;
import com.epam.application.service.AuthenticationService;
import com.epam.domain.model.Trainee;
import com.epam.domain.model.Trainer;
import com.epam.domain.model.User;
import com.epam.interfaces.web.controller.api.AuthenticationControllerApi;
import com.epam.interfaces.web.dto.request.ChangeLoginRequest;
import com.epam.interfaces.web.dto.request.LoginRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController implements AuthenticationControllerApi {

	private final AuthenticationService authService;

	private final GymFacade gymFacade;

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private JwtEncoder encoder;

	@Autowired
	public AuthenticationController(AuthenticationService authService, GymFacade gymFacade) {
		this.authService = authService;
		this.gymFacade = gymFacade;
	}

	@PostMapping("/login")
	public ResponseEntity<String> login(@Valid @RequestBody LoginRequest request) {
		// authService.authenticate(new Credentials(request.username(),
		// request.password()));
		// return ResponseEntity.ok().build();
		Authentication authentication = authenticationManager
			.authenticate(new UsernamePasswordAuthenticationToken(request.username(), request.password()));

		Instant now = Instant.now();
		long expiry = 36000L;

		JwtClaimsSet claims = JwtClaimsSet.builder()
			.issuer("self")
			.issuedAt(now)
			.expiresAt(now.plusSeconds(expiry))
			.subject(authentication.getName())
			.build();

		return ResponseEntity.ok(this.encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue());
	}

	@PutMapping("/password")
	public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangeLoginRequest request) {
		Credentials credentials = new Credentials(request.username(), request.oldPassword());
		User user = authService.authenticate(credentials);

		if (user instanceof Trainee) {
			gymFacade.updateTraineePassword(request.username(), request.newPassword());
		}
		else if (user instanceof Trainer) {
			gymFacade.updateTrainerPassword(request.username(), request.newPassword());
		}
		else {
			throw new IllegalStateException("Unsupported user type: " + user.getClass().getSimpleName());
		}

		return ResponseEntity.ok().build();
	}

}
