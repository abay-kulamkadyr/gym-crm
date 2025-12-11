package com.epam.infrastructure.security.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.Optional;

import com.epam.application.facade.GymFacade;
import com.epam.domain.model.Trainee;
import com.epam.domain.model.Trainer;
import com.epam.domain.model.TrainingType;
import com.epam.domain.model.TrainingTypeEnum;
import com.epam.domain.model.User;
import com.epam.domain.port.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class PasswordManagementServiceServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private GymFacade gymFacade;

    @InjectMocks
    private PasswordManagementService passwordManagementService;

    private static final String USERNAME = "john.doe";
    private static final String OLD_PASSWORD = "oldPassword123";
    private static final String NEW_PASSWORD = "newPassword456";
    private static final String ENCODED_OLD_PASSWORD = "$2a$10$encodedOldPassword";

    private Trainee trainee;
    private Trainer trainer;

    @BeforeEach
    void setUp() {
        trainee = new Trainee("john", "doe", true);
        trainee.setUsername(USERNAME);
        trainee.setPassword(ENCODED_OLD_PASSWORD);

        trainer = new Trainer("john", "doe", true, new TrainingType(TrainingTypeEnum.CARDIO));
        trainer.setUsername(USERNAME);
        trainer.setPassword(ENCODED_OLD_PASSWORD);
    }

    @Test
    @DisplayName("Should successfully change password for Trainee")
    void testChangePassword_Trainee_Success() {
        // Given
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));
        when(passwordEncoder.matches(OLD_PASSWORD, ENCODED_OLD_PASSWORD)).thenReturn(true);

        // When
        passwordManagementService.changePassword(USERNAME, OLD_PASSWORD, NEW_PASSWORD);

        // Then
        verify(userRepository).findByUsername(USERNAME);
        verify(passwordEncoder).matches(OLD_PASSWORD, ENCODED_OLD_PASSWORD);
        verify(gymFacade).updateTraineePassword(USERNAME, NEW_PASSWORD);
        verify(gymFacade, never()).updateTrainerPassword(anyString(), anyString());
    }

    @Test
    @DisplayName("Should successfully change password for Trainer")
    void testChangePassword_Trainer_Success() {
        // Given
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(trainer));
        when(passwordEncoder.matches(OLD_PASSWORD, ENCODED_OLD_PASSWORD)).thenReturn(true);

        // When
        passwordManagementService.changePassword(USERNAME, OLD_PASSWORD, NEW_PASSWORD);

        // Then
        verify(userRepository).findByUsername(USERNAME);
        verify(passwordEncoder).matches(OLD_PASSWORD, ENCODED_OLD_PASSWORD);
        verify(gymFacade).updateTrainerPassword(USERNAME, NEW_PASSWORD);
        verify(gymFacade, never()).updateTraineePassword(anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when user does not exist")
    void testChangePassword_UserNotFound() {
        // Given
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> passwordManagementService.changePassword(USERNAME, OLD_PASSWORD, NEW_PASSWORD))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found with username: " + USERNAME);

        verify(userRepository).findByUsername(USERNAME);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(gymFacade, never()).updateTraineePassword(anyString(), anyString());
        verify(gymFacade, never()).updateTrainerPassword(anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw BadCredentialsException when old password is incorrect for Trainee")
    void testChangePassword_Trainee_IncorrectOldPassword() {
        // Given
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));
        when(passwordEncoder.matches(OLD_PASSWORD, ENCODED_OLD_PASSWORD)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> passwordManagementService.changePassword(USERNAME, OLD_PASSWORD, NEW_PASSWORD))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Current password is incorrect");

        verify(userRepository).findByUsername(USERNAME);
        verify(passwordEncoder).matches(OLD_PASSWORD, ENCODED_OLD_PASSWORD);
        verify(gymFacade, never()).updateTraineePassword(anyString(), anyString());
        verify(gymFacade, never()).updateTrainerPassword(anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw BadCredentialsException when old password is incorrect for Trainer")
    void testChangePassword_Trainer_IncorrectOldPassword() {
        // Given
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(trainer));
        when(passwordEncoder.matches(OLD_PASSWORD, ENCODED_OLD_PASSWORD)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> passwordManagementService.changePassword(USERNAME, OLD_PASSWORD, NEW_PASSWORD))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Current password is incorrect");

        verify(userRepository).findByUsername(USERNAME);
        verify(passwordEncoder).matches(OLD_PASSWORD, ENCODED_OLD_PASSWORD);
        verify(gymFacade, never()).updateTraineePassword(anyString(), anyString());
        verify(gymFacade, never()).updateTrainerPassword(anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw IllegalStateException for unsupported user type")
    void testChangePassword_UnsupportedUserType() {
        // Given - Create a custom User subclass that's not Trainee or Trainer
        User unsupportedUser = new User("", "", true) {
            // Anonymous subclass for testing
        };
        unsupportedUser.setUsername(USERNAME);
        unsupportedUser.setPassword(ENCODED_OLD_PASSWORD);

        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(unsupportedUser));
        when(passwordEncoder.matches(OLD_PASSWORD, ENCODED_OLD_PASSWORD)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> passwordManagementService.changePassword(USERNAME, OLD_PASSWORD, NEW_PASSWORD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Unsupported user type");

        verify(userRepository).findByUsername(USERNAME);
        verify(passwordEncoder).matches(OLD_PASSWORD, ENCODED_OLD_PASSWORD);
        verify(gymFacade, never()).updateTraineePassword(anyString(), anyString());
        verify(gymFacade, never()).updateTrainerPassword(anyString(), anyString());
    }

    @Test
    @DisplayName("Should handle password change when old and new passwords are the same")
    void testChangePassword_SamePassword() {
        // Given
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));
        when(passwordEncoder.matches(OLD_PASSWORD, ENCODED_OLD_PASSWORD)).thenReturn(true);

        // When
        passwordManagementService.changePassword(USERNAME, OLD_PASSWORD, OLD_PASSWORD);

        // Then - Should still process the change
        verify(userRepository).findByUsername(USERNAME);
        verify(passwordEncoder).matches(OLD_PASSWORD, ENCODED_OLD_PASSWORD);
        verify(gymFacade).updateTraineePassword(USERNAME, OLD_PASSWORD);
    }

    @Test
    @DisplayName("Should handle password change with special characters")
    void testChangePassword_SpecialCharacters() {
        // Given
        String specialPassword = "P@ssw0rd!#$%^&*()";
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(trainer));
        when(passwordEncoder.matches(OLD_PASSWORD, ENCODED_OLD_PASSWORD)).thenReturn(true);

        // When
        passwordManagementService.changePassword(USERNAME, OLD_PASSWORD, specialPassword);

        // Then
        verify(userRepository).findByUsername(USERNAME);
        verify(passwordEncoder).matches(OLD_PASSWORD, ENCODED_OLD_PASSWORD);
        verify(gymFacade).updateTrainerPassword(USERNAME, specialPassword);
    }

    @Test
    @DisplayName("Should verify password encoder is called with correct parameters")
    void testChangePassword_PasswordEncoderCalledCorrectly() {
        // Given
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));
        when(passwordEncoder.matches(OLD_PASSWORD, ENCODED_OLD_PASSWORD)).thenReturn(true);

        // When
        passwordManagementService.changePassword(USERNAME, OLD_PASSWORD, NEW_PASSWORD);

        // Then
        verify(passwordEncoder).matches(OLD_PASSWORD, ENCODED_OLD_PASSWORD);
    }

    @Test
    @DisplayName("Should call gymFacade only once for Trainee")
    void testChangePassword_GymFacadeCalledOnceForTrainee() {
        // Given
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));
        when(passwordEncoder.matches(OLD_PASSWORD, ENCODED_OLD_PASSWORD)).thenReturn(true);

        // When
        passwordManagementService.changePassword(USERNAME, OLD_PASSWORD, NEW_PASSWORD);

        // Then
        verify(gymFacade, times(1)).updateTraineePassword(USERNAME, NEW_PASSWORD);
        verifyNoMoreInteractions(gymFacade);
    }

    @Test
    @DisplayName("Should call gymFacade only once for Trainer")
    void testChangePassword_GymFacadeCalledOnceForTrainer() {
        // Given
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(trainer));
        when(passwordEncoder.matches(OLD_PASSWORD, ENCODED_OLD_PASSWORD)).thenReturn(true);

        // When
        passwordManagementService.changePassword(USERNAME, OLD_PASSWORD, NEW_PASSWORD);

        // Then
        verify(gymFacade, times(1)).updateTrainerPassword(USERNAME, NEW_PASSWORD);
        verifyNoMoreInteractions(gymFacade);
    }
}
