package com.epam.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.epam.application.exception.ValidationException;
import com.epam.application.request.CreateTrainerProfileRequest;
import com.epam.application.request.UpdateTrainerProfileRequest;
import com.epam.application.service.impl.TrainerServiceImpl;
import com.epam.domain.model.Trainer;
import com.epam.domain.model.TrainingType;
import com.epam.domain.model.TrainingTypeEnum;
import com.epam.domain.port.TrainerRepository;
import com.epam.domain.port.TrainingTypeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class TrainerServiceImplTest {

	@Mock
	private TrainerRepository trainerRepository;

	@Mock
	private TrainingTypeRepository trainingTypeRepository;

	@Mock
	private ApplicationEventPublisher applicationEventPublisher;

	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private TrainerServiceImpl trainerService;

	private Trainer testTrainer;

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

		UpdateTrainerProfileRequest request = new UpdateTrainerProfileRequest(testTrainer.getUsername(),
				Optional.of("Bob"), Optional.of("Smith"), Optional.of("newpassword123"), Optional.of(false),
				Optional.of(TrainingTypeEnum.YOGA));

		when(trainerRepository.findByUsername("Alice.Johnson")).thenReturn(Optional.ofNullable(testTrainer));
		when(trainingTypeRepository.findByTrainingTypeName(TrainingTypeEnum.YOGA)).thenReturn(Optional.of(yogaType));

		Trainer updatedTrainer = new Trainer("Bob", "Smith", false, yogaType);
		when(trainerRepository.save(any(Trainer.class))).thenReturn(updatedTrainer);

		// When
		Trainer updated = trainerService.updateProfile(request);

		// Then
		assertThat(updated).isNotNull();
		verify(trainerRepository).save(any(Trainer.class));
	}

	@Test
	void updateProfile_shouldThrowIllegalArgumentException_whenSpecializationIsNotValid() {
		// Given
		assertThatThrownBy(
				() -> new UpdateTrainerProfileRequest(testTrainer.getUsername(), Optional.empty(), Optional.empty(),
						Optional.empty(), Optional.empty(), Optional.of(TrainingTypeEnum.valueOf("Non existent"))))
			.isInstanceOf(IllegalArgumentException.class);

	}

	@Test
	void updatePassword_shouldUpdatePassword() {
		// Given
		String newPassword = "newSecurePass123";
		when(trainerRepository.findByUsername("Alice.Johnson")).thenReturn(Optional.of(testTrainer));
		when(trainerRepository.save(any(Trainer.class))).thenReturn(testTrainer);

		// When
		trainerService.updatePassword(testTrainer.getUsername(), newPassword);

		// Then
		verify(trainerRepository).save(any(Trainer.class));
	}

	@Test
	void updatePassword_shouldThrowEntityNotFoundException_whenInvalidCredentials() {
		// When/Then
		assertThatThrownBy(() -> trainerService.updatePassword(testTrainer.getUsername(), "newPassword"))
			.isInstanceOf(EntityNotFoundException.class);
	}

	@Test
	void updatePassword_shouldThrowValidationException_whenNewPasswordTooShort() {
		// Give/When/Then
		assertThatThrownBy(() -> trainerService.updatePassword(testTrainer.getUsername(), "short"))
			.isInstanceOf(ValidationException.class)
			.hasMessageContaining("10 characters");
	}

	@Test
	void toggleActiveStatus_shouldToggleFromTrueToFalse() {
		// Given
		when(trainerRepository.findByUsername("Alice.Johnson")).thenReturn(Optional.of(testTrainer));
		when(trainerRepository.save(any(Trainer.class))).thenReturn(testTrainer);

		// When
		trainerService.toggleActiveStatus(testTrainer.getUsername());

		// Then
		verify(trainerRepository).save(any(Trainer.class));
	}

	@Test
	void toggleActiveStatus_shouldThrowEntityNotFoundException_whenInvalidCredentials() {
		// Given

		String invalidUsername = "invalid.username";
		// When/Then
		assertThatThrownBy(() -> trainerService.toggleActiveStatus(invalidUsername))
			.isInstanceOf(EntityNotFoundException.class);
	}

	@Test
	void deleteProfile_shouldDeleteTrainer() {
		// Given
		when(trainerRepository.findByUsername("Alice.Johnson")).thenReturn(Optional.ofNullable(testTrainer));
		doNothing().when(trainerRepository).deleteByUsername("Alice.Johnson");

		// When
		trainerService.deleteProfile(testTrainer.getUsername());

		// Then
		verify(trainerRepository).deleteByUsername("Alice.Johnson");
	}

	@Test
	void deleteProfile_shouldThrowEntityNotFoundException_whenInvalidCredentials() {
		// When/Then
		assertThatThrownBy(() -> trainerService.deleteProfile(testTrainer.getUsername()))
			.isInstanceOf(EntityNotFoundException.class);
	}

	@Test
	void findProfileByUsername_shouldReturnTrainer() {
		// Given
		when(trainerRepository.findByUsername("Alice.Johnson")).thenReturn(Optional.of(testTrainer));

		// When
		Trainer found = trainerService.getProfileByUsername(testTrainer.getUsername());

		// Then
		assertThat(found).isNotNull();
		assertThat(found.getUsername()).isEqualTo("Alice.Johnson");
	}

	@Test
	void findProfileByUsername_shouldThrowEntityNotFoundException_whenInvalidCredentials() {
		// Given/When/Then
		assertThatThrownBy(() -> trainerService.getProfileByUsername(testTrainer.getUsername()))
			.isInstanceOf(EntityNotFoundException.class);
	}

}
