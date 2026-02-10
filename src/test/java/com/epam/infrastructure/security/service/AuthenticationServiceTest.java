package com.epam.infrastructure.security.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import com.epam.infrastructure.security.core.AuthenticationResult;
import com.epam.infrastructure.security.core.LockoutInfo;
import com.epam.infrastructure.security.core.TokenData;
import com.epam.infrastructure.security.port.out.LoginAttemptTracker;
import com.epam.infrastructure.security.port.out.TokenBlacklist;
import com.epam.infrastructure.security.port.out.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenService tokenService;

    @Mock
    private TokenBlacklist tokenBlacklist;

    @Mock
    private LoginAttemptTracker loginAttemptTracker;

    @Mock
    private Authentication authentication;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private Clock clock;

    private AuthenticationService service;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(Instant.parse("2025-01-01T12:00:00Z"), ZoneId.systemDefault());
        service = new AuthenticationService(
                authenticationManager, tokenService, tokenBlacklist, loginAttemptTracker, clock, eventPublisher);
    }

    @Test
    void authenticate_shouldSucceed_whenCredentialsValid() {
        // Given
        String username = "testuser";
        String password = "password";
        String expectedToken = "jwt-token-123";

        when(loginAttemptTracker.isAccountLocked(username)).thenReturn(false);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getName()).thenReturn(username);
        when(tokenService.generateToken(username)).thenReturn(expectedToken);

        // When
        AuthenticationResult result = service.authenticate(username, password);

        // Then
        assertNotNull(result);
        assertEquals(username, result.username());
        assertEquals(expectedToken, result.token());

        verify(loginAttemptTracker).clearAttempts(username);
        verify(tokenService).generateToken(username);
    }

    @Test
    void authenticate_shouldThrowLockedException_whenAccountLocked() {
        // Given
        String username = "lockeduser";
        LockoutInfo lockout = new LockoutInfo(username, 3, clock.instant().plus(Duration.ofMinutes(5)));

        when(loginAttemptTracker.isAccountLocked(username)).thenReturn(true);
        when(loginAttemptTracker.getLockoutInfo(username)).thenReturn(lockout);

        // When/Then
        assertThrows(LockedException.class, () -> service.authenticate(username, "password"));

        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    void authenticate_shouldRecordFailedAttempt_whenAuthenticationFails() {
        // Given
        String username = "testuser";
        when(loginAttemptTracker.isAccountLocked(username)).thenReturn(false);
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        // When/Then
        assertThrows(BadCredentialsException.class, () -> service.authenticate(username, "wrong-password"));

        verify(loginAttemptTracker).recordFailedAttempt(username);
        verify(tokenService, never()).generateToken(any());
    }

    @Test
    void logout_shouldRevokeToken() {
        // Given
        String token = "jwt-token-123";
        TokenData tokenData =
                new TokenData("user", clock.instant(), clock.instant().plus(Duration.ofHours(1)));

        when(tokenService.parseToken(token)).thenReturn(tokenData);

        // When
        service.logout(token);

        // Then
        verify(tokenBlacklist).revokeToken(token, tokenData.expiresAt());
    }

    @Test
    void logout_shouldHandleNullToken() {
        // When
        service.logout(null);

        // Then
        verify(tokenBlacklist, never()).revokeToken(any(), any());
    }
}
