package com.epam.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordGeneratorTest {

	@Test
	void generate_shouldReturnPasswordOfCorrectLength() {
		// When
		String password = PasswordGenerator.generate(10);

		// Then
		assertThat(password).hasSize(10);
	}

	@Test
	void generate_shouldReturnPasswordWithOnlyValidCharacters() {
		// Given
		String validChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

		// When
		String password = PasswordGenerator.generate(100);

		// Then
		for (char c : password.toCharArray()) {
			assertThat(validChars).contains(String.valueOf(c));
		}
	}

	@Test
	void generate_shouldReturnDifferentPasswordsOnMultipleCalls() {
		// When
		String password1 = PasswordGenerator.generate(10);
		String password2 = PasswordGenerator.generate(10);
		String password3 = PasswordGenerator.generate(10);

		// Then - at least one should be different (statistically almost certain)
		boolean allDifferent = !password1.equals(password2) || !password1.equals(password3)
				|| !password2.equals(password3);
		assertThat(allDifferent).isTrue();
	}

	@Test
	void generate_shouldHandleDifferentLengths() {
		// When
		String password5 = PasswordGenerator.generate(5);
		String password15 = PasswordGenerator.generate(15);
		String password20 = PasswordGenerator.generate(20);

		// Then
		assertThat(password5).hasSize(5);
		assertThat(password15).hasSize(15);
		assertThat(password20).hasSize(20);
	}

	@Test
	void generate_shouldHandleMinimumLength() {
		// When
		String password = PasswordGenerator.generate(1);

		// Then
		assertThat(password).hasSize(1);
	}

}
