package com.epam.infrastructure.security.adapter;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.epam.infrastructure.security.port.out.TokenBlacklist;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InMemoryTokenBlacklistAdapter implements TokenBlacklist {

    // Map: token => expiration timestamp
    // TODO: To be replaced, can cause OutOfMemoryError if attacked
    private final Map<String, Instant> blacklist = new ConcurrentHashMap<>();

    private final Clock clock;

    @Autowired
    InMemoryTokenBlacklistAdapter(Clock clock) {
        this.clock = clock;
    }

    @Override
    public void revokeToken(String token, Instant expiresAt) {
        blacklist.put(token, expiresAt);
        log.info("Token revoked, will expire at: {}", expiresAt);
        log.debug("Blacklist size: {}", blacklist.size());
    }

    @Override
    public boolean isTokenRevoked(String token) {
        Instant expiry = blacklist.get(token);

        if (expiry == null) {
            return false;
        }

        // Auto-cleanup on lookup
        if (expiry.isBefore(clock.instant())) {
            blacklist.remove(token);
            log.trace("Removed expired token from blacklist during lookup");
            return false;
        }

        log.debug("Token found in blacklist");
        return true;
    }

    @Scheduled(fixedRate = 900000) // 15 minutes
    public void cleanupExpiredTokens() {
        Instant now = clock.instant();
        blacklist.entrySet().removeIf(entry -> entry.getValue().isBefore(now));
    }

}
