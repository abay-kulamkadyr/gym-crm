package com.epam.component;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import com.epam.domain.model.TrainingTypeEnum;
import com.epam.infrastructure.security.adapter.JwtTokenServiceAdapter;
import com.epam.integration.base.ComponentTestBase;
import com.epam.interfaces.web.dto.request.TrainerRegistrationRequest;
import com.epam.interfaces.web.dto.request.UpdateTrainerRequest;
import com.epam.interfaces.web.dto.response.CredentialsResponse;
import com.epam.interfaces.web.dto.response.TrainerResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

class TrainerComponentTest extends ComponentTestBase {

    private static final String BASE_URL = "/api/trainers";
    private static final String EXISTING_TRAINER = "John.Smith";

    @Autowired
    private JwtTokenServiceAdapter tokenService;

    @Test
    void registerTrainer_withValidData_shouldReturn201WithCredentials() {
        TrainerRegistrationRequest request = new TrainerRegistrationRequest("New", "Trainer", TrainingTypeEnum.BOXING);

        ResponseEntity<CredentialsResponse> response =
                testRestTemplate.postForEntity(BASE_URL, request, CredentialsResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().username()).isEqualTo("New.Trainer");
        assertThat(response.getBody().password()).isNotNull().hasSize(10);
    }

    @Test
    void registerTrainer_withMissingFirstName_shouldReturn400() {
        String invalidJson =
                """
                {
                  "lastName": "Trainer",
                  "specialization": "BOXING"
                }
                """;

        ResponseEntity<Map> response = postJson(invalidJson);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void registerTrainer_withInvalidSpecialization_shouldReturn400() {
        String invalidJson =
                """
                {
                  "firstName": "New",
                  "lastName": "Trainer",
                  "specialization": "INVALID_TYPE"
                }
                """;

        ResponseEntity<Map> response = postJson(invalidJson);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void registerTrainer_duplicateName_shouldAppendSuffix() {
        TrainerRegistrationRequest request = new TrainerRegistrationRequest("John", "Smith", TrainingTypeEnum.CARDIO);

        ResponseEntity<CredentialsResponse> response =
                testRestTemplate.postForEntity(BASE_URL, request, CredentialsResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        // John.Smith already exists in seed data, so suffix is appended
        assertThat(response.getBody().username()).startsWith("John.Smith");
        assertThat(response.getBody().username()).isNotEqualTo("John.Smith");
    }

    @Test
    void updateTrainer_withValidToken_shouldReturn200WithUpdatedData() {
        String token = tokenService.generateToken(EXISTING_TRAINER);
        UpdateTrainerRequest request = new UpdateTrainerRequest("John", "Smith", TrainingTypeEnum.YOGA, true);

        ResponseEntity<TrainerResponse> response =
                putWithToken(BASE_URL + "/" + EXISTING_TRAINER, token, request, TrainerResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().firstName()).isEqualTo("John");
        assertThat(response.getBody().specialization()).isEqualTo(TrainingTypeEnum.YOGA);
    }

    @Test
    void updateTrainer_withoutToken_shouldReturn401() {
        UpdateTrainerRequest request = new UpdateTrainerRequest("John", "Smith", TrainingTypeEnum.YOGA, true);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UpdateTrainerRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<Map> response =
                testRestTemplate.exchange(BASE_URL + "/" + EXISTING_TRAINER, HttpMethod.PUT, entity, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void updateTrainer_withTokenForDifferentUser_shouldReturn403() {
        String otherToken = tokenService.generateToken("Emma.Johnson");
        UpdateTrainerRequest request = new UpdateTrainerRequest("John", "Smith", TrainingTypeEnum.YOGA, true);

        ResponseEntity<Map> response = putWithToken(BASE_URL + "/" + EXISTING_TRAINER, otherToken, request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void getTrainer_withValidToken_shouldReturn200() {
        String token = tokenService.generateToken(EXISTING_TRAINER);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<TrainerResponse> response = testRestTemplate.exchange(
                BASE_URL + "/" + EXISTING_TRAINER, HttpMethod.GET, entity, TrainerResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().firstName()).isEqualTo("John");
    }

    @Test
    void toggleTrainerActivation_withValidToken_shouldReturn200() {
        String token = tokenService.generateToken(EXISTING_TRAINER);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Void> response = testRestTemplate.exchange(
                BASE_URL + "/" + EXISTING_TRAINER + "/activation", HttpMethod.PATCH, entity, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private ResponseEntity<Map> postJson(String json) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(json, headers);
        return testRestTemplate.postForEntity(BASE_URL, entity, Map.class);
    }

    private <T> ResponseEntity<T> putWithToken(String url, String token, Object body, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> entity = new HttpEntity<>(body, headers);
        return testRestTemplate.exchange(url, HttpMethod.PUT, entity, responseType);
    }
}
