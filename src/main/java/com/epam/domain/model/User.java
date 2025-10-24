package com.epam.domain.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
abstract class User {

	private String firstName;

	private String lastName;

	private String username;

	private String password;

	private boolean active;

	protected User(String firstName, String lastName) {
		this.firstName = firstName;
		this.lastName = lastName;
	}

}
