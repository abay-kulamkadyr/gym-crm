package com.epam.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.epam.application.Credentials;
import com.epam.application.exception.AuthenticationException;
import com.epam.application.exception.ValidationException;
import com.epam.application.request.CreateTrainerProfileRequest;
import com.epam.application.request.UpdateTrainerProfileRequest;
import com.epam.application.service.impl.AuthenticationServiceImpl;
import com.epam.application.service.impl.TrainerServiceImpl;
import com.epam.domain.model.Trainer;
import com.epam.domain.model.TrainingType;
import com.epam.domain.model.TrainingTypeEnum;
import com.epam.domain.repository.TrainerRepository;
import com.epam.domain.repository.TrainingTypeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class TrainerServiceImplTest {

	@Mock
	private TrainerRepository trainerRepository;

	@Mock
	private TrainingTypeRepository trainingTypeRepository;

	@Mock
	private AuthenticationServiceImpl authenticationService;

	@InjectMocks
	private TrainerServiceImpl trainerService;

	private Trainer testTrainer;

	private Credentials testCredentials;

	private TrainingType cardioType;

	@BeforeEach
	void setUp() {
		cardioType = new TrainingType(TrainingTypeEnum.CARDIO);
		cardioType.setTrainingTypeId(1L);

		testTrainer = new Trainer("Alice", "Johnson", true, cardioType);
		testTrainer.setTrainerId(1L);
		testTrainer.setUserId(10L);
		testTrainer.setUsername("Alice.Johnson");
		testTrainer.setPassword("password123");

		testCredentials = new Credentials("Alice.Johnson", "password123");
	}

	@Test
	void createProfile_shouldGenerateUsernameAndPassword() {
		// Given
		CreateTrainerProfileRequest request = new CreateTrainerProfileRequest("Alice", "Johnson", true,
				cardioType.getTrainingTypeName());

		when(trainerRepository.findLatestUsername("Alice.Johnson")).thenReturn(Optional.empty());
		when(trainerRepository.save(any(Trainer.class))).thenReturn(testTrainer);

		when(trainingTypeRepository.findByTrainingTypeName(cardioType.getTrainingTypeName()))
			.thenReturn(Optional.of(cardioType));
		// When
		Trainer created = trainerService.createProfile(request);

		// Then
		assertThat(created).isNotNull();
		assertThat(created.getUsername()).isEqualTo("Alice.Johnson");
		assertThat(created.getPassword()).isNotNull();
		assertThat(created.getSpecialization().getTrainingTypeName()).isEqualTo(TrainingTypeEnum.CARDIO);
		verify(trainerRepository).save(any(Trainer.class));
	}

	@Test
	void createProfile_shouldGenerateUniqueUsernameWhenDuplicateExists() {
		// Given
		CreateTrainerProfileRequest request = new CreateTrainerProfileRequest("Alice", "Johnson", true,
				cardioType.getTrainingTypeName());

		when(trainerRepository.findLatestUsername("Alice.Johnson")).thenReturn(Optional.of("Alice.Johnson1"));

		Trainer trainerWithSerial = new Trainer("Alice", "Johnson", true, cardioType);
		trainerWithSerial.setUsername("Alice.Johnson2");
		trainerWithSerial.setPassword("generatedPass");

		when(trainerRepository.save(any(Trainer.class))).thenReturn(trainerWithSerial);
		when(trainingTypeRepository.findByTrainingTypeName(cardioType.getTrainingTypeName()))
			.thenReturn(Optional.of(cardioType));
		// When
		Trainer created = trainerService.createProfile(request);

		// Then
		assertThat(created.getUsername()).isEqualTo("Alice.Johnson2");
		verify(trainerRepository).save(any(Trainer.class));
	}

	@Test
	void updateProfile_shouldUpdateAllProvidedFields() {
		// Given
		TrainingType yogaType = new TrainingType(TrainingTypeEnum.YOGA);
		yogaType.setTrainingTypeId(2L);

		UpdateTrainerProfileRequest request = new UpdateTrainerProfileRequest(testCredentials, Optional.of("Bob"),
				Optional.of("Smith"), Optional.of("Bob.Smith"), Optional.of("newpassword123"), Optional.of(false),
				Optional.of(TrainingTypeEnum.YOGA));

		when(authenticationService.authenticateTrainer(testCredentials)).thenReturn(true);
		when(trainerRepository.findByUsername("Alice.Johnson")).thenReturn(Optional.of(testTrainer));
		when(trainerRepository.findByUsername("Bob.Smith")).thenReturn(Optional.empty());
		when(trainingTypeRepository.findByTrainingTypeName(TrainingTypeEnum.YOGA)).thenReturn(Optional.of(yogaType));

		Trainer updatedTrainer = new Trainer("Bob", "Smith", false, yogaType);
		updatedTrainer.setUsername("Bob.Smith");
		when(trainerRepository.save(any(Trainer.class))).thenReturn(updatedTrainer);

		// When
		Trainer updated = trainerService.updateProfile(request);

		// Then
		assertThat(updated).isNotNull();
		verify(trainerRepository).save(any(Trainer.class));
	}

	@Test
	void updateProfile_shouldThrowAuthenticationException_whenCredentialsInvalid() {
		// Given
		UpdateTrainerProfileRequest request = new UpdateTrainerProfileRequest(testCredentials, Optional.of("Bob"),
				Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());

		when(authenticationService.authenticateTrainer(testCredentials)).thenReturn(false);

		// When/Then
		assertThatThrownBy(() -> trainerService.updateProfile(request)).isInstanceOf(AuthenticationException.class);
	}

	@Test
	void updateProfile_shouldThrowEntityNotFoundException_whenSpecializationIsNotValid() {
		// Given
		assertThatThrownBy(() -> {
			new UpdateTrainerProfileRequest(testCredentials, Optional.empty(), Optional.empty(), Optional.empty(),
					Optional.empty(), Optional.empty(), Optional.of(TrainingTypeEnum.fromString("Non existent")));
		}).isInstanceOf(EntityNotFoundException.class);

	}

	@Test
	void updateProfile_shouldThrowValidationException_whenNewUsernameAlreadyExists() {
		// Given
		UpdateTrainerProfileRequest request = new UpdateTrainerProfileRequest(testCredentials, Optional.empty(),
				Optional.empty(), Optional.of("Bob.Smith"), Optional.empty(), Optional.empty(), Optional.empty());

		Trainer existingTrainer = new Trainer("Bob", "Smith", true, cardioType);
		existingTrainer.setUsername("Bob.Smith");

		when(authenticationService.authenticateTrainer(testCredentials)).thenReturn(true);
		when(trainerRepository.findByUsername("Alice.Johnson")).thenReturn(Optional.of(testTrainer));
		when(trainerRepository.findByUsername("Bob.Smith")).thenReturn(Optional.of(existingTrainer));

		// When/Then
		assertThatThrownBy(() -> trainerService.updateProfile(request)).isInstanceOf(ValidationException.class)
			.hasMessageContaining("already exists");
	}

	@Test
	void updatePassword_shouldUpdatePassword() {
		// Given
		String newPassword = "newSecurePass123";
		when(authenticationService.authenticateTrainer(testCredentials)).thenReturn(true);
		when(trainerRepository.findByUsername("Alice.Johnson")).thenReturn(Optional.of(testTrainer));
		when(trainerRepository.save(any(Trainer.class))).thenReturn(testTrainer);

		// When
		trainerService.updatePassword(testCredentials, newPassword);

		// Then
		verify(trainerRepository).save(any(Trainer.class));
	}

	@Test
	void updatePassword_shouldThrowAuthenticationException_whenInvalidCredentials() {
		// Given
		when(authenticationService.authenticateTrainer(testCredentials)).thenReturn(false);

		// When/Then
		assertThatThrownBy(() -> trainerService.updatePassword(testCredentials, "newPassword"))
			.isInstanceOf(AuthenticationException.class);
	}

	@Test
	void updatePassword_shouldThrowValidationException_whenNewPasswordTooShort() {
		// Give/When/Then
		assertThatThrownBy(() -> trainerService.updatePassword(testCredentials, "short"))
			.isInstanceOf(ValidationException.class)
			.hasMessageContaining("10 characters");
	}

	@Test
	void toggleActiveStatus_shouldToggleFromTrueToFalse() {
		// Given
		when(authenticationService.authenticateTrainer(testCredentials)).thenReturn(true);
		when(trainerRepository.findByUsername("Alice.Johnson")).thenReturn(Optional.of(testTrainer));
		when(trainerRepository.save(any(Trainer.class))).thenReturn(testTrainer);

		// When
		trainerService.toggleActiveStatus(testCredentials);

		// Then
		verify(trainerRepository).save(any(Trainer.class));
	}

	@Test
	void toggleActiveStatus_shouldThrowAuthenticationException_whenInvalidCredentials() {
		// Given
		when(authenticationService.authenticateTrainer(testCredentials)).thenReturn(false);

		// When/Then
		assertThatThrownBy(() -> trainerService.toggleActiveStatus(testCredentials))
			.isInstanceOf(AuthenticationException.class);
	}

	@Test
	void deleteProfile_shouldDeleteTrainer() {
		// Given
		when(authenticationService.authenticateTrainer(testCredentials)).thenReturn(true);
		doNothing().when(trainerRepository).deleteByUsername("Alice.Johnson");

		// When
		trainerService.deleteProfile(testCredentials);

		// Then
		verify(trainerRepository).deleteByUsername("Alice.Johnson");
	}

	@Test
	void deleteProfile_shouldThrowAuthenticationException_whenInvalidCredentials() {
		// Given
		when(authenticationService.authenticateTrainer(testCredentials)).thenReturn(false);

		// When/Then
		assertThatThrownBy(() -> trainerService.deleteProfile(testCredentials))
			.isInstanceOf(AuthenticationException.class);
	}

	@Test
	void findProfileByUsername_shouldReturnTrainer() {
		// Given
		when(authenticationService.authenticateTrainer(testCredentials)).thenReturn(true);
		when(trainerRepository.findByUsername("Alice.Johnson")).thenReturn(Optional.of(testTrainer));

		// When
		Optional<Trainer> found = trainerService.findProfileByUsername(testCredentials);

		// Then
		assertThat(found).isPresent();
		assertThat(found.get().getUsername()).isEqualTo("Alice.Johnson");
	}

	@Test
	void findProfileByUsername_shouldThrowAuthenticationException_whenInvalidCredentials() {
		// Given/When/Then
		assertThatThrownBy(() -> trainerService.findProfileByUsername(testCredentials))
			.isInstanceOf(AuthenticationException.class);
	}

}