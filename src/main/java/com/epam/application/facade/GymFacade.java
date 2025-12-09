package com.epam.application.facade;

import com.epam.application.request.CreateTraineeProfileRequest;
import com.epam.application.request.CreateTrainerProfileRequest;
import com.epam.application.request.CreateTrainingRequest;
import com.epam.application.request.UpdateTraineeProfileRequest;
import com.epam.application.request.UpdateTrainerProfileRequest;
import com.epam.domain.TrainingFilter;
import com.epam.domain.model.Trainee;
import com.epam.domain.model.Trainer;
import com.epam.domain.model.Training;
import com.epam.domain.model.TrainingType;

import java.util.List;

public interface GymFacade {

	Trainee createTraineeProfile(CreateTraineeProfileRequest request);

	Trainee updateTraineeProfile(UpdateTraineeProfileRequest request);

	void updateTraineePassword(String traineeUsername, String newPassword);

	void toggleTraineeActiveStatus(String traineeUsername);

	void deleteTraineeProfile(String traineeUsername);

	Trainee getTraineeByUsername(String traineeUsername);

	void updateTraineeTrainersList(String traineeUsername, List<String> usernames);

	List<Trainer> getTraineeUnassignedTrainers(String traineeUsername);

	List<Training> getTraineeTrainings(String traineeUsername, TrainingFilter filter);

	Trainer createTrainerProfile(CreateTrainerProfileRequest request);

	Trainer updateTrainerProfile(UpdateTrainerProfileRequest request);

	void updateTrainerPassword(String trainerUsername, String newPassword);

	void toggleTrainerActiveStatus(String trainerUsername);

	void deleteTrainerProfile(String trainerUsername);

	Trainer getTrainerByUsername(String trainerUsername);

	List<Training> getTrainerTrainings(String trainerUsername, TrainingFilter filter);

	Training createTraining(CreateTrainingRequest request);

	List<Trainer> getTraineeTrainers(String traineeUsername);

	List<Trainee> getTrainerTrainees(String trainerUsername);

	List<TrainingType> getTrainingTypes();

}
