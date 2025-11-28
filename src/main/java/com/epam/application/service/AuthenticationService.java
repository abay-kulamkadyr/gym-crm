package com.epam.application.service;

import com.epam.application.Credentials;
import com.epam.domain.model.User;

public interface AuthenticationService {

	User authenticate(Credentials credentials);

}
