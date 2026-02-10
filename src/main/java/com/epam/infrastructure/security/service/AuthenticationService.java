package com.epam.infrastructure.security.service;

import java.time.Clock;
import java.time.Duration;

import com.epam.infrastructure.security.core.AuthenticationResult;
import com.epam.infrastructure.security.core.LockoutInfo;
import com.epam.infrastructure.security.core.TokenData;
import com.epam.infrastructure.security.event.UserLoginAttemptEvent;
import com.epam.infrastructure.security.event.UserLoginFailedEvent;
import com.epam.infrastructure.security.event.UserLoginSuccessEvent;
import com.epam.infrastructure.security.port.in.AuthenticationUseCase;
import com.epam.infrastructure.security.port.out.LoginAttemptTracker;
import com.epam.infrastructure.security.port.out.TokenBlacklist;
import com.epam.infrastructure.security.port.out.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuthenticationService implements AuthenticationUseCase {

    private final AuthenticationManager authenticationManager;

    private final TokenService tokenService;

    private final TokenBlacklist tokenBlacklist;

    private final LoginAttemptTracker loginAttemptTracker;

    private final Clock clock;

    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    AuthenticationService(
            AuthenticationManager authManager,
            TokenService tokenService,
            TokenBlacklist tokenBlacklist,
            LoginAttemptTracker loginAttemptTracker,
            Clock clock,
            ApplicationEventPublisher eventPublisher) {
        this.authenticationManager = authManager;
        this.tokenService = tokenService;
        this.tokenBlacklist = tokenBlacklist;
        this.loginAttemptTracker = loginAttemptTracker;
        this.clock = clock;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public AuthenticationResult authenticate(String username, String password) throws AuthenticationException {
        log.info("Authentication attempt for user: {}", username);
        eventPublisher.publishEvent(new UserLoginAttemptEvent(username));

        // Check brute force protection
        if (loginAttemptTracker.isAccountLocked(username)) {
            LockoutInfo lockout = loginAttemptTracker.getLockoutInfo(username);
            Duration remaining = lockout.remainingLockTime(clock.instant());
            String message = formatLockoutMessage(remaining);

            log.warn("Authentication blocked - account locked: {}", username);
            throw new LockedException(message);
        }

        try {
            // Delegate Authentication to Spring Security
            Authentication auth =
                    authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));

            // Success - clear attempts and generate token
            loginAttemptTracker.clearAttempts(username);
            String token = tokenService.generateToken(auth.getName());

            // Publish event for metrics
            eventPublisher.publishEvent(new UserLoginSuccessEvent(username));

            log.info("Authentication successful for user: {}", username);
            return new AuthenticationResult(username, token);

        } catch (AuthenticationException e) {
            // Failed - record attempt
            loginAttemptTracker.recordFailedAttempt(username);

            // Publish event for metrics
            eventPublisher.publishEvent(new UserLoginFailedEvent(username));

            log.warn("Authentication failed for user: {} - {}", username, e.getMessage());
            throw e;
        }
    }

    @Override
    public void logout(String token) {
        if (token == null || token.isBlank()) {
            log.warn("Logout attempted with null/empty token");
            return;
        }

        try {
            TokenData tokenData = tokenService.parseToken(token);
            tokenBlacklist.revokeToken(token, tokenData.expiresAt());
            log.info("User logged out successfully: {}", tokenData.username());
        } catch (IllegalArgumentException e) {
            log.error("Error during logout: {}", e.getMessage());
        }
    }

    private String formatLockoutMessage(Duration remaining) {
        long minutes = remaining.toMinutes();
        long seconds = remaining.minusMinutes(minutes).getSeconds();
        return String.format(
                "Account temporarily locked due to multiple failed login attempts. "
                        + "Please try again in %d minutes and %d seconds.",
                minutes, seconds);
    }
}
