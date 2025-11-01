package com.epam.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString
public class TrainingType {

	@EqualsAndHashCode.Include
	private Long trainingTypeId;

	private String trainingNameType;

	private Long trainerId;

	private Long trainingId;

	public TrainingType(Long trainingTypeId, String trainingNameType, Long trainerId, Long trainingId) {
		if (trainingNameType == null || trainingNameType.isBlank()) {
			throw new IllegalArgumentException("Training type name cannot be null or empty");
		}

		this.trainingTypeId = trainingTypeId;
		this.trainingNameType = trainingNameType;
		this.trainerId = trainerId;
		this.trainingId = trainingId;

	}

}
