package com.epam.interfaces.web.controller;

import com.epam.application.Credentials;
import com.epam.application.exception.AuthenticationException;
import com.epam.application.facade.GymFacade;
import com.epam.application.service.AuthenticationService;
import com.epam.domain.model.Trainee;
import com.epam.domain.model.Trainer;
import com.epam.domain.model.TrainingType;
import com.epam.domain.model.TrainingTypeEnum;
import com.epam.interfaces.web.controller.impl.AuthenticationController;
import com.epam.interfaces.web.dto.request.ChangeLoginRequest;
import com.epam.interfaces.web.dto.request.LoginRequest;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthenticationController.class)
@TestPropertySource(properties = "spring.main.banner-mode=off")
@ActiveProfiles("test")
class AuthenticationControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private AuthenticationService authService;

	@MockitoBean
	private GymFacade gymFacade;

	private Trainee testTrainee;

	private Trainer testTrainer;

	@BeforeEach
	void setUp() {
		testTrainee = new Trainee("John", "Doe", true);
		testTrainee.setUsername("john.doe");
		testTrainee.setPassword("password123");
		testTrainee.setDob(LocalDate.of(1990, 1, 1));
		testTrainee.setAddress("123 Main St");

		TrainingType testTrainingType = new TrainingType(TrainingTypeEnum.CARDIO);
		testTrainingType.setTrainingTypeId(1L);

		testTrainer = new Trainer("Jane", "Smith", true, testTrainingType);
		testTrainer.setUsername("jane.smith");
		testTrainer.setPassword("password123");
	}

	@Test
	@DisplayName("POST /api/auth/login - Should login successfully")
	void testLogin_Success() throws Exception {
		// Given
		LoginRequest request = new LoginRequest("john.doe", "password123");
		when(authService.authenticate(any(Credentials.class))).thenReturn(testTrainee);

		// When & Then
		mockMvc
			.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk());

		verify(authService).authenticate(new Credentials("john.doe", "password123"));
	}

	@Test
	@DisplayName("POST /api/auth/login - Should return 401 for invalid credentials")
	void testLogin_InvalidCredentials() throws Exception {
		// Given
		LoginRequest request = new LoginRequest("john.doe", "wrongpassword");
		when(authService.authenticate(any(Credentials.class)))
			.thenThrow(new AuthenticationException("Invalid credentials"));

		// When & Then
		mockMvc
			.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.error").value("Authentication Failed"))
			.andExpect(jsonPath("$.message").value("Invalid credentials"));

		verify(authService).authenticate(any(Credentials.class));
	}

	@Test
	@DisplayName("POST /api/auth/login - Should return 400 for missing username")
	void testLogin_MissingUsername() throws Exception {
		// Given
		LoginRequest request = new LoginRequest("", "password123");

		// When & Then
		mockMvc
			.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error").value("Validation Failed"));

		verify(authService, never()).authenticate(any());
	}

	@Test
	@DisplayName("POST /api/auth/login - Should return 400 for missing password")
	void testLogin_MissingPassword() throws Exception {
		// Given
		LoginRequest request = new LoginRequest("john.doe", "");

		// When & Then
		mockMvc
			.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error").value("Validation Failed"));

		verify(authService, never()).authenticate(any());
	}

	@Test
	@DisplayName("PUT /api/auth/password - Should change password for trainee")
	void testChangePassword_Trainee_Success() throws Exception {
		// Given
		ChangeLoginRequest request = new ChangeLoginRequest("john.doe", "oldPassword123", "newPassword456");

		when(authService.authenticate(any(Credentials.class))).thenReturn(testTrainee);
		doNothing().when(gymFacade).updateTraineePassword(any(), anyString());

		// When & Then
		mockMvc
			.perform(put("/api/auth/password").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk());

		verify(authService).authenticate(new Credentials("john.doe", "oldPassword123"));
		verify(gymFacade).updateTraineePassword(any(Credentials.class), eq("newPassword456"));
		verify(gymFacade, never()).updateTrainerPassword(any(), any());
	}

	@Test
	@DisplayName("PUT /api/auth/password - Should change password for trainer")
	void testChangePassword_Trainer_Success() throws Exception {
		// Given
		ChangeLoginRequest request = new ChangeLoginRequest("jane.smith", "oldPassword123", "newPassword456");

		when(authService.authenticate(any(Credentials.class))).thenReturn(testTrainer);
		doNothing().when(gymFacade).updateTrainerPassword(any(), anyString());

		// When & Then
		mockMvc
			.perform(put("/api/auth/password").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk());

		verify(authService).authenticate(new Credentials("jane.smith", "oldPassword123"));
		verify(gymFacade).updateTrainerPassword(any(Credentials.class), eq("newPassword456"));
		verify(gymFacade, never()).updateTraineePassword(any(), any());
	}

	@Test
	@DisplayName("PUT /api/auth/password - Should return 401 for wrong old password")
	void testChangePassword_WrongOldPassword() throws Exception {
		// Given
		ChangeLoginRequest request = new ChangeLoginRequest("john.doe", "wrongOldPassword", "newPassword456");

		when(authService.authenticate(any(Credentials.class)))
			.thenThrow(new AuthenticationException("Invalid credentials"));

		// When & Then
		mockMvc
			.perform(put("/api/auth/password").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.error").value("Authentication Failed"));

		verify(authService).authenticate(any(Credentials.class));
		verify(gymFacade, never()).updateTraineePassword(any(), any());
		verify(gymFacade, never()).updateTrainerPassword(any(), any());
	}

	@Test
	@DisplayName("PUT /api/auth/password - Should return 400 for short new password")
	void testChangePassword_ShortNewPassword() throws Exception {
		// Given
		ChangeLoginRequest request = new ChangeLoginRequest("john.doe", "oldPassword123", "short");

		// When & Then
		mockMvc
			.perform(put("/api/auth/password").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error").value("Validation Failed"));

		verify(authService, never()).authenticate(any());
	}

	@Test
	@DisplayName("PUT /api/auth/password - Should return 400 for long new password")
	void testChangePassword_LongNewPassword() throws Exception {
		// Given
		String longPassword = "a".repeat(101); // More than 100 characters
		ChangeLoginRequest request = new ChangeLoginRequest("john.doe", "oldPassword123", longPassword);

		// When & Then
		mockMvc
			.perform(put("/api/auth/password").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error").value("Validation Failed"));

		verify(authService, never()).authenticate(any());
	}

}