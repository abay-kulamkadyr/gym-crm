package com.epam.application.util;

import com.epam.application.exception.ValidationException;

import java.security.SecureRandom;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;

public class CredentialsUtil {

	private static final String PASSWORD_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

	private static final int MIN_PASSWORD_LENGTH = 10;

	// letter, hyphens, apostrophes
	private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z\\s'-]*$");

	// FirstName.LastName{number} pattern
	private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z]+\\.[A-Za-z]+(\\d+)?$");

	private static final SecureRandom RANDOM = new SecureRandom();

	private CredentialsUtil() {
		throw new UnsupportedOperationException("Utility class");
	}

	public static String generateUniqueUsername(String firstname, String lastName,
			Function<String, Optional<String>> latestUsernameFinder) {
		String baseUsername = firstname + "." + lastName;
		String res = baseUsername;
		Optional<String> latestUsername = latestUsernameFinder.apply(baseUsername);
		if (latestUsername.isEmpty()) {
			return baseUsername;
		}
		else {
			try {
				String serialPart = latestUsername.get().substring(baseUsername.length());
				res += Long.parseLong(serialPart) + 1;
			}
			catch (NumberFormatException e) {
				res += "1";
			}
		}
		return res;

	}

	public static String generateRandomPassword(int length) {
		StringBuilder sb = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			sb.append(PASSWORD_CHARS.charAt(RANDOM.nextInt(PASSWORD_CHARS.length())));
		}
		return sb.toString();
	}

	public static void validatePassword(String password) {
		if (password == null) {
			throw new ValidationException("Password is null");
		}

		if (password.length() <= MIN_PASSWORD_LENGTH) {
			throw new ValidationException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters long");
		}

		if (password.length() > 100) {
			throw new ValidationException("Password must not exceed 100 characters");
		}

		// Password should not be only whitespace
		if (password.trim().isEmpty()) {
			throw new ValidationException("Password cannot contain only whitespace");
		}

	}

	public static void validateUsername(String username) {
		if (username == null || username.trim().isEmpty()) {
			throw new ValidationException("Username cannot be null or empty");
		}

		if (username.length() < 5) {
			throw new ValidationException("Username must be at least 5 characters long");
		}

		if (username.length() > 100) {
			throw new ValidationException("Username must not exceed 100 characters");
		}

		if (!USERNAME_PATTERN.matcher(username).matches()) {
			throw new ValidationException(
					"Username must follow the format: FirstName.LastName or FirstName.LastName<number>. "
							+ "Example: John.Doe or John.Doe2");
		}

		// Validate the username parts
		String[] parts = username.split("\\.");
		if (parts.length != 2) {
			throw new ValidationException("Username must contain exactly one dot separator");
		}

		String firstName = parts[0];
		String lastNameWithSerial = parts[1];

		// Extract last name and serial number
		String lastName = lastNameWithSerial.replaceAll("\\d+$", "");
		String serial = lastNameWithSerial.substring(lastName.length());

		if (firstName.length() < 2) {
			throw new ValidationException("First name in username must be at least 2 characters long");
		}

		if (lastName.length() < 2) {
			throw new ValidationException("Last name in username must be at least 2 characters long");
		}

		// Validate serial number if present
		if (!serial.isEmpty()) {
			try {
				int serialNum = Integer.parseInt(serial);
				if (serialNum < 0) {
					throw new ValidationException("Username serial number must be positive");
				}
			}
			catch (NumberFormatException e) {
				throw new ValidationException("Invalid serial number in username: " + serial);
			}
		}
	}

	/**
	 * Validates that both first name and last name are not null or empty.
	 */
	public static void validateFullName(String firstName, String lastName) {
		validateName(firstName, "First name");
		validateName(lastName, "Last name");
	}

	public static void validateName(String name, String fieldName) {
		if (name == null || name.trim().isEmpty()) {
			throw new ValidationException(fieldName + " cannot be null");
		}
		name = name.trim();

		if (name.isEmpty()) {
			throw new ValidationException(fieldName + " empty");
		}

		if (name.length() < 2) {
			throw new ValidationException(fieldName + " must be at least 2 characters long");
		}

		if (name.length() > 50) {
			throw new ValidationException(fieldName + " must not exceed 50 characters");
		}

		if (!NAME_PATTERN.matcher(name).matches()) {
			throw new ValidationException(fieldName + " can only contain letters, spaces, hyphens, and apostrophes");
		}

	}

}
