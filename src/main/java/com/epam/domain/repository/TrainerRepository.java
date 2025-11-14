package com.epam.domain.repository;

import com.epam.domain.model.Trainer;
import java.util.Optional;

public interface TrainerRepository extends CrudRepository<Trainer> {

	Optional<Trainer> findByUsername(String username);

	Optional<String> findLatestUsername(String prefix);

	void deleteByUsername(String username);

}
