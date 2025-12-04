package com.epam.infrastructure.bootstrap.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingDTO {

	private String name;

	private LocalDateTime date;

	private Integer durationMinutes;

	private String traineeUsername; // Reference to Trainee

	private String trainerUsername; // Reference to Trainer

	private String trainingType; // Reference to TrainingType

}
