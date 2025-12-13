package com.epam.infrastructure.security.provider;

import java.util.Collections;

import com.epam.infrastructure.security.authentication_token.JwtAuthenticationToken;
import com.epam.infrastructure.security.core.TokenData;
import com.epam.infrastructure.security.port.out.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtAuthenticationProvider implements AuthenticationProvider {

    private final TokenService tokenService;

    @Autowired
    JwtAuthenticationProvider(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String token = (String) authentication.getPrincipal();
        log.debug("JwtAuthenticationProvider: Validating token");
        try {
            TokenData tokenData = tokenService.validateToken(token);
            log.debug("Token successfully validated for user: {}", tokenData.username());
            return new UsernamePasswordAuthenticationToken(tokenData.username(), null, Collections.emptyList());
        }
        catch (IllegalArgumentException e) {
            log.warn("Token parsing failed: {}", e.getMessage());
            throw new BadCredentialsException("Invalid token", e);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return JwtAuthenticationToken.class.isAssignableFrom(authentication);
    }

}
