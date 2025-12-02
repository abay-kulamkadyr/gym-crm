package com.epam.infrastructure.bootstrap.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TraineeDTO {

	private String username; // Reference to User

	private LocalDate dateOfBirth;

	private String address;

	private List<String> trainerUsernames = new ArrayList<>(); // References to Trainers

}
