package com.epam.application.service;

import com.epam.domain.repository.TrainerRepository;
import lombok.extern.slf4j.Slf4j;
import com.epam.domain.model.Trainer;
import com.epam.domain.util.PasswordGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@Slf4j
public class TrainerService implements CrudService<Trainer> {

	private TrainerRepository trainerRepository;

	@Autowired
	public void setTrainerRepository(TrainerRepository trainerRepository) {
		this.trainerRepository = trainerRepository;
	}

	@Override
	public void create(Trainer trainer) {
		if (trainer == null) {
			throw new IllegalArgumentException("Trainer cannot be null");
		}

		if (trainerRepository.findById(trainer.getUserId()) != null) {
			throw new IllegalArgumentException("Trainer with the given id=" + trainer.getUserId() + " already exists");
		}

		validateTrainer(trainer);

		String username = generateUniqueUsername(trainer);
		trainer.setUsername(username);
		trainer.setPassword(PasswordGenerator.generate(10));
		trainerRepository.save(trainer);
		log.info("Created trainer: {}", username);
	}

	@Override
	public void update(Trainer trainer) {
		if (trainer == null) {
			throw new IllegalArgumentException("Trainer cannot be null");
		}

		validateTrainer(trainer);

		Trainer existing = trainerRepository.findById(trainer.getUserId());
		if (existing == null) {
			throw new IllegalArgumentException("Trainer with id " + trainer.getUserId() + " does not exist");
		}

		trainerRepository.save(trainer);
		log.info("Updated trainer: {}", trainer.getUserId());
	}

	@Override
	public void delete(long id) {
		Trainer existing = trainerRepository.findById(id);
		if (existing == null) {
			log.warn("Attempted to delete non-existent trainer: {}", id);
			return;
		}

		trainerRepository.delete(id);
		log.info("Deleted trainer: {}", id);
	}

	@Override
	public Trainer getById(long id) {
		return trainerRepository.findById(id);
	}

	@Override
	public Collection<Trainer> getAll() {
		return trainerRepository.findAll();
	}

	// TODO: Replace with efficient query when migrating to Hibernate
	// Current implementation loads all records - acceptable for in-memory storage
	// Future: SELECT COUNT(*) FROM trainee WHERE username LIKE 'base%'
	private String generateUniqueUsername(Trainer trainer) {
		String base = trainer.getFirstName() + "." + trainer.getLastName();
		long duplicates = trainerRepository.findAll()
			.stream()
			.filter(t -> t.getUsername() != null && t.getUsername().startsWith(base))
			.count();
		return duplicates > 0 ? base + (duplicates + 1) : base;
	}

	private void validateTrainer(Trainer trainer) {
		if (trainer.getFirstName() == null || trainer.getFirstName().isBlank()) {
			throw new IllegalArgumentException("Trainer first name cannot be null or empty");
		}
		if (trainer.getLastName() == null || trainer.getLastName().isBlank()) {
			throw new IllegalArgumentException("Trainer last name cannot be null or empty");
		}
		if (trainer.getSpecialization() == null || trainer.getSpecialization().isBlank()) {
			throw new IllegalArgumentException("Trainer specialization cannot be null or empty");
		}
	}

}
