package com.epam.interfaces.web.util;

import com.epam.application.Credentials;
import com.epam.application.exception.AuthenticationException;
import com.epam.application.service.AuthenticationService;
import com.epam.domain.model.Trainee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationHelperTest {

	@Mock
	private AuthenticationService authenticationService;

	@InjectMocks
	private AuthenticationHelper authenticationHelper;

	private Trainee testTrainee;

	@BeforeEach
	void setUp() {
		testTrainee = new Trainee("John", "Doe", true);
		testTrainee.setUsername("john.doe");
		testTrainee.setPassword("password123");
		testTrainee.setDob(LocalDate.of(1990, 1, 1));
		testTrainee.setAddress("123 Main St");
	}

	@Test
	@DisplayName("Should extract and validate credentials successfully")
	void testExtractAndValidateCredentials_Success() {
		// Given
		String authHeader = "john.doe:password123";
		String expectedUsername = "john.doe";

		when(authenticationService.authenticate(any(Credentials.class))).thenReturn(testTrainee);

		// When
		Credentials result = authenticationHelper.extractAndValidateCredentials(authHeader, expectedUsername);

		// Then
		assertNotNull(result);
		assertEquals("john.doe", result.username());
		assertEquals("password123", result.password());
		verify(authenticationService).authenticate(any(Credentials.class));
	}

	@Test
	@DisplayName("Should throw exception when header is null")
	void testExtractAndValidateCredentials_NullHeader() {
		// When & Then
		AuthenticationException exception = assertThrows(AuthenticationException.class,
				() -> authenticationHelper.extractAndValidateCredentials(null, "john.doe"));

		assertEquals("Authorization header is required", exception.getMessage());
		verify(authenticationService, never()).authenticate(any());
	}

	@Test
	@DisplayName("Should throw exception when header is blank")
	void testExtractAndValidateCredentials_BlankHeader() {
		// When & Then
		AuthenticationException exception = assertThrows(AuthenticationException.class,
				() -> authenticationHelper.extractAndValidateCredentials("   ", "john.doe"));

		assertEquals("Authorization header is required", exception.getMessage());
		verify(authenticationService, never()).authenticate(any());
	}

	@Test
	@DisplayName("Should throw exception when header is empty string")
	void testExtractAndValidateCredentials_EmptyHeader() {
		// When & Then
		AuthenticationException exception = assertThrows(AuthenticationException.class,
				() -> authenticationHelper.extractAndValidateCredentials("", "john.doe"));

		assertEquals("Authorization header is required", exception.getMessage());
		verify(authenticationService, never()).authenticate(any());
	}

	@Test
	@DisplayName("Should throw exception when header format is invalid - no colon")
	void testExtractAndValidateCredentials_InvalidFormatNoColon() {
		// When & Then
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> authenticationHelper.extractAndValidateCredentials("invalidformat", "john.doe"));

		assertTrue(exception.getMessage().contains("Invalid Authorization header format"));
		verify(authenticationService, never()).authenticate(any());
	}

	@Test
	@DisplayName("Should throw exception when username is empty")
	void testExtractAndValidateCredentials_EmptyUsername() {
		// When & Then
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> authenticationHelper.extractAndValidateCredentials(":password123", "john.doe"));

		assertTrue(exception.getMessage().contains("Username and password cannot be empty"));
		verify(authenticationService, never()).authenticate(any());
	}

	@Test
	@DisplayName("Should throw exception when password is empty")
	void testExtractAndValidateCredentials_EmptyPassword() {
		// When & Then
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> authenticationHelper.extractAndValidateCredentials("john.doe:", "john.doe"));

		assertTrue(exception.getMessage().contains("Username and password cannot be empty"));
		verify(authenticationService, never()).authenticate(any());
	}

	@Test
	@DisplayName("Should throw exception when both username and password are empty")
	void testExtractAndValidateCredentials_EmptyBoth() {
		// When & Then
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> authenticationHelper.extractAndValidateCredentials(":", "john.doe"));

		assertTrue(exception.getMessage().contains("Username and password cannot be empty"));
		verify(authenticationService, never()).authenticate(any());
	}

	@Test
	@DisplayName("Should throw exception when username mismatch")
	void testExtractAndValidateCredentials_UsernameMismatch() {
		// When & Then
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> authenticationHelper.extractAndValidateCredentials("john.doe:password123", "different.user"));

		assertEquals("Username in path does not match authenticated user", exception.getMessage());
		verify(authenticationService, never()).authenticate(any());
	}

	@Test
	@DisplayName("Should throw exception when authentication fails")
	void testExtractAndValidateCredentials_AuthenticationFails() {
		// Given
		when(authenticationService.authenticate(any(Credentials.class)))
			.thenThrow(new AuthenticationException("Invalid credentials"));

		// When & Then
		assertThrows(AuthenticationException.class,
				() -> authenticationHelper.extractAndValidateCredentials("john.doe:wrongpassword", "john.doe"));

		verify(authenticationService).authenticate(any(Credentials.class));
	}

	@Test
	@DisplayName("Should trim whitespace from credentials")
	void testExtractAndValidateCredentials_TrimsWhitespace() {
		// Given
		String authHeader = " john.doe : password123 ";
		when(authenticationService.authenticate(any(Credentials.class))).thenReturn(testTrainee);

		// When
		Credentials result = authenticationHelper.extractAndValidateCredentials(authHeader, "john.doe");

		// Then
		assertEquals("john.doe", result.username());
		assertEquals("password123", result.password());
	}

	@Test
	@DisplayName("Should handle password with colon")
	void testExtractAndValidateCredentials_PasswordWithColon() {
		// Given
		String authHeader = "john.doe:pass:word:123";
		when(authenticationService.authenticate(any(Credentials.class))).thenReturn(testTrainee);

		// When
		Credentials result = authenticationHelper.extractAndValidateCredentials(authHeader, "john.doe");

		// Then
		assertEquals("john.doe", result.username());
		assertEquals("pass:word:123", result.password());
	}

	@Test
	@DisplayName("Should handle password with multiple colons")
	void testExtractAndValidateCredentials_PasswordWithMultipleColons() {
		// Given
		String authHeader = "john.doe:p:a:s:s:w:o:r:d";
		when(authenticationService.authenticate(any(Credentials.class))).thenReturn(testTrainee);

		// When
		Credentials result = authenticationHelper.extractAndValidateCredentials(authHeader, "john.doe");

		// Then
		assertEquals("john.doe", result.username());
		assertEquals("p:a:s:s:w:o:r:d", result.password());
	}

	@Test
	@DisplayName("Should handle username with special characters")
	void testExtractAndValidateCredentials_UsernameWithSpecialChars() {
		// Given
		String authHeader = "john.doe_123:password123";
		when(authenticationService.authenticate(any(Credentials.class))).thenReturn(testTrainee);

		// When
		Credentials result = authenticationHelper.extractAndValidateCredentials(authHeader, "john.doe_123");

		// Then
		assertEquals("john.doe_123", result.username());
		assertEquals("password123", result.password());
	}

	@Test
	@DisplayName("Should handle long password")
	void testExtractAndValidateCredentials_LongPassword() {
		// Given
		String longPassword = "a".repeat(100);
		String authHeader = "john.doe:" + longPassword;
		when(authenticationService.authenticate(any(Credentials.class))).thenReturn(testTrainee);

		// When
		Credentials result = authenticationHelper.extractAndValidateCredentials(authHeader, "john.doe");

		// Then
		assertEquals("john.doe", result.username());
		assertEquals(longPassword, result.password());
	}

	@Test
	@DisplayName("Should handle password with spaces")
	void testExtractAndValidateCredentials_PasswordWithSpaces() {
		// Given
		String authHeader = "john.doe:my pass word";
		when(authenticationService.authenticate(any(Credentials.class))).thenReturn(testTrainee);

		// When
		Credentials result = authenticationHelper.extractAndValidateCredentials(authHeader, "john.doe");

		// Then
		assertEquals("john.doe", result.username());
		assertEquals("my pass word", result.password());
	}

	@Test
	@DisplayName("Should throw exception when username is only whitespace")
	void testExtractAndValidateCredentials_UsernameOnlyWhitespace() {
		// When & Then
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> authenticationHelper.extractAndValidateCredentials("   :password123", "john.doe"));

		assertTrue(exception.getMessage().contains("Username and password cannot be empty"));
		verify(authenticationService, never()).authenticate(any());
	}

	@Test
	@DisplayName("Should throw exception when password is only whitespace")
	void testExtractAndValidateCredentials_PasswordOnlyWhitespace() {
		// When & Then
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> authenticationHelper.extractAndValidateCredentials("john.doe:   ", "john.doe"));

		assertTrue(exception.getMessage().contains("Username and password cannot be empty"));
		verify(authenticationService, never()).authenticate(any());
	}

}