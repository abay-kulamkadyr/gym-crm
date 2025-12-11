package com.epam.application.service.impl;

import java.util.List;

import com.epam.application.event.TrainerRegisteredEvent;
import com.epam.application.exception.ValidationException;
import com.epam.application.request.CreateTrainerProfileRequest;
import com.epam.application.request.UpdateTrainerProfileRequest;
import com.epam.application.service.TrainerService;
import com.epam.application.util.CredentialsUtil;
import com.epam.domain.model.Trainee;
import com.epam.domain.model.Trainer;
import com.epam.domain.model.TrainingType;
import com.epam.domain.model.TrainingTypeEnum;
import com.epam.domain.port.TrainerRepository;
import com.epam.domain.port.TrainingTypeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
public class TrainerServiceImpl implements TrainerService {

    private TrainerRepository trainerRepository;

    private TrainingTypeRepository trainingTypeRepository;

    private ApplicationEventPublisher applicationEventPublisher;

    private PasswordEncoder passwordEncoder;

    @Autowired
    void setTrainerRepository(TrainerRepository trainerRepository) {
        this.trainerRepository = trainerRepository;
    }

    @Autowired
    void setTrainingTypeRepository(TrainingTypeRepository trainingTypeRepository) {
        this.trainingTypeRepository = trainingTypeRepository;
    }

    @Autowired
    void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Autowired
    void setApplicationEventPublisher(ApplicationEventPublisher publisher) {
        this.applicationEventPublisher = publisher;
    }

    @Override
    public Trainer createProfile(CreateTrainerProfileRequest request) {
        TrainingType specialization = findTrainingTypeOrThrow(request.specialization());

        Trainer trainer = new Trainer(request.firstName(), request.lastName(), request.active(), specialization);

        String username = CredentialsUtil
                .generateUniqueUsername(
                    trainer.getFirstName(),
                    trainer.getLastName(),
                    trainerRepository::findLatestUsername);
        String password = CredentialsUtil.generateRandomPassword(10);

        trainer.setUsername(username);
        trainer.setPassword(passwordEncoder.encode(password));

        applicationEventPublisher.publishEvent(new TrainerRegisteredEvent(trainer.getUserId()));
        Trainer savedTrainer = trainerRepository.save(trainer);
        savedTrainer.setPassword(password);
        return savedTrainer;
    }

    @Override
    public Trainer updateProfile(UpdateTrainerProfileRequest request) {
        Trainer trainer = findTrainerByUsernameOrThrow(request.username());

        request.firstName().ifPresent(newFirstName -> {
            CredentialsUtil.validateName(newFirstName, "First name");
            trainer.setFirstName(newFirstName);
        });

        request.lastName().ifPresent(newLastName -> {
            CredentialsUtil.validateName(newLastName, "Last name");
            trainer.setLastName(newLastName);
        });

        request.password().ifPresent(newPassword -> {
            validateNewPassword(newPassword);
            trainer.setPassword(passwordEncoder.encode(newPassword));
        });

        request.active().ifPresent(trainer::setActive);

        request.specialization().ifPresent(newSpecialization -> {
            TrainingType trainingType =
                    trainingTypeRepository.findByTrainingTypeName(newSpecialization).orElseThrow(() -> {
                        log.error("TrainingType not found: {}", newSpecialization);
                        return new EntityNotFoundException(
                                String.format("TrainingType with name '%s' not found", newSpecialization));
                    });

            trainer.setSpecialization(trainingType);
        });

        Trainer savedTrainer = trainerRepository.save(trainer);
        request.password().ifPresent(savedTrainer::setPassword);
        return savedTrainer;
    }

    @Override
    public void updatePassword(String username, String newPassword) {
        validateNewPassword(newPassword);
        Trainer trainer = findTrainerByUsernameOrThrow(username);
        trainer.setPassword(passwordEncoder.encode(newPassword));
        trainerRepository.save(trainer);
    }

    @Override
    public void toggleActiveStatus(String username) {
        Trainer trainer = findTrainerByUsernameOrThrow(username);

        boolean oldStatus = trainer.getActive();
        boolean newStatus = !oldStatus;
        trainer.setActive(newStatus);

        trainerRepository.save(trainer);
    }

    @Override
    public void deleteProfile(String username) {
        findTrainerByUsernameOrThrow(username);
        trainerRepository.deleteByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public Trainer getProfileByUsername(String username) {
        return findTrainerByUsernameOrThrow(username);
    }

    @Override
    public List<Trainee> getTrainees(String username) {
        findTrainerByUsernameOrThrow(username);
        return trainerRepository.getTrainees(username);
    }

    private Trainer findTrainerByUsernameOrThrow(String username) {
        return trainerRepository.findByUsername(username).orElseThrow(() -> {
            log.error("Trainer not found with username: {}", username);
            return new EntityNotFoundException(String.format("Trainer not found with username: %s", username));
        });
    }

    private TrainingType findTrainingTypeOrThrow(TrainingTypeEnum trainingType) {
        return trainingTypeRepository.findByTrainingTypeName(trainingType).orElseThrow(() -> {
            log.error("TrainingType not found with type: {}", trainingType);
            return new EntityNotFoundException(String.format("TrainingType not found with type: %s", trainingType));
        });
    }

    private void validateNewPassword(String password) {
        if (password == null) {
            throw new ValidationException("New password cannot be null");
        }
        CredentialsUtil.validatePassword(password);
    }

}
