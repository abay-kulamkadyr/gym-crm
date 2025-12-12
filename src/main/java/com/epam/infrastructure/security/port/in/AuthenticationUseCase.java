package com.epam.infrastructure.security.port.in;

import com.epam.infrastructure.security.core.AuthenticationResult;
import org.springframework.security.core.AuthenticationException;

public interface AuthenticationUseCase {

	AuthenticationResult authenticate(String username, String password) throws AuthenticationException;

	void logout(String token);

}
