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
	private Long userId;

	private LocalDate dob;

	private String address;

	private Long trainingId;

	public Trainee(Long userId, String firstName, String lastName, LocalDate dob) {
		super(firstName, lastName);

		if (dob == null || dob.isAfter(LocalDate.now())) {
			throw new IllegalArgumentException("Date of birth is not valid");
		}
		this.userId = userId;
		this.dob = dob;
	}

}
