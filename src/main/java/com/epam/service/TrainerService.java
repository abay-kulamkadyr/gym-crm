package com.epam.service;

import lombok.extern.slf4j.Slf4j;
import com.epam.dao.TrainerDao;
import com.epam.domain.Trainer;
import com.epam.util.PasswordGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Objects;

@Service
@Slf4j
public class TrainerService implements CrudService<Trainer> {

	private TrainerDao trainerDao;

	@Autowired
	public void setTrainerDao(TrainerDao trainerDao) {
		this.trainerDao = trainerDao;
	}

	@Override
	public void create(Trainer trainer) {
		Objects.requireNonNull(trainer, "Trainer cannot be null");
		if (trainerDao.findById(trainer.getUserId()) != null) {
			throw new IllegalArgumentException("Trainer with the given id=" + trainer.getUserId() + " already exists");
		}

		validateTrainer(trainer);

		String username = generateUniqueUsername(trainer);
		trainer.setUsername(username);
		trainer.setPassword(PasswordGenerator.generate(10));
		trainerDao.save(trainer);
		log.info("Created trainer: {}", username);
	}

	@Override
	public void update(Trainer trainer) {
		Objects.requireNonNull(trainer, "Trainer cannot be null");
		validateTrainer(trainer);

		Trainer existing = trainerDao.findById(trainer.getUserId());
		if (existing == null) {
			throw new IllegalArgumentException("Trainer with id " + trainer.getUserId() + " does not exist");
		}

		trainerDao.save(trainer);
		log.info("Updated trainer: {}", trainer.getUserId());
	}

	@Override
	public void delete(long id) {
		Trainer existing = trainerDao.findById(id);
		if (existing == null) {
			log.warn("Attempted to delete non-existent trainer: {}", id);
			return;
		}

		trainerDao.delete(id);
		log.info("Deleted trainer: {}", id);
	}

	@Override
	public Trainer getById(long id) {
		return trainerDao.findById(id);
	}

	@Override
	public Collection<Trainer> getAll() {
		return trainerDao.findAll();
	}

	private String generateUniqueUsername(Trainer trainer) {
		String base = trainer.getFirstName() + "." + trainer.getLastName();
		long duplicates = trainerDao.findAll().stream().filter(t -> t.getUsername().startsWith(base)).count();
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
