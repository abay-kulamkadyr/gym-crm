package com.epam.infrastructure.security.port.out;

import com.epam.infrastructure.security.core.TokenData;

public interface TokenService {

	String generateToken(String username);

	// if parsable return TokenData, if not throw
	TokenData parseToken(String token) throws IllegalArgumentException;

	// if valid -> TokenData, if not throw
	TokenData validateToken(String token) throws IllegalArgumentException;

}
