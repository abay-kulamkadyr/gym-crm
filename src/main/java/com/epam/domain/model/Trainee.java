package com.epam.domain.model;

import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class Trainee extends User {

	private long userId;

	private LocalDate dob;

	private String address;

	private long trainingId;

	public Trainee(long userId, String firstName, String lastName, LocalDate dob) {
		super(firstName, lastName);
		this.userId = userId;
		this.dob = dob;
	}

}
