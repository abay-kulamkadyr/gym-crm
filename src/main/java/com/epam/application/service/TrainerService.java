package com.epam.application.service;

import com.epam.domain.model.Trainer;
import com.epam.domain.repository.TrainerRepository;
import com.epam.application.util.CredentialsGenerator;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

		if (trainerRepository.findById(trainer.getUserId()).isPresent()) {
			throw new IllegalArgumentException("Trainer with the given id=" + trainer.getUserId() + " already exists");
		}

		trainer.setUsername(CredentialsGenerator.generateUniqueUsername(trainer.getFirstName(), trainer.getLastName(),
				trainerRepository::findLatestUsername));
		trainer.setPassword(CredentialsGenerator.generateRandomPassword(10));
		trainerRepository.save(trainer);
		log.info("Created trainer: {}", trainer.getUsername());
	}

	@Override
	public void update(Trainer trainer) {
		if (trainer == null) {
			throw new IllegalArgumentException("Trainer cannot be null");
		}

		Optional<Trainer> existing = trainerRepository.findById(trainer.getUserId());
		if (existing.isEmpty()) {
			throw new IllegalArgumentException("Trainer with id " + trainer.getUserId() + " does not exist");
		}

		trainerRepository.save(trainer);
		log.info("Updated trainer: {}", trainer.getUserId());
	}

	@Override
	public void delete(long id) {
		Optional<Trainer> existing = trainerRepository.findById(id);
		if (existing.isEmpty()) {
			log.warn("Attempted to delete non-existent trainer: {}", id);
			return;
		}

		trainerRepository.delete(id);
		log.info("Deleted trainer: {}", id);
	}

	@Override
	public Optional<Trainer> getById(long id) {
		return trainerRepository.findById(id);
	}

}
