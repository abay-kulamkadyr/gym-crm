package com.epam.application.service;

import com.epam.application.Credentials;
import com.epam.application.request.CreateTrainingRequest;
import com.epam.domain.TrainingFilter;
import com.epam.domain.model.Training;

import java.util.List;

public interface TrainingService {

	Training create(CreateTrainingRequest request);

	List<Training> getTraineeTrainings(Credentials credentials, TrainingFilter filter);

	List<Training> getTrainerTrainings(Credentials credentials, TrainingFilter filter);

}
