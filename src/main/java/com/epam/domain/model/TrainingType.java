package com.epam.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TrainingType {

	private final TrainingTypeEnum trainingTypeName;

	@Setter
	@EqualsAndHashCode.Include
	private Long trainingTypeId;

	public TrainingType(TrainingTypeEnum trainingTypeName) {
		this.trainingTypeName = trainingTypeName;
	}

}