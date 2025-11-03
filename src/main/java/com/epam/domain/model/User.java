package com.epam.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@ToString
abstract class User {

	private String firstName;

	private String lastName;

	private String username;

	private String password;

	private boolean active;

	protected User(String firstName, String lastName) {
		if (firstName == null || firstName.isBlank()) {
			throw new IllegalArgumentException("First name cannot be null or empty");
		}

		if (lastName == null || lastName.isBlank()) {
			throw new IllegalArgumentException("Last name cannot be null or empty");
		}

		this.firstName = firstName;
		this.lastName = lastName;
	}

}
