package com.epam.application.service.impl;

import com.epam.application.Credentials;
import com.epam.application.exception.AuthenticationException;
import com.epam.application.service.AuthenticationService;
import com.epam.domain.model.User;
import com.epam.domain.port.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class AuthenticationServiceImpl implements AuthenticationService {

	private final UserRepository userRepository;

	@Autowired
	public AuthenticationServiceImpl(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public User authenticate(Credentials credentials) {
		Optional<User> userOpt = userRepository.findByUsername(credentials.username());

		if (userOpt.isEmpty()) {
			throw new EntityNotFoundException(
					String.format("User with username '%s' not found", credentials.username()));
		}

		// Validate Password
		User user = userOpt.get();
		if (user.getPassword().equals(credentials.password())) {
			return user;
		}

		throw new AuthenticationException(
				String.format("Invalid credentials for username: %s", credentials.username()));
	}

}