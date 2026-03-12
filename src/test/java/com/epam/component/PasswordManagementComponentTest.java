package com.epam.component;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import com.epam.infrastructure.security.adapter.JwtTokenServiceAdapter;
import com.epam.integration.base.ComponentTestBase;
import com.epam.interfaces.web.dto.request.ChangePasswordRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

class PasswordManagementComponentTest extends ComponentTestBase {

    private static final String BASE_URL = "/api/auth/password";
    private static final String USERNAME = "Sarah.Brown";

    @Autowired
    private JwtTokenServiceAdapter tokenService;

    @Test
    void changePassword_withCorrectOldPassword_shouldReturn200() {
        String token = tokenService.generateToken(USERNAME);

        ChangePasswordRequest request = new ChangePasswordRequest(USERNAME, "pass123456", "newValidPass99");

        ResponseEntity<Void> response = putWithToken(token, request, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void changePassword_withWrongOldPassword_shouldReturn401() {
        String token = tokenService.generateToken(USERNAME);

        ChangePasswordRequest request =
                new ChangePasswordRequest(USERNAME, "definitelyWrongPassword", "newValidPass99");

        ResponseEntity<Map> response = putWithToken(token, request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void changePassword_withoutToken_shouldReturn401() {
        ChangePasswordRequest request = new ChangePasswordRequest(USERNAME, "pass123456", "newValidPass99");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ChangePasswordRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<Map> response = testRestTemplate.exchange(BASE_URL, HttpMethod.PUT, entity, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void changePassword_withShortNewPassword_shouldReturn400() {
        String token = tokenService.generateToken(USERNAME);

        ChangePasswordRequest request = new ChangePasswordRequest(USERNAME, "pass123456", "short");

        ResponseEntity<Map> response = putWithToken(token, request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private <T> ResponseEntity<T> putWithToken(String token, Object body, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> entity = new HttpEntity<>(body, headers);
        return testRestTemplate.exchange(BASE_URL, HttpMethod.PUT, entity, responseType);
    }
}
