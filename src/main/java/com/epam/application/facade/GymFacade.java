package com.epam.application.facade;

import com.epam.application.Credentials;
import com.epam.application.request.CreateTraineeProfileRequest;
import com.epam.application.request.CreateTrainerProfileRequest;
import com.epam.application.request.CreateTrainingRequest;
import com.epam.application.request.UpdateTraineeProfileRequest;
import com.epam.application.request.UpdateTrainerProfileRequest;
import com.epam.domain.TrainingFilter;
import com.epam.domain.model.Trainee;
import com.epam.domain.model.Trainer;
import com.epam.domain.model.Training;
import java.util.List;
import java.util.Optional;

public interface GymFacade {

	Trainee createTraineeProfile(CreateTraineeProfileRequest request);

	Trainee updateTraineeProfile(UpdateTraineeProfileRequest request);

	void updateTraineePassword(Credentials credentials, String newPassword);

	void toggleTraineeActiveStatus(Credentials credentials);

	void deleteTraineeProfile(Credentials credentials);

	Optional<Trainee> findTraineeByUsername(Credentials credentials);

	void updateTraineeTrainersList(Credentials credentials, List<String> usernames);

	List<Trainer> getTraineeUnassignedTrainers(Credentials credentials);

	List<Training> getTraineeTrainings(Credentials credentials, TrainingFilter filter);

	Trainer createTrainerProfile(CreateTrainerProfileRequest request);

	Trainer updateTrainerProfile(UpdateTrainerProfileRequest request);

	void updateTrainerPassword(Credentials credentials, String newPassword);

	void toggleTrainerActiveStatus(Credentials credentials);

	void deleteTrainerProfile(Credentials credentials);

	Optional<Trainer> findTrainerByUsername(Credentials credentials);

	List<Training> getTrainerTrainings(Credentials credentials, TrainingFilter filter);

	void createTraining(CreateTrainingRequest request);

}
