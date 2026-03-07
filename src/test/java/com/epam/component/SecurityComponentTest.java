package com.epam.component;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import com.epam.infrastructure.security.adapter.JwtTokenServiceAdapter;
import com.epam.integration.base.ComponentTestBase;
import com.epam.interfaces.web.dto.response.TraineeResponse;
import com.epam.interfaces.web.dto.response.TrainerResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class SecurityComponentTest extends ComponentTestBase {

    private static final String TRAINEE_USERNAME = "David.Davis";
    private static final String TRAINER_USERNAME = "John.Smith";
    private static final String OTHER_USERNAME = "Emma.Johnson";

    @Autowired
    private JwtTokenServiceAdapter tokenService;

    @Test
    void getTraineeProfile_withoutToken_shouldReturn401() {
        ResponseEntity<Map> response = testRestTemplate.getForEntity("/api/trainees/" + TRAINEE_USERNAME, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void getTrainerProfile_withoutToken_shouldReturn401() {
        ResponseEntity<Map> response = testRestTemplate.getForEntity("/api/trainers/" + TRAINER_USERNAME, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void getTraineeProfile_withInvalidToken_shouldReturn401() {
        ResponseEntity<Map> response = getWithToken("/api/trainees/" + TRAINEE_USERNAME, "not.a.valid.jwt", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void getTrainerProfile_withInvalidToken_shouldReturn401() {
        ResponseEntity<Map> response = getWithToken("/api/trainers/" + TRAINER_USERNAME, "not.a.valid.jwt", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void getTraineeProfile_withTokenForAnotherUser_shouldReturn403() {
        String tokenForOther = tokenService.generateToken(OTHER_USERNAME);

        ResponseEntity<Map> response = getWithToken("/api/trainees/" + TRAINEE_USERNAME, tokenForOther, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void getTrainerProfile_withTokenForAnotherUser_shouldReturn403() {
        String tokenForOther = tokenService.generateToken(OTHER_USERNAME);

        ResponseEntity<Map> response = getWithToken("/api/trainers/" + TRAINER_USERNAME, tokenForOther, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void getTraineeProfile_withValidToken_shouldReturn200WithBody() {
        String token = tokenService.generateToken(TRAINEE_USERNAME);

        ResponseEntity<TraineeResponse> response =
                getWithToken("/api/trainees/" + TRAINEE_USERNAME, token, TraineeResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().firstName()).isEqualTo("David");
        assertThat(response.getBody().lastName()).isEqualTo("Davis");
        assertThat(response.getBody().active()).isTrue();
    }

    @Test
    void getTrainerProfile_withValidToken_shouldReturn200WithBody() {
        String token = tokenService.generateToken(TRAINER_USERNAME);

        ResponseEntity<TrainerResponse> response =
                getWithToken("/api/trainers/" + TRAINER_USERNAME, token, TrainerResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().firstName()).isEqualTo("John");
        assertThat(response.getBody().lastName()).isEqualTo("Smith");
    }

    @Test
    void traineeToken_cannotAccessTrainerProfile_shouldReturn403() {
        String traineeToken = tokenService.generateToken(TRAINEE_USERNAME);

        ResponseEntity<Map> response = getWithToken("/api/trainers/" + TRAINER_USERNAME, traineeToken, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void trainerToken_cannotAccessTraineeProfile_shouldReturn403() {
        String trainerToken = tokenService.generateToken(TRAINER_USERNAME);

        ResponseEntity<Map> response = getWithToken("/api/trainees/" + TRAINEE_USERNAME, trainerToken, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    private <T> ResponseEntity<T> getWithToken(String url, String token, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        return testRestTemplate.exchange(url, HttpMethod.GET, entity, responseType);
    }
}
