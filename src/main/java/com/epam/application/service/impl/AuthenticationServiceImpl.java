package com.epam.application.service.impl;

import com.epam.application.Credentials;
import com.epam.application.event.UserLoginAttemptEvent;
import com.epam.application.event.UserLoginFailedEvent;
import com.epam.application.exception.AuthenticationException;
import com.epam.application.service.AuthenticationService;
import com.epam.domain.model.User;
import com.epam.domain.port.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class AuthenticationServiceImpl implements AuthenticationService {

	private final UserRepository userRepository;

	private ApplicationEventPublisher applicationEventPublisher;

	@Autowired
	public AuthenticationServiceImpl(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Autowired
	void setApplicationEventPublisher(ApplicationEventPublisher publisher) {
		this.applicationEventPublisher = publisher;
	}

	@Override
	public User authenticate(Credentials credentials) {
		applicationEventPublisher.publishEvent(new UserLoginAttemptEvent(credentials.username()));

		Optional<User> userOpt = userRepository.findByUsername(credentials.username());

		if (userOpt.isEmpty()) {
			applicationEventPublisher.publishEvent(new UserLoginFailedEvent(credentials.username()));
			throw new EntityNotFoundException(
					String.format("User with username '%s' not found", credentials.username()));
		}

		// Validate Password
		User user = userOpt.get();
		if (user.getPassword().equals(credentials.password())) {
			return user;
		}

		applicationEventPublisher.publishEvent(new UserLoginFailedEvent(credentials.username()));
		throw new AuthenticationException(
				String.format("Invalid credentials for username: %s", credentials.username()));
	}

}