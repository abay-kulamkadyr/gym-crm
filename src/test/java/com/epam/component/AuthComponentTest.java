package com.epam.component;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import com.epam.infrastructure.security.adapter.JwtTokenServiceAdapter;
import com.epam.integration.base.ComponentTestBase;
import com.epam.interfaces.web.dto.request.LoginRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class AuthComponentTest extends ComponentTestBase {

    private static final String BASE_URL = "/api/auth";
    private static final String USERNAME = "John.Smith";
    private static final String CORRECT_PASSWORD = "pass123456";
    private static final String WRONG_PASSWORD = "wrongpassword";

    @Value("${security.login.max-attempts:3}")
    private int maxLoginAttempts;

    @Autowired
    private JwtTokenServiceAdapter tokenService;

    @Test
    void loginWithValidCredentials_shouldReturnToken() {
        LoginRequest request = new LoginRequest(USERNAME, CORRECT_PASSWORD);

        ResponseEntity<Map> response = testRestTemplate.postForEntity(BASE_URL + "/login", request, Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsKey("token");
        assertThat(response.getBody().get("token")).isNotNull();
    }

    @Test
    void loginWithWrongPassword_shouldReturn401() {
        LoginRequest request = new LoginRequest(USERNAME, WRONG_PASSWORD);

        ResponseEntity<Map> response = testRestTemplate.postForEntity(BASE_URL + "/login", request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void loginWithNonExistentUser_shouldReturn401() {
        LoginRequest request = new LoginRequest("NonExistent.User", WRONG_PASSWORD);

        ResponseEntity<Map> response = testRestTemplate.postForEntity(BASE_URL + "/login", request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void loginWithBlankUsername_shouldReturn400() {
        LoginRequest request = new LoginRequest("", CORRECT_PASSWORD);

        ResponseEntity<Map> response = testRestTemplate.postForEntity(BASE_URL + "/login", request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void loginAfterMultipleWrongAttempts_shouldReturn429() {
        String lockedUser = "Emma.Johnson";
        LoginRequest badRequest = new LoginRequest(lockedUser, WRONG_PASSWORD);

        for (int i = 0; i < maxLoginAttempts; i++) {
            testRestTemplate.postForEntity(BASE_URL + "/login", badRequest, Map.class);
        }

        ResponseEntity<Map> blockedResponse =
                testRestTemplate.postForEntity(BASE_URL + "/login", badRequest, Map.class);

        assertThat(blockedResponse.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(blockedResponse.getBody()).isNotNull();
    }

    @Test
    void logoutWithValidToken_shouldReturn204() {
        String token = tokenService.generateToken(USERNAME);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Void> response = testRestTemplate.postForEntity(BASE_URL + "/logout", entity, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void logoutWithoutToken_shouldReturn401() {
        ResponseEntity<Map> response = testRestTemplate.postForEntity(BASE_URL + "/logout", null, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void logoutWithInvalidToken_shouldReturn401() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("invalid.jwt.token");
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = testRestTemplate.postForEntity(BASE_URL + "/logout", entity, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void loggedOutToken_shouldBeRejectedOnSubsequentRequest() {
        String token = tokenService.generateToken(USERNAME);

        // Logout
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> logoutEntity = new HttpEntity<>(headers);
        testRestTemplate.postForEntity(BASE_URL + "/logout", logoutEntity, Void.class);

        // Try to use the same token again
        ResponseEntity<Map> response = testRestTemplate.exchange(
                "/api/trainees/" + USERNAME, org.springframework.http.HttpMethod.GET, logoutEntity, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
