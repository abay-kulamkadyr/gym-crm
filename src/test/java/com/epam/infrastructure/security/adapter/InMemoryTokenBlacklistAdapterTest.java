package com.epam.infrastructure.security.adapter;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class InMemoryTokenBlacklistAdapterTest {

	private Clock clock;

	private InMemoryTokenBlacklistAdapter adapter;

	@BeforeEach
	void setUp() {
		clock = Clock.fixed(Instant.parse("2025-01-01T12:00:00Z"), ZoneId.systemDefault());
		adapter = new InMemoryTokenBlacklistAdapter(clock);
	}

	@Test
	void isTokenRevoked_shouldReturnFalse_forNonRevokedToken() {
		assertFalse(adapter.isTokenRevoked("some-token"));
	}

	@Test
	void isTokenRevoked_shouldReturnTrue_forRevokedToken() {
		Instant expiresAt = clock.instant().plus(Duration.ofHours(1));

		adapter.revokeToken("token123", expiresAt);

		assertTrue(adapter.isTokenRevoked("token123"));
	}

	@Test
	void isTokenRevoked_shouldReturnFalse_afterExpiration() {
		// Revoke token that expires at T+1 hour
		Instant expiresAt = clock.instant().plus(Duration.ofHours(1));
		adapter.revokeToken("token123", expiresAt);

		// Move clock to T+2 hours
		Clock futureClock = Clock.fixed(clock.instant().plus(Duration.ofHours(2)), ZoneId.systemDefault());
		ReflectionTestUtils.setField(adapter, "clock", futureClock);

		// Token should be auto-removed
		adapter.revokeToken("token123", expiresAt);
		assertFalse(adapter.isTokenRevoked("token123"));
	}

}
