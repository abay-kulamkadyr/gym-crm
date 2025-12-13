package com.epam.infrastructure.security.adapter;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import com.epam.infrastructure.security.core.LockoutInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class InMemoryLoginAttemptTrackerAdapterTest {

    private Clock clock;

    private InMemoryLoginAttemptTrackerAdapter tracker;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(Instant.parse("2025-01-01T12:00:00Z"), ZoneId.systemDefault());
        tracker = new InMemoryLoginAttemptTrackerAdapter(clock);
        ReflectionTestUtils.setField(tracker, "maxAttempts", 3);
        ReflectionTestUtils.setField(tracker, "lockoutDuration", Duration.ofMinutes(5));
    }

    @Test
    void recordFailedAttempt_shouldIncrementCounter() {
        tracker.recordFailedAttempt("user");

        LockoutInfo info = tracker.getLockoutInfo("user");
        assertEquals(1, info.attempts());
        assertNull(info.lockUntil());
    }

    @Test
    void recordFailedAttempt_shouldLockAccount_afterMaxAttempts() {
        tracker.recordFailedAttempt("user");
        tracker.recordFailedAttempt("user");
        tracker.recordFailedAttempt("user");

        assertTrue(tracker.isAccountLocked("user"));
        LockoutInfo info = tracker.getLockoutInfo("user");
        assertEquals(3, info.attempts());
        assertNotNull(info.lockUntil());
    }

    @Test
    void clearAttempts_shouldRemoveTrackingRecord() {
        tracker.recordFailedAttempt("user");
        tracker.clearAttempts("user");

        LockoutInfo info = tracker.getLockoutInfo("user");
        assertEquals(0, info.attempts());
    }

    @Test
    void isAccountLocked_shouldReturnFalse_afterLockoutExpires() {
        // Lock the account
        for (int i = 0; i < 3; i++) {
            tracker.recordFailedAttempt("user");
        }
        assertTrue(tracker.isAccountLocked("user"));

        // Move clock forward past lockout
        Clock futureClock = Clock.fixed(clock.instant().plus(Duration.ofMinutes(6)), ZoneId.systemDefault());
        ReflectionTestUtils.setField(tracker, "clock", futureClock);

        assertFalse(tracker.isAccountLocked("user"));
    }

}
