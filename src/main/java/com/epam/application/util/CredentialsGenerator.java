package com.epam.application.util;

import java.security.SecureRandom;
import java.util.Optional;
import java.util.function.Function;

public class CredentialsGenerator {

	private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

	private static final SecureRandom RANDOM = new SecureRandom();

	private CredentialsGenerator() {
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
			sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
		}
		return sb.toString();
	}

}
