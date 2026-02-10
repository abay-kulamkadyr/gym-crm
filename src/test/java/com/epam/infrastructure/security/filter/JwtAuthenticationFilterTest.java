package com.epam.infrastructure.security.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import com.epam.infrastructure.security.authentication_token.JwtAuthenticationToken;
import com.epam.infrastructure.security.port.out.TokenBlacklist;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenBlacklist tokenBlacklist;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        filter = new JwtAuthenticationFilter(authenticationManager, tokenBlacklist);
    }

    @Test
    void doFilterInternal_shouldProceed_whenNoAuthorizationHeader() throws Exception {
        // Given
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/api/test");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(authenticationManager, never()).authenticate(any());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_shouldReject_whenTokenBlacklisted() throws Exception {
        // Given
        String token = "blacklisted-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(request.getRequestURI()).thenReturn("/api/test");
        when(tokenBlacklist.isTokenRevoked(token)).thenReturn(true);

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(response).sendError(401, "Token has been revoked");
        verify(authenticationManager, never()).authenticate(any());
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldAuthenticate_whenTokenValid() throws Exception {
        // Given
        String token = "valid-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(request.getRequestURI()).thenReturn("/api/test");
        when(tokenBlacklist.isTokenRevoked(token)).thenReturn(false);

        UsernamePasswordAuthenticationToken authResult =
                new UsernamePasswordAuthenticationToken("user", null, Collections.emptyList());

        when(authenticationManager.authenticate(any(JwtAuthenticationToken.class)))
                .thenReturn(authResult);

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(authenticationManager).authenticate(any(JwtAuthenticationToken.class));
        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(
                "user", SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @Test
    void doFilterInternal_shouldClearContext_whenAuthenticationFails() {
        // Given
        String token = "invalid-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(request.getRequestURI()).thenReturn("/api/test");
        when(tokenBlacklist.isTokenRevoked(token)).thenReturn(false);
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Invalid token"));

        // When
        assertThrows(AuthenticationException.class, () -> filter.doFilterInternal(request, response, filterChain));

        // Then
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
