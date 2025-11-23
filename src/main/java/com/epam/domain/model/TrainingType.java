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

	private final TrainingTypeEnum trainingTypeName;

	public TrainingType(TrainingTypeEnum trainingTypeName) {
		this.trainingTypeName = trainingTypeName;
	}

}