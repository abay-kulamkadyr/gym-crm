package com.epam.interfaces.web.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.infrastructure.security.core.AuthenticationResult;
import com.epam.infrastructure.security.port.in.AuthenticationUseCase;
import com.epam.infrastructure.security.port.in.PasswordManagementUseCase;
import com.epam.interfaces.web.controller.impl.UserController;
import com.epam.interfaces.web.dto.request.ChangePasswordRequest;
import com.epam.interfaces.web.dto.request.LoginRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@TestPropertySource(properties = "spring.main.banner-mode=off")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticationUseCase authenticationUseCase;

    @MockitoBean
    private PasswordManagementUseCase passwordManagementUseCase;

    private static final String TEST_USERNAME = "user";

    private static final String TEST_PASSWORD = "password";

    private static final String NEW_PASSWORD = "newPassword456";

    private static final String TEST_TOKEN = "eyJhbGciOiJIUzI1NiJ9.test.jwt";

    @Test
    @WithMockUser
    @DisplayName("POST /api/auth/login - Should login successfully and return JWT")
    void testLogin_Success() throws Exception {
        LoginRequest request = new LoginRequest(TEST_USERNAME, TEST_PASSWORD);
        AuthenticationResult authResult = new AuthenticationResult(TEST_USERNAME, TEST_TOKEN);

        when(authenticationUseCase.authenticate(TEST_USERNAME, TEST_PASSWORD)).thenReturn(authResult);

        mockMvc
                .perform(
                    post("/api/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(TEST_TOKEN));

        verify(authenticationUseCase).authenticate(TEST_USERNAME, TEST_PASSWORD);
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/auth/login - Should return 401 for invalid credentials")
    void testLogin_InvalidCredentials() throws Exception {
        LoginRequest request = new LoginRequest(TEST_USERNAME, "wrongpassword");

        when(authenticationUseCase.authenticate(TEST_USERNAME, "wrongpassword"))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        mockMvc
                .perform(
                    post("/api/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Authentication Failed"))
                .andExpect(jsonPath("$.message").value("Invalid credentials"));

        verify(authenticationUseCase).authenticate(TEST_USERNAME, "wrongpassword");
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/auth/login - Should return 400 for blank username")
    void testLogin_BlankUsername() throws Exception {
        LoginRequest request = new LoginRequest("", TEST_PASSWORD);

        mockMvc
                .perform(
                    post("/api/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));

        verify(authenticationUseCase, never()).authenticate(anyString(), anyString());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/auth/login - Should return 400 for blank password")
    void testLogin_BlankPassword() throws Exception {
        LoginRequest request = new LoginRequest(TEST_USERNAME, "");

        mockMvc
                .perform(
                    post("/api/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));

        verify(authenticationUseCase, never()).authenticate(anyString(), anyString());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/auth/login - Should return 429 for locked account")
    void testLogin_AccountLocked() throws Exception {
        LoginRequest request = new LoginRequest(TEST_USERNAME, TEST_PASSWORD);

        when(authenticationUseCase.authenticate(TEST_USERNAME, TEST_PASSWORD))
                .thenThrow(new LockedException("Account locked. Try again in 5 minutes"));

        mockMvc
                .perform(
                    post("/api/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.message").value(containsString("locked")));

        verify(authenticationUseCase).authenticate(TEST_USERNAME, TEST_PASSWORD);
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/auth/logout - Should logout successfully with valid token")
    void testLogout_Success() throws Exception {
        String authHeader = "Bearer " + TEST_TOKEN;

        mockMvc
                .perform(post("/api/auth/logout").with(csrf()).header("Authorization", authHeader))
                .andExpect(status().isNoContent());

        verify(authenticationUseCase).logout(TEST_TOKEN);
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/auth/logout - Should return 400 for missing Authorization header")
    void testLogout_MissingAuthorizationHeader() throws Exception {
        mockMvc.perform(post("/api/auth/logout").with(csrf())).andExpect(status().isBadRequest());

        verify(authenticationUseCase, never()).logout(anyString());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/auth/logout - Should handle malformed Authorization header")
    void testLogout_MalformedAuthorizationHeader() throws Exception {
        mockMvc
                .perform(post("/api/auth/logout").with(csrf()).header("Authorization", "InvalidFormat"))
                .andExpect(status().isNoContent());

        verify(authenticationUseCase, never()).logout(anyString());
    }

    @Test
    @WithMockUser
    @DisplayName("PUT /api/auth/password - Should change password successfully")
    void testChangePassword_Success() throws Exception {
        // Given
        ChangePasswordRequest request = new ChangePasswordRequest(TEST_USERNAME, TEST_PASSWORD, NEW_PASSWORD);

        // When
        mockMvc
                .perform(
                    put("/api/auth/password")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Verify
        verify(passwordManagementUseCase).changePassword(TEST_USERNAME, TEST_PASSWORD, NEW_PASSWORD);
    }

    @Test
    @WithMockUser(username = "different.user")
    @DisplayName("PUT /api/auth/password - Should use authenticated username")
    void testChangePassword_UsesAuthenticatedUser() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest(TEST_USERNAME, TEST_PASSWORD, NEW_PASSWORD);

        mockMvc
                .perform(
                    put("/api/auth/password")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Verify
        verify(passwordManagementUseCase).changePassword("different.user", TEST_PASSWORD, NEW_PASSWORD);
    }

    @Test
    @DisplayName("PUT /api/auth/password - Should return 401 when not authenticated")
    void testChangePassword_NotAuthenticated() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest(TEST_USERNAME, TEST_PASSWORD, NEW_PASSWORD);

        mockMvc
                .perform(
                    put("/api/auth/password")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(passwordManagementUseCase, never()).changePassword(anyString(), anyString(), anyString());
    }

    @Test
    @WithMockUser(username = TEST_USERNAME)
    @DisplayName("PUT /api/auth/password - Should return 400 for blank old password")
    void testChangePassword_BlankOldPassword() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest(TEST_USERNAME, "", NEW_PASSWORD);

        mockMvc
                .perform(
                    put("/api/auth/password")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));

        verify(passwordManagementUseCase, never()).changePassword(anyString(), anyString(), anyString());
    }

    @Test
    @WithMockUser(username = TEST_USERNAME)
    @DisplayName("PUT /api/auth/password - Should return 400 for short new password")
    void testChangePassword_ShortNewPassword() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest(TEST_USERNAME, TEST_PASSWORD, "short");

        mockMvc
                .perform(
                    put("/api/auth/password")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));

        verify(passwordManagementUseCase, never()).changePassword(anyString(), anyString(), anyString());
    }

    @Test
    @WithMockUser(username = TEST_USERNAME)
    @DisplayName("PUT /api/auth/password - Should return 401 when old password is incorrect")
    void testChangePassword_IncorrectOldPassword() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest(TEST_USERNAME, "wrongOldPassword", NEW_PASSWORD);

        doThrow(new BadCredentialsException("Current password is incorrect"))
                .when(passwordManagementUseCase)
                .changePassword(TEST_USERNAME, "wrongOldPassword", NEW_PASSWORD);

        mockMvc
                .perform(
                    put("/api/auth/password")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Current password is incorrect"));

        verify(passwordManagementUseCase).changePassword(TEST_USERNAME, "wrongOldPassword", NEW_PASSWORD);
    }

}
