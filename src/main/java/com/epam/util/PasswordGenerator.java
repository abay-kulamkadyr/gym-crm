package com.epam.util;

import java.security.SecureRandom;

public final class PasswordGenerator {

	private PasswordGenerator() {

	}

	private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

	private static final SecureRandom RANDOM = new SecureRandom();

	public static String generate(int length) {
		StringBuilder sb = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
		}
		return sb.toString();
	}

}
