package com.epam.application.request.types;

import com.epam.application.Credentials;

public interface AuthenticatedRequest {

	Credentials credentials();

}
