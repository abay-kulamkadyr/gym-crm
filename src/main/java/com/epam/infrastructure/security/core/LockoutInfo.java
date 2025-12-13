package com.epam.infrastructure.security.core;

import java.time.Duration;
import java.time.Instant;

public record LockoutInfo(String username, int attempts, Instant lockUntil) {
    public Duration remainingLockTime(Instant currentTime) {
        if (lockUntil == null) {
            return Duration.ZERO;
        }
        Duration remaining = Duration.between(currentTime, lockUntil);
        return remaining.isNegative() ? Duration.ZERO : remaining;
    }
}
