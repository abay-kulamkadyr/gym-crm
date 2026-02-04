package com.epam.interfaces.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import com.epam.application.facade.GymFacade;
import com.epam.application.request.CreateTrainingRequest;
import com.epam.domain.model.Trainer;
import com.epam.domain.model.TrainingType;
import com.epam.domain.model.TrainingTypeEnum;
import com.epam.interfaces.web.client.TrainerWorkloadInterface;
import com.epam.interfaces.web.client.request.TrainerWorkloadWebRequest;
import com.epam.interfaces.web.controller.impl.TrainingController;
import com.epam.interfaces.web.dto.request.AddTrainingRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TrainingController.class)
@TestPropertySource(properties = "spring.main.banner-mode=off")
@ImportAutoConfiguration(exclude = { SecurityAutoConfiguration.class })
@ActiveProfiles("test")
class TrainingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GymFacade gymFacade;

    @MockitoBean
    private TrainerWorkloadInterface workload;

    private final Trainer mockTrainer = new Trainer("john", "doe", true, new TrainingType(TrainingTypeEnum.CARDIO));

    @Test
    @DisplayName("POST /api/trainings - Should add training successfully")
    void testAddTraining_Success() throws Exception {
        // Given
        AddTrainingRequest request = new AddTrainingRequest("john.doe",
                "jane.smith",
                "Morning Workout",
                LocalDateTime.now().plusDays(7),
                60);

        when(gymFacade.getTrainerByUsername(anyString())).thenReturn(mockTrainer);
        when(gymFacade.createTraining(any(CreateTrainingRequest.class))).thenReturn(null);
        when(workload.processTrainerRequest(any(TrainerWorkloadWebRequest.class))).thenReturn(null);

        // When & Then
        mockMvc
                .perform(
                    post("/api/trainings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(gymFacade, times(1)).createTraining(any(CreateTrainingRequest.class));
    }

    @Test
    @DisplayName("POST /api/trainings - Should return 400 for missing trainee username")
    void testAddTraining_MissingTraineeUsername() throws Exception {
        // Given
        AddTrainingRequest request =
                new AddTrainingRequest("", "jane.smith", "Morning Workout", LocalDateTime.of(2024, 12, 1, 9, 0), 60);

        // When & Then
        mockMvc
                .perform(
                    post("/api/trainings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));

        verify(gymFacade, never()).createTraining(any());
    }

    @Test
    @DisplayName("POST /api/trainings - Should return 400 for missing trainer username")
    void testAddTraining_MissingTrainerUsername() throws Exception {
        // Given
        AddTrainingRequest request =
                new AddTrainingRequest("john.doe", "", "Morning Workout", LocalDateTime.of(2024, 12, 1, 9, 0), 60);

        // When & Then
        mockMvc
                .perform(
                    post("/api/trainings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));

        verify(gymFacade, never()).createTraining(any());
    }

    @Test
    @DisplayName("POST /api/trainings - Should return 400 for short training name")
    void testAddTraining_ShortTrainingName() throws Exception {
        // Given
        AddTrainingRequest request = new AddTrainingRequest("john.doe",
                "jane.smith",
                "AB", // Invalid:
                // less
                // than
                // 3
                // characters
                LocalDateTime.of(2024, 12, 1, 9, 0),
                60);

        // When & Then
        mockMvc
                .perform(
                    post("/api/trainings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));

        verify(gymFacade, never()).createTraining(any());
    }

    @Test
    @DisplayName("POST /api/trainings - Should return 400 for long training name")
    void testAddTraining_LongTrainingName() throws Exception {
        // Given
        String longName = "a".repeat(101); // More than 100 characters
        AddTrainingRequest request =
                new AddTrainingRequest("john.doe", "jane.smith", longName, LocalDateTime.of(2024, 12, 1, 9, 0), 60);

        // When & Then
        mockMvc
                .perform(
                    post("/api/trainings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));

        verify(gymFacade, never()).createTraining(any());
    }

    @Test
    @DisplayName("POST /api/trainings - Should return 400 for past training date")
    void testAddTraining_PastTrainingDate() throws Exception {
        // Given
        AddTrainingRequest request = new AddTrainingRequest("john.doe",
                "jane.smith",
                "Morning Workout",
                LocalDateTime.of(2020, 1, 1, 9, 0), // Past date
                60);

        // When & Then
        mockMvc
                .perform(
                    post("/api/trainings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));

        verify(gymFacade, never()).createTraining(any());
    }

    @Test
    @DisplayName("POST /api/trainings - Should return 400 for null training date")
    void testAddTraining_NullTrainingDate() throws Exception {
        // Given
        String requestJson = """
                             {
                               "traineeUsername": "john.doe",
                               "trainerUsername": "jane.smith",
                               "trainingName": "Morning Workout",
                               "trainingDurationMin": 60
                             }
                             """;

        // When & Then
        mockMvc
                .perform(post("/api/trainings").contentType(MediaType.APPLICATION_JSON).content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));

        verify(gymFacade, never()).createTraining(any());
    }

    @Test
    @DisplayName("POST /api/trainings - Should return 400 for duration less than 1")
    void testAddTraining_DurationTooShort() throws Exception {
        // Given
        AddTrainingRequest request = new AddTrainingRequest("john.doe",
                "jane.smith",
                "Morning Workout",
                LocalDateTime.of(2024, 12, 1, 9, 0),
                0 // Invalid: less than 1 minute
        );

        // When & Then
        mockMvc
                .perform(
                    post("/api/trainings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));

        verify(gymFacade, never()).createTraining(any());
    }

    @Test
    @DisplayName("POST /api/trainings - Should return 400 for duration more than 480")
    void testAddTraining_DurationTooLong() throws Exception {
        // Given
        AddTrainingRequest request = new AddTrainingRequest("john.doe",
                "jane.smith",
                "Morning Workout",
                LocalDateTime.of(2024, 12, 1, 9, 0),
                500 // Invalid: more than 480 minutes
        // (8 hours)
        );

        // When & Then
        mockMvc
                .perform(
                    post("/api/trainings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));

        verify(gymFacade, never()).createTraining(any());
    }

    @Test
    @DisplayName("POST /api/trainings - Should return 400 for null duration")
    void testAddTraining_NullDuration() throws Exception {
        // Given
        String requestJson = """
                             {
                               "traineeUsername": "john.doe",
                               "trainerUsername": "jane.smith",
                               "trainingName": "Morning Workout",
                               "trainingDate": "2024-12-01T09:00:00"
                             }
                             """;

        // When & Then
        mockMvc
                .perform(post("/api/trainings").contentType(MediaType.APPLICATION_JSON).content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));

        verify(gymFacade, never()).createTraining(any());
    }

    @Test
    @DisplayName("POST /api/trainings - Should accept minimum valid duration")
    void testAddTraining_MinimumDuration() throws Exception {
        // Given
        AddTrainingRequest request =
                new AddTrainingRequest("john.doe", "jane.smith", "Quick Session", LocalDateTime.now().plusDays(7), 1 // Minimum valid: 1 minute
                );

        when(gymFacade.getTrainerByUsername(anyString())).thenReturn(mockTrainer);
        when(gymFacade.createTraining(any(CreateTrainingRequest.class))).thenReturn(null);
        when(workload.processTrainerRequest(any(TrainerWorkloadWebRequest.class))).thenReturn(null);

        // When & Then
        mockMvc
                .perform(
                    post("/api/trainings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(gymFacade, times(1)).createTraining(any(CreateTrainingRequest.class));
    }

    @Test
    @DisplayName("POST /api/trainings - Should accept maximum valid duration")
    void testAddTraining_MaximumDuration() throws Exception {
        // Given
        AddTrainingRequest request = new AddTrainingRequest("john.doe",
                "jane.smith",
                "All Day Training",
                LocalDateTime.now().plusDays(7),
                480 // Maximum valid: 480 minutes (8
        // hours)
        );

        when(gymFacade.getTrainerByUsername(anyString())).thenReturn(mockTrainer);
        when(gymFacade.createTraining(any(CreateTrainingRequest.class))).thenReturn(null);
        when(workload.processTrainerRequest(any())).thenReturn(null);

        // When & Then
        mockMvc
                .perform(
                    post("/api/trainings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(gymFacade, times(1)).createTraining(any(CreateTrainingRequest.class));
    }

}
