package com.epam.domain.repository;

import com.epam.domain.model.Trainer;
import java.util.Optional;

public interface TrainerRepository extends CrudRepository<Trainer> {

	Optional<Trainer> findByUsername(String username);

	/**
	 * Finds the latest (highest serial) username matching the given prefix. E.g., if
	 * prefix is "John.Doe" and the usernames are "John.Doe" and "John.Doe3", this should
	 * return "John.Doe3".
	 */
	Optional<String> findLatestUsername(String prefix);

}
