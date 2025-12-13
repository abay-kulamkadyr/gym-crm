package com.epam.application.facade;

import java.util.List;

import com.epam.application.request.CreateTraineeProfileRequest;
import com.epam.application.request.CreateTrainerProfileRequest;
import com.epam.application.request.CreateTrainingRequest;
import com.epam.application.request.UpdateTraineeProfileRequest;
import com.epam.application.request.UpdateTrainerProfileRequest;
import com.epam.application.service.TraineeService;
import com.epam.application.service.TrainerService;
import com.epam.application.service.TrainingService;
import com.epam.domain.TrainingFilter;
import com.epam.domain.model.Trainee;
import com.epam.domain.model.Trainer;
import com.epam.domain.model.Training;
import com.epam.domain.model.TrainingType;
import com.epam.domain.port.TrainingTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class GymFacadeImpl implements GymFacade {

    private final TraineeService traineeService;

    private final TrainerService trainerService;

    private final TrainingService trainingService;

    private final TrainingTypeRepository trainingTypeRepository;

    @Autowired
    public GymFacadeImpl(
            TraineeService traineeService,
            TrainerService trainerService,
            TrainingService trainingService,
            TrainingTypeRepository trainingTypeRepository) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
        this.trainingTypeRepository = trainingTypeRepository;
    }

    @Override
    public Trainee createTraineeProfile(CreateTraineeProfileRequest request) {
        return traineeService.createProfile(request);
    }

    @Override
    public Trainee updateTraineeProfile(UpdateTraineeProfileRequest request) {
        return traineeService.updateProfile(request);
    }

    @Override
    public void updateTraineePassword(String traineeUsername, String newPassword) {
        traineeService.updatePassword(traineeUsername, newPassword);
    }

    @Override
    public void toggleTraineeActiveStatus(String traineeUsername) {
        traineeService.toggleActiveStatus(traineeUsername);
    }

    @Override
    public void deleteTraineeProfile(String traineeUsername) {
        traineeService.deleteProfile(traineeUsername);
    }

    @Override
    public Trainee getTraineeByUsername(String traineeUsername) {
        return traineeService.getProfileByUsername(traineeUsername);
    }

    @Override
    public void updateTraineeTrainersList(String traineeUsername, List<String> usernames) {
        traineeService.updateTrainersList(traineeUsername, usernames);
    }

    @Override
    public List<Trainer> getTraineeUnassignedTrainers(String traineeUsername) {
        return traineeService.getUnassignedTrainers(traineeUsername);
    }

    @Override
    public List<Training> getTraineeTrainings(String traineeUsername, TrainingFilter filter) {
        return trainingService.getTraineeTrainings(traineeUsername, filter);
    }

    @Override
    public Trainer createTrainerProfile(CreateTrainerProfileRequest request) {
        return trainerService.createProfile(request);
    }

    @Override
    public Trainer updateTrainerProfile(UpdateTrainerProfileRequest request) {
        return trainerService.updateProfile(request);
    }

    @Override
    public void updateTrainerPassword(String trainerUsername, String newPassword) {
        trainerService.updatePassword(trainerUsername, newPassword);
    }

    @Override
    public void toggleTrainerActiveStatus(String trainerUsername) {
        trainerService.toggleActiveStatus(trainerUsername);
    }

    @Override
    public void deleteTrainerProfile(String trainerUsername) {
        trainerService.deleteProfile(trainerUsername);
    }

    @Override
    public Trainer getTrainerByUsername(String trainerUsername) {
        return trainerService.getProfileByUsername(trainerUsername);
    }

    @Override
    public List<Training> getTrainerTrainings(String trainerUsername, TrainingFilter filter) {
        return trainingService.getTrainerTrainings(trainerUsername, filter);
    }

    @Override
    public Training createTraining(CreateTrainingRequest request) {
        return trainingService.create(request);
    }

    @Override
    public List<Trainer> getTraineeTrainers(String traineeUsername) {
        return traineeService.getTrainers(traineeUsername);
    }

    @Override
    public List<Trainee> getTrainerTrainees(String trainerUsername) {
        return trainerService.getTrainees(trainerUsername);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainingType> getTrainingTypes() {
        return trainingTypeRepository.getTrainingTypes();
    }

}
