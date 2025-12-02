package com.epam.interfaces.web.controller;

import com.epam.application.Credentials;
import com.epam.application.facade.GymFacade;
import com.epam.application.request.CreateTrainerProfileRequest;
import com.epam.domain.TrainingFilter;
import com.epam.domain.model.Trainee;
import com.epam.domain.model.Trainer;
import com.epam.domain.model.Training;
import com.epam.domain.model.TrainingType;
import com.epam.domain.model.TrainingTypeEnum;
import com.epam.interfaces.web.controller.impl.TrainerController;
import com.epam.interfaces.web.dto.request.TrainerRegistrationRequest;
import com.epam.interfaces.web.dto.request.UpdateTrainerRequest;
import com.epam.interfaces.web.util.AuthenticationHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
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

@WebMvcTest(TrainerController.class)
@TestPropertySource(properties = "spring.main.banner-mode=off")
@ActiveProfiles("test")
class TrainerControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private GymFacade gymFacade;

	@MockitoBean
	private AuthenticationHelper authHelper;

	private final String AUTHORIZATION_HEADER = "TemporaryAuthentication";

	private Trainer testTrainer;

	private Trainee testTrainee;

	private Credentials testCredentials;

	private TrainingType testTrainingType;

	@BeforeEach
	void setUp() {
		testCredentials = new Credentials("jane.smith", "password123");

		testTrainingType = new TrainingType(TrainingTypeEnum.CARDIO);
		testTrainingType.setTrainingTypeId(1L);

		testTrainer = new Trainer("Jane", "Smith", true, testTrainingType);
		testTrainer.setUsername("jane.smith");
		testTrainer.setPassword("password123");

		testTrainee = new Trainee("John", "Doe", true);
		testTrainee.setUsername("john.doe");
		testTrainee.setDob(LocalDate.of(1990, 1, 1));
		testTrainee.setAddress("123 Main St");
	}

	@Test
	@DisplayName("POST /api/trainers - Should register trainer successfully")
	void testRegisterTrainer_Success() throws Exception {
		// Given
		TrainerRegistrationRequest request = new TrainerRegistrationRequest("Jane", "Smith", TrainingTypeEnum.CARDIO);

		when(gymFacade.createTrainerProfile(any(CreateTrainerProfileRequest.class))).thenReturn(testTrainer);

		// When & Then
		mockMvc
			.perform(post("/api/trainers").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.username").value("jane.smith"))
			.andExpect(jsonPath("$.password").value("password123"));

		verify(gymFacade, times(1)).createTrainerProfile(any());
	}

	@Test
	@DisplayName("POST /api/trainers - Should return 400 for invalid input")
	void testRegisterTrainer_InvalidInput() throws Exception {
		// Given
		TrainerRegistrationRequest request = new TrainerRegistrationRequest("", "Smith", TrainingTypeEnum.CARDIO);

		// When & Then
		mockMvc
			.perform(post("/api/trainers").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest());

		verify(gymFacade, never()).createTrainerProfile(any());
	}

	@Test
	@DisplayName("POST /api/trainers - Should return 400 for missing specialization")
	void testRegisterTrainer_MissingSpecialization() throws Exception {
		// Given
		String requestJson = """
				{
				  "firstName": "Jane",
				  "lastName": "Smith"
				}
				""";

		// When & Then
		mockMvc.perform(post("/api/trainers").contentType(MediaType.APPLICATION_JSON).content(requestJson))
			.andExpect(status().isBadRequest());

		verify(gymFacade, never()).createTrainerProfile(any());
	}

	@Test
	@DisplayName("GET /api/trainers/{username} - Should return trainer profile")
	void testGetProfile_Success() throws Exception {
		// Given
		when(authHelper.extractAndValidateCredentials("jane.smith:password123", "jane.smith"))
			.thenReturn(testCredentials);
		when(gymFacade.findTrainerByUsername(testCredentials)).thenReturn(Optional.of(testTrainer));
		when(gymFacade.getTrainerTrainees(testCredentials)).thenReturn(List.of(testTrainee));

		// When & Then
		mockMvc.perform(get("/api/trainers/jane.smith").header(AUTHORIZATION_HEADER, "jane.smith:password123"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.firstName").value("Jane"))
			.andExpect(jsonPath("$.lastName").value("Smith"))
			.andExpect(jsonPath("$.specialization").value("CARDIO"))
			.andExpect(jsonPath("$.active").value(true))
			.andExpect(jsonPath("$.trainees").isArray())
			.andExpect(jsonPath("$.trainees[0].username").value("john.doe"));

		verify(authHelper).extractAndValidateCredentials("jane.smith:password123", "jane.smith");
		verify(gymFacade).findTrainerByUsername(testCredentials);
	}

	@Test
	@DisplayName("GET /api/trainers/{username} - Should return 404 when trainer not found")
	void testGetProfile_NotFound() throws Exception {
		// Given
		when(authHelper.extractAndValidateCredentials(anyString(), anyString())).thenReturn(testCredentials);
		when(gymFacade.findTrainerByUsername(testCredentials)).thenReturn(Optional.empty());

		// When & Then
		mockMvc.perform(get("/api/trainers/jane.smith").header(AUTHORIZATION_HEADER, "jane.smith:password123"))
			.andExpect(status().isNotFound());

		verify(gymFacade).findTrainerByUsername(testCredentials);
	}

	@Test
	@DisplayName("PUT /api/trainers/{username} - Should update trainer profile")
	void testUpdateProfile_Success() throws Exception {
		// Given
		UpdateTrainerRequest request = new UpdateTrainerRequest("Jane", "Smith Updated", TrainingTypeEnum.YOGA, false);

		testTrainer.setLastName("Smith Updated");
		testTrainer.setActive(false);
		TrainingType yogaType = new TrainingType(TrainingTypeEnum.YOGA);
		yogaType.setTrainingTypeId(2L);
		testTrainer.setSpecialization(yogaType);

		when(authHelper.extractAndValidateCredentials(anyString(), eq("jane.smith"))).thenReturn(testCredentials);
		when(gymFacade.updateTrainerProfile(any())).thenReturn(testTrainer);
		when(gymFacade.getTrainerTrainees(testCredentials)).thenReturn(List.of());

		// When & Then
		mockMvc
			.perform(put("/api/trainers/jane.smith").header(AUTHORIZATION_HEADER, "jane.smith:password123")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.username").value("jane.smith"))
			.andExpect(jsonPath("$.lastName").value("Smith Updated"))
			.andExpect(jsonPath("$.specialization").value("YOGA"))
			.andExpect(jsonPath("$.active").value(false));

		verify(gymFacade).updateTrainerProfile(any());
	}

	@Test
	@DisplayName("PUT /api/trainers/{username} - Should return 400 for invalid input")
	void testUpdateProfile_InvalidInput() throws Exception {
		// Given
		UpdateTrainerRequest request = new UpdateTrainerRequest("", // Invalid: blank
																	// first name
				"Smith", TrainingTypeEnum.CARDIO, true);

		// When & Then
		mockMvc
			.perform(put("/api/trainers/jane.smith").header(AUTHORIZATION_HEADER, "jane.smith:password123")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest());

		verify(gymFacade, never()).updateTrainerProfile(any());
	}

	@Test
	@DisplayName("GET /api/trainers/{username}/trainings - Should return trainings with filters")
	void testGetTrainings_Success() throws Exception {
		// Given
		Training training = Training.builder()
			.trainingName("Morning Session")
			.trainingDate(LocalDateTime.now())
			.trainingDurationMin(90)
			.trainee(testTrainee)
			.trainer(testTrainer)
			.trainingType(testTrainingType)
			.build();

		when(authHelper.extractAndValidateCredentials(anyString(), eq("jane.smith"))).thenReturn(testCredentials);
		when(gymFacade.getTrainerTrainings(eq(testCredentials), any(TrainingFilter.class)))
			.thenReturn(List.of(training));

		// When & Then
		mockMvc
			.perform(get("/api/trainers/jane.smith/trainings").header(AUTHORIZATION_HEADER, "jane.smith:password123")
				.param("traineeName", "john.doe")
				.param("periodFrom", "2024-01-01T00:00:00")
				.param("periodTo", "2024-12-31T23:59:59"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[0].trainingName").value("Morning Session"))
			.andExpect(jsonPath("$[0].durationMin").value(90));

		verify(gymFacade).getTrainerTrainings(eq(testCredentials), any(TrainingFilter.class));
	}

	@Test
	@DisplayName("GET /api/trainers/{username}/trainings - Should return trainings without filters")
	void testGetTrainings_NoFilters() throws Exception {
		// Given
		when(authHelper.extractAndValidateCredentials(anyString(), eq("jane.smith"))).thenReturn(testCredentials);
		when(gymFacade.getTrainerTrainings(eq(testCredentials), any(TrainingFilter.class))).thenReturn(List.of());

		// When & Then
		mockMvc
			.perform(get("/api/trainers/jane.smith/trainings").header(AUTHORIZATION_HEADER, "jane.smith:password123"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$").isEmpty());

		verify(gymFacade).getTrainerTrainings(eq(testCredentials), any(TrainingFilter.class));
	}

	@Test
	@DisplayName("PATCH /api/trainers/{username}/activation - Should toggle activation status")
	void testToggleActivation_Success() throws Exception {
		// Given
		when(authHelper.extractAndValidateCredentials(anyString(), eq("jane.smith"))).thenReturn(testCredentials);
		doNothing().when(gymFacade).toggleTrainerActiveStatus(testCredentials);

		// When & Then
		mockMvc
			.perform(
					patch("/api/trainers/jane.smith/activation").header(AUTHORIZATION_HEADER, "jane.smith:password123"))
			.andExpect(status().isOk());

		verify(gymFacade).toggleTrainerActiveStatus(testCredentials);
	}

	@Test
	@DisplayName("Should return 400 when Authorization header is missing")
	void testMissingAuthorizationHeader() throws Exception {
		// Given
		when(authHelper.extractAndValidateCredentials(null, "jane.smith"))
			.thenThrow(new IllegalArgumentException("Authorization header is required"));

		// When & Then
		mockMvc.perform(get("/api/trainers/jane.smith")).andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("Should return 400 when username mismatch")
	void testUsernameMismatch() throws Exception {
		// Given
		when(authHelper.extractAndValidateCredentials("different.user:password", "jane.smith"))
			.thenThrow(new IllegalArgumentException("Username in path does not match authenticated user"));

		// When & Then
		mockMvc.perform(get("/api/trainers/jane.smith").header(AUTHORIZATION_HEADER, "different.user:password"))
			.andExpect(status().isBadRequest());
	}

}