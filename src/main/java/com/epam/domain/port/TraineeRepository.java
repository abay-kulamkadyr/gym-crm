package com.epam.domain.port;

import java.util.List;
import java.util.Optional;

import com.epam.domain.model.Trainee;
import com.epam.domain.model.Trainer;

public interface TraineeRepository extends CrudRepository<Trainee> {

	Optional<Trainee> findByUsername(String username);

	Optional<String> findLatestUsername(String prefix);

	List<Trainer> getTrainers(String traineeUsername);

	List<Trainer> getUnassignedTrainers(String traineeUsername);

	void deleteByUsername(String username);

	void updateTrainersList(String traineeUsername, List<String> trainerUsernames);

}
