package com.epam.component;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import com.epam.infrastructure.security.adapter.JwtTokenServiceAdapter;
import com.epam.integration.base.ComponentTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class TraineeActivationComponentTest extends ComponentTestBase {

    private static final String BASE_URL = "/api/trainees";
    private static final String USERNAME = "James.Wilson";

    @Autowired
    private JwtTokenServiceAdapter tokenService;

    @Test
    void toggleActivation_withValidToken_shouldReturn200AndFlipStatus() {
        String token = tokenService.generateToken(USERNAME);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> before =
                testRestTemplate.exchange(BASE_URL + "/" + USERNAME, HttpMethod.GET, entity, Map.class);
        assertThat(before.getStatusCode()).isEqualTo(HttpStatus.OK);
        Boolean initialActive = (Boolean) before.getBody().get("active");

        ResponseEntity<Void> toggle = testRestTemplate.exchange(
                BASE_URL + "/" + USERNAME + "/activation", HttpMethod.PATCH, entity, Void.class);
        assertThat(toggle.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<Map> after =
                testRestTemplate.exchange(BASE_URL + "/" + USERNAME, HttpMethod.GET, entity, Map.class);
        Boolean newActive = (Boolean) after.getBody().get("active");
        assertThat(newActive).isNotEqualTo(initialActive);
    }

    @Test
    void toggleActivation_withoutToken_shouldReturn401() {
        ResponseEntity<Map> response = testRestTemplate.exchange(
                BASE_URL + "/" + USERNAME + "/activation", HttpMethod.PATCH, HttpEntity.EMPTY, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void toggleActivation_withTokenForDifferentUser_shouldReturn403() {
        String otherToken = tokenService.generateToken("David.Davis");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(otherToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = testRestTemplate.exchange(
                BASE_URL + "/" + USERNAME + "/activation", HttpMethod.PATCH, entity, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
