package com.epam.application.service.impl;

import com.epam.application.service.TrainerService;
import com.epam.domain.model.Trainer;
import com.epam.domain.repository.TrainerRepository;
import com.epam.application.util.CredentialsGenerator;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TrainerServiceImpl implements TrainerService {

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

		if (trainer.getUserId() != null) {
			throw new IllegalArgumentException("New trainer must not have an ID — it will be generated automatically");
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

		if (trainer.getUserId() == null) {
			throw new IllegalArgumentException("Cannot update trainer without an ID");
		}

		if (trainerRepository.findById(trainer.getUserId()).isEmpty()) {
			throw new IllegalArgumentException("Trainer with id " + trainer.getUserId() + " does not exist");
		}

		trainerRepository.save(trainer);
		log.info("Updated trainer: {}", trainer.getUserId());
	}

	@Override
	public void delete(Long id) {
		trainerRepository.delete(id);
		log.info("Deleted trainer: {}", id);
	}

	@Override
	public Optional<Trainer> getById(Long id) {
		return trainerRepository.findById(id);
	}

}
