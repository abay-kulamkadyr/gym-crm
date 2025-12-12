package com.epam.application.service;

import java.util.List;

import com.epam.application.request.CreateTrainerProfileRequest;
import com.epam.application.request.UpdateTrainerProfileRequest;
import com.epam.domain.model.Trainee;
import com.epam.domain.model.Trainer;

public interface TrainerService extends UserService<Trainer, CreateTrainerProfileRequest, UpdateTrainerProfileRequest> {

	List<Trainee> getTrainees(String username);

}
