package com.epam.domain.port;

import com.epam.domain.model.User;

import java.util.Optional;

public interface UserRepository {

	Optional<User> findByUsername(String username);

}
