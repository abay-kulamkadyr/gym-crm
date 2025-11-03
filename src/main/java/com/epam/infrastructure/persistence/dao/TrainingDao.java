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

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Builder
public class TrainingDao {

	@EqualsAndHashCode.Include
	private Long trainingId;

	private Long traineeId;

	private Long trainerId;

	private Long trainingTypeId;

	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate trainingDate;

	@JsonFormat(shape = JsonFormat.Shape.STRING)
	private Duration trainingDuration;

}
