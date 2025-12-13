package com.epam.infrastructure.security.service;

import com.epam.application.facade.GymFacade;
import com.epam.domain.model.Trainee;
import com.epam.domain.model.Trainer;
import com.epam.domain.model.User;
import com.epam.domain.port.UserRepository;
import com.epam.infrastructure.security.port.in.PasswordManagementUseCase;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class PasswordManagementService implements PasswordManagementUseCase {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final GymFacade gymFacade;

    @Autowired
    PasswordManagementService(UserRepository userRepository, PasswordEncoder encoder, GymFacade facade) {
        this.userRepository = userRepository;
        this.passwordEncoder = encoder;
        this.gymFacade = facade;
    }

    @Override
    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        log.info("Password change request for user: {}", username);

        User user = userRepository.findByUsername(username).orElseThrow(() -> {
            log.error("User not found: {}", username);
            return new EntityNotFoundException(String.format("User not found with username: %s", username));
        });

        // Verify old password
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            log.warn("Invalid old password provided for user: {}", username);
            throw new BadCredentialsException("Current password is incorrect");
        }

        updatePasswordByUserType(user, newPassword);
        log.info("Password successfully changed for user: {}", username);
    }

    private void updatePasswordByUserType(User user, String newPassword) {
        if (user instanceof Trainee) {
            gymFacade.updateTraineePassword(user.getUsername(), newPassword);
        }
        else if (user instanceof Trainer) {
            gymFacade.updateTrainerPassword(user.getUsername(), newPassword);
        }
        else {
            log.error("Unsupported user type: {}", user.getClass().getSimpleName());
            throw new IllegalStateException(
                    String.format("Unsupported user type: %s", user.getClass().getSimpleName()));
        }
    }

}
