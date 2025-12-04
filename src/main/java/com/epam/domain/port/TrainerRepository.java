package com.epam.domain.port;

import com.epam.domain.model.Trainee;
import com.epam.domain.model.Trainer;

import java.util.List;
import java.util.Optional;

public interface TrainerRepository extends CrudRepository<Trainer> {

	Optional<Trainer> findByUsername(String trainerUsername);

	Optional<String> findLatestUsername(String prefix);

	List<Trainee> getTrainees(String trainerUsername);

	void deleteByUsername(String username);

}
