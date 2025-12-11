package com.epam.infrastructure.security.adapter;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;

import com.epam.infrastructure.security.core.TokenData;
import com.epam.infrastructure.security.port.out.TokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtTokenServiceAdapter implements TokenService {

	private final Duration tokenLifetime;

	private final SecretKey signingKey;

	private final Clock clock;

	@Autowired
	JwtTokenServiceAdapter(@Value("${security.jwt.secret}") String secret,
			@Value("${security.jwt.lifetime}") Duration tokenLifetime, Clock clock) {
		this.tokenLifetime = tokenLifetime;
		this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.clock = clock;
	}

	@Override
	public String generateToken(String username) {
		Instant now = clock.instant();
		Instant expiration = now.plus(tokenLifetime);

		String token = Jwts.builder()
			.subject(username)
			.issuedAt(Date.from(now))
			.expiration(Date.from(expiration))
			.signWith(signingKey)
			.compact();

		log.debug("Generated JWT token for user: {}, expires at: {}", username, expiration);
		return token;
	}

	@Override
	public TokenData parseToken(String token) throws IllegalArgumentException {
		try {
			Claims claims = parseClaims(token);
			return new TokenData(claims.getSubject(), claims.getIssuedAt().toInstant(),
					claims.getExpiration().toInstant());
		}
		catch (JwtException | IllegalArgumentException e) {
			log.error("Failed to parse token: {}", e.getMessage());
			throw new IllegalArgumentException("Invalid token", e);
		}
	}

	@Override
	public TokenData validateToken(String token) throws IllegalArgumentException {
		try {
			TokenData tokenData = parseToken(token);
			boolean expired = tokenData.isExpired(clock.instant());

			if (expired) {
				log.debug("Token validation failed: token expired");
				throw new IllegalArgumentException("Token validation failed: token expired");
			}

			log.trace("Token validation successful");
			return tokenData;
		}
		catch (IllegalArgumentException e) {
			log.debug("Token validation failed: {}", e.getMessage());
			throw e;
		}
	}

	private Claims parseClaims(String token) throws JwtException, IllegalArgumentException {
		return Jwts.parser()
			.verifyWith(signingKey)
			.clock(() -> Date.from(clock.instant()))
			.build()
			.parseSignedClaims(token)
			.getPayload();
	}

}
