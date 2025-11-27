package com.epam.application.service;

import com.epam.application.Credentials;
import com.epam.application.request.CreateTraineeProfileRequest;
import com.epam.application.request.UpdateTraineeProfileRequest;
import com.epam.domain.model.Trainee;
import com.epam.domain.model.Trainer;
import java.util.List;

public interface TraineeService extends UserService<Trainee, CreateTraineeProfileRequest, UpdateTraineeProfileRequest> {

	List<Trainer> getUnassignedTrainers(Credentials credentials);

	List<Trainer> getTrainers(Credentials credentials);

	void updateTrainersList(Credentials credentials, List<String> trainerUsernames);

}
