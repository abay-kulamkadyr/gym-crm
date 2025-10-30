package com.epam.application.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class PasswordGeneratorTest {

	@Test
	void generate_shouldReturnPasswordOfCorrectLength() {
		// When
		String password = CredentialsGenerator.generateRandomPassword(10);

		// Then
		assertThat(password).hasSize(10);
	}

	@Test
	void generate_shouldReturnPasswordWithOnlyValidCharacters() {
		// Given
		String validChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

		// When
		String password = CredentialsGenerator.generateRandomPassword(100);

		// Then
		for (char c : password.toCharArray()) {
			assertThat(validChars).contains(String.valueOf(c));
		}
	}

	@Test
	void generate_shouldReturnDifferentPasswordsOnMultipleCalls() {
		// When
		String password1 = CredentialsGenerator.generateRandomPassword(10);
		String password2 = CredentialsGenerator.generateRandomPassword(10);
		String password3 = CredentialsGenerator.generateRandomPassword(10);

		// Then - at least one should be different (statistically almost certain)
		boolean allDifferent = !password1.equals(password3) || !password2.equals(password3);
		assertThat(allDifferent).isTrue();
	}

	@Test
	void generate_shouldHandleDifferentLengths() {
		// When
		String password5 = CredentialsGenerator.generateRandomPassword(5);
		String password15 = CredentialsGenerator.generateRandomPassword(15);
		String password20 = CredentialsGenerator.generateRandomPassword(20);

		// Then
		assertThat(password5).hasSize(5);
		assertThat(password15).hasSize(15);
		assertThat(password20).hasSize(20);
	}

	@Test
	void generate_shouldHandleMinimumLength() {
		// When
		String password = CredentialsGenerator.generateRandomPassword(1);

		// Then
		assertThat(password).hasSize(1);
	}

	@Test
	void generate_shouldReturnEmptyStringWithZeroLength() {
		// When
		String password = CredentialsGenerator.generateRandomPassword(0);

		// Then
		assertThat(password).isEqualTo("");
	}

	@Test
	void generate_shouldThrowWhenLengthNegative() {
		// Then
		assertThrows(NegativeArraySizeException.class, () -> CredentialsGenerator.generateRandomPassword(-20));
	}

}
