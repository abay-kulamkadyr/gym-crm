package com.epam.infrastructure.security.port.out;

import java.time.Instant;

public interface TokenBlacklist {

    void revokeToken(String token, Instant expiresAt);

    boolean isTokenRevoked(String token);
}
