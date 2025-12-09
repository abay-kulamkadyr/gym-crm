package com.epam.application.service;

import com.epam.application.exception.ValidationException;
import com.epam.application.request.CreateTraineeProfileRequest;
import com.epam.application.request.UpdateTraineeProfileRequest;
import com.epam.application.service.impl.TraineeServiceImpl;
import com.epam.domain.model.Trainee;
import com.epam.domain.model.Trainer;
import com.epam.domain.model.TrainingType;
import com.epam.domain.model.TrainingTypeEnum;
import com.epam.domain.port.TraineeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TraineeServiceImplTest {

	@Mock
	private TraineeRepository traineeRepository;

	@Mock
	private ApplicationEventPublisher applicationEventPublisher;

	@InjectMocks
	private TraineeServiceImpl traineeService;

	private Trainee testTrainee;

	@BeforeEach
	void setUp() {
		testTrainee = new Trainee("John", "Doe", true);
		testTrainee.setTraineeId(1L);
		testTrainee.setUserId(10L);
		testTrainee.setUsername("John.Doe");
		testTrainee.setPassword("password123");
		testTrainee.setDob(LocalDate.of(1990, 1, 1));
		testTrainee.setAddress("123 Main St");
	}

	@Test
	void createProfile_shouldGenerateUsernameAndPassword() {
		// Given
		CreateTraineeProfileRequest request = new CreateTraineeProfileRequest("John", "Doe", true, Optional.empty(),
				Optional.empty());

		when(traineeRepository.findLatestUsername("John.Doe")).thenReturn(Optional.empty());
		when(traineeRepository.save(any(Trainee.class))).thenReturn(testTrainee);

		// When
		Trainee created = traineeService.createProfile(request);

		// Then
		assertThat(created).isNotNull();
		assertThat(created.getUsername()).isEqualTo("John.Doe");
		assertThat(created.getPassword()).isNotNull();
		verify(traineeRepository).save(any(Trainee.class));
	}

	@Test
	void createProfile_shouldGenerateUniqueUsernameWhenDuplicateExists() {
		// Given
		CreateTraineeProfileRequest request = new CreateTraineeProfileRequest("John", "Doe", true, Optional.empty(),
				Optional.empty());

		when(traineeRepository.findLatestUsername("John.Doe")).thenReturn(Optional.of("John.Doe1"));

		Trainee traineeWithSerial = new Trainee("John", "Doe", true);
		traineeWithSerial.setUsername("John.Doe2");
		traineeWithSerial.setPassword("generatedPass");
		when(traineeRepository.save(any(Trainee.class))).thenReturn(traineeWithSerial);

		// When
		Trainee created = traineeService.createProfile(request);

		// Then
		assertThat(created.getUsername()).isEqualTo("John.Doe2");
		verify(traineeRepository).save(any(Trainee.class));
	}

	@Test
	void createProfile_withDobAndAddress_shouldSetBothFields() {
		// Given
		LocalDate dob = LocalDate.of(1995, 5, 15);
		CreateTraineeProfileRequest request = new CreateTraineeProfileRequest("Jane", "Smith", true, Optional.of(dob),
				Optional.of("456 Oak Ave"));

		Trainee savedTrainee = new Trainee("Jane", "Smith", true);
		savedTrainee.setDob(dob);
		savedTrainee.setAddress("456 Oak Ave");
		savedTrainee.setUsername("Jane.Smith");

		when(traineeRepository.findLatestUsername(any())).thenReturn(Optional.empty());
		when(traineeRepository.save(any(Trainee.class))).thenReturn(savedTrainee);

		// When
		Trainee created = traineeService.createProfile(request);

		// Then
		assertThat(created.getDob()).isEqualTo(dob);
		assertThat(created.getAddress()).isEqualTo("456 Oak Ave");
		verify(traineeRepository).save(any(Trainee.class));
	}

	@Test
	void createProfile_shouldThrowValidationException_whenFirstNameIsNull() {
		// Given
		CreateTraineeProfileRequest request = new CreateTraineeProfileRequest(null, "Doe", true, Optional.empty(),
				Optional.empty());

		// When/Then
		assertThatThrownBy(() -> traineeService.createProfile(request)).isInstanceOf(ValidationException.class)
			.hasMessageContaining("First name");
	}

	@Test
	void createProfile_shouldThrowValidationException_whenLastNameIsEmpty() {
		// Given
		CreateTraineeProfileRequest request = new CreateTraineeProfileRequest("John", "", true, Optional.empty(),
				Optional.empty());

		// When/Then
		assertThatThrownBy(() -> traineeService.createProfile(request)).isInstanceOf(ValidationException.class)
			.hasMessageContaining("Last name");
	}

	@Test
	void updateProfile_shouldUpdateAllProvidedFields() {
		// Given
		UpdateTraineeProfileRequest request = new UpdateTraineeProfileRequest(testTrainee.getUsername(), //
				Optional.of("Jane"), //
				Optional.of("Smith"), //
				Optional.of("newpassword123"), //
				Optional.of(false), //
				Optional.of(LocalDate.of(1992, 3, 15)), //
				Optional.of("789 Elm St"));

		when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(testTrainee));

		Trainee updatedTrainee = new Trainee("Jane", "Smith", false);
		updatedTrainee.setUsername("Jane.Smith");
		updatedTrainee.setPassword("newpassword123");
		updatedTrainee.setDob(LocalDate.of(1992, 3, 15));
		updatedTrainee.setAddress("789 Elm St");

		when(traineeRepository.save(any(Trainee.class))).thenReturn(updatedTrainee);

		// When
		Trainee updated = traineeService.updateProfile(request);

		// Then
		assertThat(updated).isNotNull();
		verify(traineeRepository).save(any(Trainee.class));
	}

	@Test
	void updateProfile_shouldThrowEntityNotFoundException_whenCredentialsInvalid() {
		// Given
		UpdateTraineeProfileRequest request = new UpdateTraineeProfileRequest(testTrainee.getUsername(),
				Optional.of("Jane"), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
				Optional.empty());

		// When/Then
		assertThatThrownBy(() -> traineeService.updateProfile(request)).isInstanceOf(EntityNotFoundException.class);
	}

	@Test
	void updateProfile_shouldThrowValidationException_whenNewUsernameSameAsCurrent() {
		// Given
		UpdateTraineeProfileRequest request = new UpdateTraineeProfileRequest(testTrainee.getUsername(), //
				Optional.empty(), //
				Optional.empty(), //
				Optional.of("John.Doe"), //
				Optional.empty(), //
				Optional.empty(), //
				Optional.empty() //
		);

		when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(testTrainee));

		// When/Then
		assertThatThrownBy(() -> traineeService.updateProfile(request)).isInstanceOf(ValidationException.class);
	}

	@Test
	void updatePassword_shouldUpdatePassword() {
		// Given
		String newPassword = "newSecurePass123";
		when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(testTrainee));
		when(traineeRepository.save(any(Trainee.class))).thenReturn(testTrainee);

		// When
		traineeService.updatePassword(testTrainee.getUsername(), newPassword);

		// Then
		verify(traineeRepository).save(any(Trainee.class));
	}

	@Test
	void updatePassword_shouldThrowEntityNotFoundException_whenInvalidCredentials() {
		// When/Then
		assertThatThrownBy(() -> traineeService.updatePassword(testTrainee.getUsername(), "newPassword"))
			.isInstanceOf(EntityNotFoundException.class);
	}

	@Test
	void updatePassword_shouldThrowValidationException_whenNewPasswordTooShort() {
		// Give/When/Then
		assertThatThrownBy(() -> traineeService.updatePassword(testTrainee.getUsername(), "short"))
			.isInstanceOf(ValidationException.class)
			.hasMessageContaining("10 characters");
	}

	@Test
	void toggleActiveStatus_shouldToggleFromTrueToFalse() {
		// Given
		when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(testTrainee));
		when(traineeRepository.save(any(Trainee.class))).thenReturn(testTrainee);

		// When
		traineeService.toggleActiveStatus(testTrainee.getUsername());

		// Then
		verify(traineeRepository).save(any(Trainee.class));
	}

	@Test
	void toggleActiveStatus_shouldThrowEntityNotFoundException_whenInvalidCredentials() {
		// When/Then
		assertThatThrownBy(() -> traineeService.toggleActiveStatus(testTrainee.getUsername()))
			.isInstanceOf(EntityNotFoundException.class);
	}

	@Test
	void deleteProfile_shouldDeleteTrainee() {
		// Given
		doNothing().when(traineeRepository).deleteByUsername("John.Doe");
		when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.ofNullable(testTrainee));

		// When
		traineeService.deleteProfile(testTrainee.getUsername());

		// Then
		verify(traineeRepository).deleteByUsername("John.Doe");
	}

	@Test
	void deleteProfile_shouldThrowEntityNotFoundException_whenInvalidCredentials() {
		// When/Then
		assertThatThrownBy(() -> traineeService.deleteProfile(testTrainee.getUsername()))
			.isInstanceOf(EntityNotFoundException.class);
	}

	@Test
	void findProfileByUsername_shouldReturnTrainee() {
		// Given
		when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(testTrainee));

		// When
		Trainee found = traineeService.getProfileByUsername(testTrainee.getUsername());

		// Then
		assertThat(found).isNotNull();
		assertThat(found.getUsername()).isEqualTo("John.Doe");
	}

	@Test
	void findProfileByUsername_shouldThrowAuthenticationException_whenInvalidCredentials() {
		// Given
		String invalidUsername = "invalid.username";
		// When/Then
		assertThatThrownBy(() -> traineeService.getProfileByUsername(invalidUsername))
			.isInstanceOf(EntityNotFoundException.class);
	}

	@Test
	void getUnassignedTrainers_shouldReturnListOfTrainers() {
		// Given
		TrainingType yogaType = new TrainingType(TrainingTypeEnum.YOGA);
		TrainingType boxingType = new TrainingType(TrainingTypeEnum.BOXING);

		Trainer trainer1 = new Trainer("Alice", "Trainer", true, yogaType);
		trainer1.setUsername("Alice.Trainer");

		Trainer trainer2 = new Trainer("Bob", "Coach", true, boxingType);
		trainer2.setUsername("Bob.Coach");

		when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(testTrainee));
		when(traineeRepository.getUnassignedTrainers("John.Doe")).thenReturn(List.of(trainer1, trainer2));

		// When
		List<Trainer> trainers = traineeService.getUnassignedTrainers(testTrainee.getUsername());

		// Then
		assertThat(trainers).hasSize(2);
		assertThat(trainers).extracting(Trainer::getUsername).containsExactlyInAnyOrder("Alice.Trainer", "Bob.Coach");
	}

	@Test
	void updateTrainersList_shouldUpdateTrainersList() {
		// Given
		List<String> trainerUsernames = List.of("Trainer1.Smith", "Trainer2.Jones");

		when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.ofNullable(testTrainee));
		doNothing().when(traineeRepository).updateTrainersList("John.Doe", trainerUsernames);

		// When
		traineeService.updateTrainersList(testTrainee.getUsername(), trainerUsernames);

		// Then
		verify(traineeRepository).updateTrainersList("John.Doe", trainerUsernames);
	}

	@Test
	void updateTrainersList_shouldHandleEmptyList() {
		// Given
		List<String> emptyList = new ArrayList<>();

		when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.ofNullable(testTrainee));
		doNothing().when(traineeRepository).updateTrainersList("John.Doe", emptyList);

		// When
		traineeService.updateTrainersList(testTrainee.getUsername(), emptyList);

		// Then
		verify(traineeRepository).updateTrainersList("John.Doe", emptyList);
	}

}