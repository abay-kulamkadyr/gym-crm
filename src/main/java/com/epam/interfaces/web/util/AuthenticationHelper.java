package com.epam.interfaces.web.util;

import com.epam.application.Credentials;
import com.epam.application.exception.AuthenticationException;
import com.epam.application.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Helper class for handling authentication in controllers. Centralizes credential
 * extraction and validation logic.
 */
@Component
@RequiredArgsConstructor
public class AuthenticationHelper {

	private final AuthenticationService authenticationService;

	public Credentials extractAndValidateCredentials(String authHeader, String expectedUsername) {
		if (authHeader == null || authHeader.isBlank()) {
			throw new AuthenticationException("Authorization header is required");
		}

		Credentials credentials = parseAuthorizationHeader(authHeader);

		// Validate username matches path variable
		if (!expectedUsername.equals(credentials.username())) {
			throw new IllegalArgumentException("Username in path does not match authenticated user");
		}

		// Authenticate the user
		authenticationService.authenticate(credentials);

		return credentials;
	}

	private Credentials parseAuthorizationHeader(String header) {
		if (header == null || !header.contains(":")) {
			throw new IllegalArgumentException("Invalid Authorization header format. Expected 'username:password'");
		}

		String[] parts = header.split(":", 2);
		if (parts.length != 2 || parts[0].isBlank() || parts[1].isBlank()) {
			throw new IllegalArgumentException(
					"Invalid Authorization header format. Username and password cannot be empty");
		}

		return new Credentials(parts[0].trim(), parts[1].trim());
	}

}
