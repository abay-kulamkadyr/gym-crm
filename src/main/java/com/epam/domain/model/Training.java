package com.epam.domain.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Duration;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class Training {

	private long trainingId;

	private long traineeId;

	private long trainerId;

	private long trainingTypeId;

	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate trainingDate;

	@JsonFormat(shape = JsonFormat.Shape.STRING)
	private Duration trainingDuration;

	public Training(long trainingId, long trainerId, long traineeId, LocalDate trainingDate, Duration duration) {
		this.trainingId = trainingId;
		this.trainerId = trainerId;
		this.traineeId = traineeId;
		this.trainingDate = trainingDate;
		this.trainingDuration = duration;
	}

}
