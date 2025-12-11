package com.epam.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class User {

	@EqualsAndHashCode.Include
	private Long userId;

	private String firstName;

	private String lastName;

	private String username;

	private String password;

	private Boolean active;

	protected User(String firstName, String lastName, Boolean active) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.active = active;
	}

}
