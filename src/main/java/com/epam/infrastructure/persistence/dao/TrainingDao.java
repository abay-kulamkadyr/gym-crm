package com.epam.infrastructure.persistence.dao;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Duration;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class TrainingDao {

	@EqualsAndHashCode.Include
	private long trainingId;

	private long traineeId;

	private long trainerId;

	private long trainingTypeId;

	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate trainingDate;

	@JsonFormat(shape = JsonFormat.Shape.STRING)
	private Duration trainingDuration;

}
