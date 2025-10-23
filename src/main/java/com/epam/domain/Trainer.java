package com.epam.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class Trainer extends User {

	private long userId;

	private String specialization;

	private long trainingId;

	private long trainingTypeId;

	public Trainer(long userId, String firstName, String lastName, String specialization) {
		super(firstName, lastName);
		this.userId = userId;
		this.specialization = specialization;
	}

}
