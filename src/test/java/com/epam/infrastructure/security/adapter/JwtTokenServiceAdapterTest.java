package com.epam.infrastructure.security.adapter;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import com.epam.infrastructure.security.core.TokenData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JwtTokenServiceAdapterTest {

	private static final String SECRET = "test-secret-key-with-at-least-256-bits-for-hs256";

	private static final Duration LIFETIME = Duration.ofHours(1);

	private Clock clock;

	private JwtTokenServiceAdapter adapter;

	@BeforeEach
	void setUp() {
		Instant fixedInstant = Instant.parse("2025-01-01T12:00:00Z");
		clock = Clock.fixed(fixedInstant, ZoneId.systemDefault());
		adapter = new JwtTokenServiceAdapter(SECRET, LIFETIME, clock);
	}

	@Test
	void generateToken_shouldCreateValidToken() {
		// Given
		String token = adapter.generateToken("testuser");

		// When
		assertNotNull(token);
		TokenData tokenData = adapter.validateToken(token);

		// Then
		assertDoesNotThrow(() -> adapter.validateToken(token));
		assertThat(tokenData.username()).isEqualTo("testuser");
		assertThat(tokenData.issuedAt()).isEqualTo(clock.instant());
		assertThat(tokenData.expiresAt()).isEqualTo(clock.instant().plus(LIFETIME));
	}

	@Test
	void parseToken_shouldExtractCorrectUsername() {
		// Given
		String token = adapter.generateToken("testuser");

		// When
		TokenData tokenData = adapter.parseToken(token);

		// Then
		assertEquals("testuser", tokenData.username());
		assertEquals(clock.instant(), tokenData.issuedAt());
		assertEquals(clock.instant().plus(LIFETIME), tokenData.expiresAt());
	}

	@Test
	void validateToken_shouldReturnFalse_forExpiredToken() {
		// Given
		String token = adapter.generateToken("testuser");

		// When: Move clock forward past expiration
		Instant futureTime = clock.instant().plus(LIFETIME).plus(Duration.ofMinutes(1));
		Clock futureClock = Clock.fixed(futureTime, ZoneId.systemDefault());
		JwtTokenServiceAdapter futureAdapter = new JwtTokenServiceAdapter(SECRET, LIFETIME, futureClock);

		// Then
		assertThrows(IllegalArgumentException.class, () -> futureAdapter.validateToken(token));
	}

	@Test
	void validateToken_shouldReturnFalse_forInvalidToken() {
		assertThrows(IllegalArgumentException.class, () -> adapter.validateToken("invalid.token.here"));
	}

	@Test
	void parseToken_shouldThrowException_forInvalidToken() {
		assertThrows(IllegalArgumentException.class, () -> adapter.parseToken("invalid.token.here"));
	}

}
