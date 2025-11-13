package com.epam.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TrainingType {

	@Setter
	@EqualsAndHashCode.Include
	private Long trainingTypeId;

	private final String trainingTypeName;

	public TrainingType(String trainingTypeName) {
		this.trainingTypeName = trainingTypeName;
	}

}