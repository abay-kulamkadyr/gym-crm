package com.epam.application.service;

import com.epam.application.Credentials;
import com.epam.application.request.CreateTrainerProfileRequest;
import com.epam.application.request.UpdateTrainerProfileRequest;
import com.epam.domain.model.Trainee;
import com.epam.domain.model.Trainer;

import java.util.List;

public interface TrainerService extends UserService<Trainer, CreateTrainerProfileRequest, UpdateTrainerProfileRequest> {

	List<Trainee> getTrainees(Credentials credentials);

}
