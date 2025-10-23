package com.epam.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class TrainingType {

	private long id;

	private String trainingNameType;

	private long trainerId;

	private long trainingId;

	public TrainingType(long id, String trainingNameType, long trainerId, long trainingId) {
		this.id = id;
		this.trainingNameType = trainingNameType;
		this.trainerId = trainerId;
		this.trainingId = trainingId;

	}

}
