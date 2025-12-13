package com.epam.application.service;

import java.util.List;

import com.epam.application.request.CreateTraineeProfileRequest;
import com.epam.application.request.UpdateTraineeProfileRequest;
import com.epam.domain.model.Trainee;
import com.epam.domain.model.Trainer;

public interface TraineeService extends UserService<Trainee, CreateTraineeProfileRequest, UpdateTraineeProfileRequest> {

    List<Trainer> getUnassignedTrainers(String username);

    List<Trainer> getTrainers(String username);

    void updateTrainersList(String username, List<String> trainerUsernames);

}
