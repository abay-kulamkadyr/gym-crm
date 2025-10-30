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
	private long trainingTypeId;

	private String trainingNameType;

	private long trainerId;

	private long trainingId;

	public TrainingType(long trainingTypeId, String trainingNameType, long trainerId, long trainingId) {
		if (trainingNameType == null || trainingNameType.isBlank()) {
			throw new IllegalArgumentException("Training type name cannot be null or empty");
		}

		if (trainerId <= 0) {
			throw new IllegalArgumentException("TrainingType must have a valid trainer ID");
		}

		if (trainingId <= 0) {
			throw new IllegalArgumentException("TrainingType must have a valid training ID");
		}

		this.trainingTypeId = trainingTypeId;
		this.trainingNameType = trainingNameType;
		this.trainerId = trainerId;
		this.trainingId = trainingId;

	}

}
