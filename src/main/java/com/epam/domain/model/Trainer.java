package com.epam.domain.model;

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
public class Trainer extends User {

	@EqualsAndHashCode.Include
	private Long userId;

	private String specialization;

	private Long trainingId;

	private Long trainingTypeId;

	public Trainer(Long userId, String firstName, String lastName, String specialization) {
		super(firstName, lastName);
		if (specialization == null || specialization.isEmpty()) {
			throw new IllegalArgumentException("Specialization cannot be null or empty");
		}
		this.userId = userId;
		this.specialization = specialization;
	}

}
