package com.epam.domain.port;

import java.util.Optional;

import com.epam.domain.model.User;

public interface UserRepository {

    Optional<User> findByUsername(String username);

}
