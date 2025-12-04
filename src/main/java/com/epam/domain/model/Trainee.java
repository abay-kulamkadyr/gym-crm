package com.epam.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Trainee extends User {

	@EqualsAndHashCode.Include
	private Long traineeId;

	private LocalDate dob;

	private String address;

	public Trainee(String firstName, String lastName, Boolean active) {
		super(firstName, lastName, active);
	}

}