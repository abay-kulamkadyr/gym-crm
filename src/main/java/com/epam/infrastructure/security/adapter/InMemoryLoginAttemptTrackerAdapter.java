package com.epam.infrastructure.security.adapter;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.epam.infrastructure.security.core.LockoutInfo;
import com.epam.infrastructure.security.port.out.LoginAttemptTracker;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InMemoryLoginAttemptTrackerAdapter implements LoginAttemptTracker {

    // Map: username => {attempts, lockedUntil}
    private final Map<String, LoginAttemptRecord> attempts = new ConcurrentHashMap<>();

    private final Clock clock;

    @Value("${security.login.max-attempts:3}")
    private int maxAttempts;

    @Value("${security.login.penalty:5m}")
    private Duration lockoutDuration;

    @Autowired
    InMemoryLoginAttemptTrackerAdapter(Clock clock) {
        this.clock = clock;
    }

    @Data
    private static class LoginAttemptRecord {

        int attempts = 0;

        Instant lockUntil;

    }

    @Override
    public void recordFailedAttempt(String username) {
        attempts.compute(username, (key, record) -> {
            if (record == null) {
                record = new LoginAttemptRecord();
            }

            // Don't increment if account is already locked
            if (record.lockUntil != null && record.lockUntil.isAfter(clock.instant())) {
                log.debug("Failed attempt for locked account: {}", username);
                return record;
            }

            record.attempts++;

            if (record.attempts >= maxAttempts) {
                record.lockUntil = clock.instant().plus(lockoutDuration);
                log.warn("Account locked after {} failed attempts: {}", record.attempts, username);
            }
            else {
                log.debug("Failed attempt #{} recorded for: {}", record.attempts, username);
            }
            return record;
        });
    }

    @Override
    public void clearAttempts(String username) {
        attempts.remove(username);
    }

    @Override
    public LockoutInfo getLockoutInfo(String username) {
        LoginAttemptRecord record = attempts.get(username);
        if (record == null) {
            return new LockoutInfo(username, 0, null);
        }
        return new LockoutInfo(username, record.attempts, record.lockUntil);
    }

    @Override
    public boolean isAccountLocked(String username) {
        LoginAttemptRecord record = attempts.get(username);
        if (record == null || record.lockUntil == null) {
            return false;
        }
        boolean locked = record.lockUntil.isAfter(clock.instant());
        if (!locked) {
            // Auto-cleanup expired lockouts
            attempts.remove(username);
            log.debug("Lockout expired for: {}", username);
        }
        return locked;
    }

    @Scheduled(cron = "0 0 * * * *") // At the start of every hour
    private void cleanupExpiredLockouts() {
        Instant now = clock.instant();
        int sizeBefore = attempts.size();

        attempts.entrySet().removeIf(entry -> {
            LoginAttemptRecord record = entry.getValue();
            return record.lockUntil != null && record.lockUntil.isBefore(now);
        });

        int removed = sizeBefore - attempts.size();
        if (removed > 0) {
            log.info("Lockout cleanup: removed {} expired entries, {} remaining", removed, attempts.size());
        }
    }

}
