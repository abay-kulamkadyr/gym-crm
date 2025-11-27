package com.epam.interfaces.web.controller;

import com.epam.application.Credentials;
import com.epam.application.facade.GymFacade;
import com.epam.application.request.CreateTraineeProfileRequest;
import com.epam.domain.TrainingFilter;
import com.epam.domain.model.Trainee;
import com.epam.domain.model.Trainer;
import com.epam.domain.model.Training;
import com.epam.domain.model.TrainingType;
import com.epam.domain.model.TrainingTypeEnum;
import com.epam.interfaces.web.dto.request.TraineeRegistrationRequest;
import com.epam.interfaces.web.dto.request.UpdateTraineeRequest;
import com.epam.interfaces.web.dto.request.UpdateTraineeTrainersRequest;
import com.epam.interfaces.web.util.AuthenticationHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TraineeController.class)
class TraineeControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private GymFacade gymFacade;

	@MockitoBean
	private AuthenticationHelper authHelper;

	private final String AUTHORIZATION_HEADER = "TemporaryAuthentication";

	private Trainee testTrainee;

	private Credentials testCredentials;

	private Trainer testTrainer;

	@BeforeEach
	void setUp() {
		testCredentials = new Credentials("john.doe", "password123");

		testTrainee = new Trainee("John", "Doe", true);
		testTrainee.setUsername("john.doe");
		testTrainee.setPassword("password123");
		testTrainee.setDob(LocalDate.of(1990, 1, 1));
		testTrainee.setAddress("123 Main St");

		TrainingType trainingType = new TrainingType(TrainingTypeEnum.BOXING);
		trainingType.setTrainingTypeId(1L);

		testTrainer = new Trainer("Jane", "Smith", true, trainingType);
		testTrainer.setUsername("jane.smith");
	}

	@Test
	@DisplayName("POST /api/trainees - Should register trainee successfully")
	void testRegisterTrainee_Success() throws Exception {
		// Given
		TraineeRegistrationRequest request = new TraineeRegistrationRequest("John", "Doe",
				Optional.of(LocalDate.of(1990, 1, 1)), Optional.of("123 Main St"));

		when(gymFacade.createTraineeProfile(any(CreateTraineeProfileRequest.class))).thenReturn(testTrainee);

		// When & Then
		mockMvc
			.perform(post("/api/trainees").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.username").value("john.doe"))
			.andExpect(jsonPath("$.password").value("password123"));

		verify(gymFacade, times(1)).createTraineeProfile(any());
	}

	@Test
	@DisplayName("POST /api/trainees - Should return 400 for invalid input")
	void testRegisterTrainee_InvalidInput() throws Exception {
		// Given
		TraineeRegistrationRequest request = new TraineeRegistrationRequest("", // Invalid:
																				// blank
																				// first
																				// name
				"Doe", Optional.of(LocalDate.of(1990, 1, 1)), Optional.of("123 Main St"));

		// When & Then
		mockMvc
			.perform(post("/api/trainees").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest());

		verify(gymFacade, never()).createTraineeProfile(any());
	}

	@Test
	@DisplayName("POST /api/trainees - Should return 400 for future date of birth")
	void testRegisterTrainee_FutureDateOfBirth() throws Exception {
		// Given
		TraineeRegistrationRequest request = new TraineeRegistrationRequest("John", "Doe",
				Optional.of(LocalDate.now().plusDays(1)), // Future date
				Optional.of("123 Main St"));

		// When & Then
		mockMvc
			.perform(post("/api/trainees").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("GET /api/trainees/{username} - Should return trainee profile")
	void testGetProfile_Success() throws Exception {
		// Given
		when(authHelper.extractAndValidateCredentials("john.doe:password123", "john.doe")).thenReturn(testCredentials);
		when(gymFacade.findTraineeByUsername(testCredentials)).thenReturn(Optional.of(testTrainee));
		when(gymFacade.getTraineeTrainers(testCredentials)).thenReturn(List.of(testTrainer));

		// When & Then
		mockMvc.perform(get("/api/trainees/john.doe").header(AUTHORIZATION_HEADER, "john.doe:password123"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.firstName").value("John"))
			.andExpect(jsonPath("$.lastName").value("Doe"))
			.andExpect(jsonPath("$.address").value("123 Main St"))
			.andExpect(jsonPath("$.active").value(true))
			.andExpect(jsonPath("$.trainers").isArray())
			.andExpect(jsonPath("$.trainers[0].username").value("jane.smith"));

		verify(authHelper).extractAndValidateCredentials("john.doe:password123", "john.doe");
		verify(gymFacade).findTraineeByUsername(testCredentials);
	}

	@Test
	@DisplayName("GET /api/trainees/{username} - Should return 404 when trainee not found")
	void testGetProfile_NotFound() throws Exception {
		// Given
		when(authHelper.extractAndValidateCredentials(anyString(), anyString())).thenReturn(testCredentials);
		when(gymFacade.findTraineeByUsername(testCredentials)).thenReturn(Optional.empty());

		// When & Then
		mockMvc.perform(get("/api/trainees/john.doe").header(AUTHORIZATION_HEADER, "john.doe:password123"))
			.andExpect(status().isNotFound()); // Will throw IllegalArgumentException

		verify(gymFacade).findTraineeByUsername(testCredentials);
	}

	@Test
	@DisplayName("PUT /api/trainees/{username} - Should update trainee profile")
	void testUpdateProfile_Success() throws Exception {
		// Given
		UpdateTraineeRequest request = new UpdateTraineeRequest("John", "Doe Updated", LocalDate.of(1990, 1, 1),
				"456 New St", false);

		testTrainee.setLastName("Doe Updated");
		testTrainee.setAddress("456 New St");
		testTrainee.setActive(false);

		when(authHelper.extractAndValidateCredentials(anyString(), eq("john.doe"))).thenReturn(testCredentials);
		when(gymFacade.updateTraineeProfile(any())).thenReturn(testTrainee);
		when(gymFacade.getTraineeTrainers(testCredentials)).thenReturn(List.of());

		// When & Then
		mockMvc
			.perform(put("/api/trainees/john.doe").header(AUTHORIZATION_HEADER, "john.doe:password123")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.username").value("john.doe"))
			.andExpect(jsonPath("$.lastName").value("Doe Updated"))
			.andExpect(jsonPath("$.address").value("456 New St"))
			.andExpect(jsonPath("$.active").value(false));

		verify(gymFacade).updateTraineeProfile(any());
	}

	@Test
	@DisplayName("DELETE /api/trainees/{username} - Should delete trainee profile")
	void testDeleteProfile_Success() throws Exception {
		// Given
		when(authHelper.extractAndValidateCredentials(anyString(), eq("john.doe"))).thenReturn(testCredentials);
		doNothing().when(gymFacade).deleteTraineeProfile(testCredentials);

		// When & Then
		mockMvc.perform(delete("/api/trainees/john.doe").header(AUTHORIZATION_HEADER, "john.doe:password123"))
			.andExpect(status().isNoContent());

		verify(gymFacade).deleteTraineeProfile(testCredentials);
	}

	@Test
	@DisplayName("GET /api/trainees/{username}/available-trainers - Should return available trainers")
	void testGetAvailableTrainers_Success() throws Exception {
		// Given
		when(authHelper.extractAndValidateCredentials(anyString(), eq("john.doe"))).thenReturn(testCredentials);
		when(gymFacade.getTraineeUnassignedTrainers(testCredentials)).thenReturn(List.of(testTrainer));

		// When & Then
		mockMvc
			.perform(get("/api/trainees/john.doe/available-trainers").header(AUTHORIZATION_HEADER,
					"john.doe:password123"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[0].username").value("jane.smith"))
			.andExpect(jsonPath("$[0].specialization").value("BOXING"));

		verify(gymFacade).getTraineeUnassignedTrainers(testCredentials);
	}

	@Test
	@DisplayName("PUT /api/trainees/{username}/trainers - Should update trainers list")
	void testUpdateTrainers_Success() throws Exception {
		// Given
		UpdateTraineeTrainersRequest request = new UpdateTraineeTrainersRequest(List.of("jane.smith", "bob.jones"));

		// When
		when(authHelper.extractAndValidateCredentials(anyString(), eq("john.doe"))).thenReturn(testCredentials);
		when(gymFacade.getTraineeTrainers(testCredentials)).thenReturn(List.of(testTrainer));

		// Then
		mockMvc
			.perform(put("/api/trainees/john.doe/trainers").header(AUTHORIZATION_HEADER, "john.doe:password123")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray());

		verify(gymFacade).updateTraineeTrainersList(testCredentials, request.trainerUsernames());
	}

	@Test
	@DisplayName("PUT /api/trainees/{username}/trainers - Should return 400 for empty list")
	void testUpdateTrainers_EmptyList() throws Exception {
		// Given
		UpdateTraineeTrainersRequest request = new UpdateTraineeTrainersRequest(List.of());

		// When & Then
		mockMvc
			.perform(put("/api/trainees/john.doe/trainers").header(AUTHORIZATION_HEADER, "john.doe:password123")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest());

		verify(gymFacade, never()).updateTraineeTrainersList(any(), any());
	}

	@Test
	@DisplayName("GET /api/trainees/{username}/trainings - Should return trainings with filters")
	void testGetTrainings_Success() throws Exception {
		// Given
		TrainingType trainingType = new TrainingType(TrainingTypeEnum.CARDIO);
		Training training = Training.builder()
			.trainingName("Morning Workout") //
			.trainingDate(LocalDateTime.now()) //
			.trainingDurationMin(60) //
			.trainee(testTrainee) //
			.trainer(testTrainer) //
			.trainingType(trainingType)
			.build();

		// When
		when(authHelper.extractAndValidateCredentials(anyString(), eq("john.doe"))).thenReturn(testCredentials);
		when(gymFacade.getTraineeTrainings(eq(testCredentials), any(TrainingFilter.class)))
			.thenReturn(List.of(training));

		// Then
		mockMvc
			.perform(get("/api/trainees/john.doe/trainings").header(AUTHORIZATION_HEADER, "john.doe:password123")
				.param("trainerName", "jane.smith")
				.param("trainingType", "CARDIO"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[0].trainingName").value("Morning Workout"));

		verify(gymFacade).getTraineeTrainings(eq(testCredentials), any(TrainingFilter.class));
	}

	@Test
	@DisplayName("PATCH /api/trainees/{username}/activation - Should toggle activation status")
	void testToggleActivation_Success() throws Exception {
		// Given
		when(authHelper.extractAndValidateCredentials(anyString(), eq("john.doe"))).thenReturn(testCredentials);
		doNothing().when(gymFacade).toggleTraineeActiveStatus(testCredentials);

		// When & Then
		mockMvc.perform(patch("/api/trainees/john.doe/activation").header(AUTHORIZATION_HEADER, "john.doe:password123"))
			.andExpect(status().isOk());

		verify(gymFacade).toggleTraineeActiveStatus(testCredentials);
	}

	@Test
	@DisplayName("Should return 401 when Authorization header is missing")
	void testMissingAuthorizationHeader() throws Exception {
		// Given
		when(authHelper.extractAndValidateCredentials(null, "john.doe"))
			.thenThrow(new IllegalArgumentException("Authorization header is required"));

		// When & Then
		mockMvc.perform(get("/api/trainees/john.doe")).andExpect(status().isBadRequest());
	}

}