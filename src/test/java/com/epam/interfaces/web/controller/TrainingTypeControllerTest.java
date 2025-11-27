package com.epam.interfaces.web.controller;

import com.epam.application.facade.GymFacade;
import com.epam.domain.model.TrainingType;
import com.epam.domain.model.TrainingTypeEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(TrainingTypeController.class)
class TrainingTypeControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private GymFacade gymFacade;

	private List<TrainingType> trainingTypes;

	@BeforeEach
	void setUp() {
		TrainingType fitnessType = new TrainingType(TrainingTypeEnum.CROSSFIT);
		fitnessType.setTrainingTypeId(1L);

		TrainingType yogaType = new TrainingType(TrainingTypeEnum.YOGA);
		yogaType.setTrainingTypeId(2L);

		TrainingType boxingType = new TrainingType(TrainingTypeEnum.BOXING);
		boxingType.setTrainingTypeId(3L);

		TrainingType cardioType = new TrainingType(TrainingTypeEnum.CARDIO);
		cardioType.setTrainingTypeId(4L);

		TrainingType strengthType = new TrainingType(TrainingTypeEnum.STRENGTH);
		strengthType.setTrainingTypeId(5L);

		trainingTypes = List.of(fitnessType, yogaType, boxingType, cardioType, strengthType);
	}

	@Test
	@DisplayName("GET /api/training-types - Should return all training types")
	void testGetTrainingTypes_Success() throws Exception {
		// Given
		when(gymFacade.getTrainingTypes()).thenReturn(trainingTypes);

		// When & Then
		mockMvc.perform(get("/api/training-types"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$.length()").value(5))
			.andExpect(jsonPath("$[0].id").value(1))
			.andExpect(jsonPath("$[0].trainingType").value("CROSSFIT"))
			.andExpect(jsonPath("$[1].id").value(2))
			.andExpect(jsonPath("$[1].trainingType").value("YOGA"))
			.andExpect(jsonPath("$[2].id").value(3))
			.andExpect(jsonPath("$[2].trainingType").value("BOXING"))
			.andExpect(jsonPath("$[3].id").value(4))
			.andExpect(jsonPath("$[3].trainingType").value("CARDIO"))
			.andExpect(jsonPath("$[4].id").value(5))
			.andExpect(jsonPath("$[4].trainingType").value("STRENGTH"));

		verify(gymFacade, times(1)).getTrainingTypes();
	}

	@Test
	@DisplayName("GET /api/training-types - Should return empty list when no types exist")
	void testGetTrainingTypes_EmptyList() throws Exception {
		// Given
		when(gymFacade.getTrainingTypes()).thenReturn(List.of());

		// When & Then
		mockMvc.perform(get("/api/training-types"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$.length()").value(0));

		verify(gymFacade, times(1)).getTrainingTypes();
	}

	@Test
	@DisplayName("GET /api/training-types - Should return single training type")
	void testGetTrainingTypes_SingleType() throws Exception {
		// Given
		TrainingType singleType = new TrainingType(TrainingTypeEnum.CARDIO);
		singleType.setTrainingTypeId(1L);

		when(gymFacade.getTrainingTypes()).thenReturn(List.of(singleType));

		// When & Then
		mockMvc.perform(get("/api/training-types"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$.length()").value(1))
			.andExpect(jsonPath("$[0].id").value(1))
			.andExpect(jsonPath("$[0].trainingType").value("CARDIO"));

		verify(gymFacade, times(1)).getTrainingTypes();
	}

	@Test
	@DisplayName("GET /api/training-types - Should handle facade exception gracefully")
	void testGetTrainingTypes_FacadeException() throws Exception {
		// Given
		when(gymFacade.getTrainingTypes()).thenThrow(new RuntimeException("Database connection failed"));

		// When & Then
		mockMvc.perform(get("/api/training-types"))
			.andExpect(status().isInternalServerError())
			.andExpect(jsonPath("$.error").value("Internal Server Error"));

		verify(gymFacade, times(1)).getTrainingTypes();
	}

	@Test
	@DisplayName("GET /api/training-types - Should verify response format")
	void testGetTrainingTypes_ResponseFormat() throws Exception {
		// Given
		when(gymFacade.getTrainingTypes()).thenReturn(trainingTypes);

		// When & Then
		mockMvc.perform(get("/api/training-types"))
			.andExpect(status().isOk())
			.andExpect(content().contentType("application/json"))
			.andExpect(jsonPath("$[*].id").exists())
			.andExpect(jsonPath("$[*].trainingType").exists());

		verify(gymFacade, times(1)).getTrainingTypes();
	}

}