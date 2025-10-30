package com.epam.domain.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Duration;
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
public class Training {

	@EqualsAndHashCode.Include
	private long trainingId;

	private long traineeId;

	private long trainerId;

	private long trainingTypeId;

	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate trainingDate;

	@JsonFormat(shape = JsonFormat.Shape.STRING)
	private Duration trainingDuration;

	public Training(long trainingId, long trainerId, long traineeId, LocalDate trainingDate,
			Duration trainingDuration) {
		if (trainingId <= 0) {
			throw new IllegalArgumentException("Training must have a valid training ID");
		}

		if (trainerId <= 0) {
			throw new IllegalArgumentException("Training must have a valid trainer ID");
		}

		if (traineeId <= 0) {
			throw new IllegalArgumentException("Training must have a valid trainee ID");
		}

		if (trainingDate == null) {
			throw new IllegalArgumentException("Training date cannot be null");
		}

		if (trainingDuration == null || trainingDuration.isZero() || trainingDuration.isNegative()) {
			throw new IllegalArgumentException("Training duration must be positive");
		}

		this.trainingId = trainingId;
		this.trainerId = trainerId;
		this.traineeId = traineeId;
		this.trainingDate = trainingDate;
		this.trainingDuration = trainingDuration;
	}

}
