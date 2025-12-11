package com.epam.infrastructure.bootstrap.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainerDTO {

	private String username; // Reference to User

	private String specialization; // Reference to TrainingType by name

}
