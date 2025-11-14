package com.epam.application.service;

import com.epam.application.Credentials;

public interface AuthenticationService {

	Boolean authenticateTrainee(Credentials credentials);

	Boolean authenticateTrainer(Credentials credentials);

}
