package com.epam.infrastructure.security.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.Instant;

import com.epam.infrastructure.security.authentication_token.JwtAuthenticationToken;
import com.epam.infrastructure.security.core.TokenData;
import com.epam.infrastructure.security.port.out.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationProviderTest {

    @Mock
    private TokenService tokenService;

    private JwtAuthenticationProvider provider;

    @BeforeEach
    void setUp() {
        provider = new JwtAuthenticationProvider(tokenService);
    }

    @Test
    void authenticate_shouldSucceed_whenTokenValid() {
        // Given
        String token = "valid-jwt-token";
        TokenData tokenData = new TokenData("testuser", Instant.now(), Instant.now().plusSeconds(3600));

        when(tokenService.validateToken(token)).thenReturn(tokenData);

        JwtAuthenticationToken authRequest = new JwtAuthenticationToken(token);

        // When
        Authentication result = provider.authenticate(authRequest);

        // Then
        assertTrue(result.isAuthenticated());
        assertEquals("testuser", result.getName());
        assertInstanceOf(UsernamePasswordAuthenticationToken.class, result);
    }

    @Test
    void authenticate_shouldThrowException_whenTokenInvalid() {
        // Given
        String token = "invalid-token";
        when(tokenService.validateToken(token)).thenThrow(IllegalArgumentException.class);

        JwtAuthenticationToken authRequest = new JwtAuthenticationToken(token);

        // When/Then
        assertThrows(BadCredentialsException.class, () -> provider.authenticate(authRequest));
    }

    @Test
    void supports_shouldReturnTrue_forJwtAuthenticationToken() {
        assertTrue(provider.supports(JwtAuthenticationToken.class));
    }

    @Test
    void supports_shouldReturnFalse_forOtherTokens() {
        assertFalse(provider.supports(UsernamePasswordAuthenticationToken.class));
    }

}
