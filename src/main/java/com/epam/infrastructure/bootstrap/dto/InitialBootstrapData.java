package com.epam.infrastructure.bootstrap.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InitialBootstrapData {

	private List<TrainingTypeDTO> trainingTypes = new ArrayList<>();

	private List<UserDTO> users = new ArrayList<>();

	private List<TrainerDTO> trainers = new ArrayList<>();

	private List<TraineeDTO> trainees = new ArrayList<>();

	private List<TrainingDTO> trainings = new ArrayList<>();

}
