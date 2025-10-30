package com.epam.domain.model;

import java.time.LocalDate;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor // for jackson
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString
public class Trainee extends User {

	@EqualsAndHashCode.Include
	private long userId;

	private LocalDate dob;

	private String address;

	private long trainingId;

	public Trainee(long userId, String firstName, String lastName, LocalDate dob) {
		super(firstName, lastName);
		if (userId <= 0) {
			throw new IllegalArgumentException("UserId cannot be less or equal to 0");
		}
		if (dob == null || dob.isAfter(LocalDate.now())) {
			throw new IllegalArgumentException("Date of birth is not valid");
		}
		this.userId = userId;
		this.dob = dob;
	}

}
