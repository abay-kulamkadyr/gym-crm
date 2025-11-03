package com.epam.infrastructure.persistence.mapper;

import com.epam.domain.model.Trainer;
import com.epam.infrastructure.persistence.dao.TrainerDao;

public class TrainerMapper {

	private TrainerMapper() {

	}

	public static TrainerDao toEntity(Trainer trainer) {
		if (trainer == null) {
			return null;
		}

		return TrainerDao.builder()
			.userId(trainer.getUserId())
			.firstName(trainer.getFirstName())
			.lastName(trainer.getLastName())
			.username(trainer.getUsername())
			.password(trainer.getPassword())
			.active(trainer.isActive())
			.specialization(trainer.getSpecialization())
			.trainingId(trainer.getTrainingId())
			.trainingTypeId(trainer.getTrainingTypeId())
			.build();
	}

	public static Trainer toDomain(TrainerDao entity) {
		if (entity == null) {
			return null;
		}

		Trainer trainee = new Trainer(entity.getUserId(), entity.getFirstName(), entity.getLastName(),
				entity.getSpecialization());

		trainee.setTrainingId(entity.getTrainingId());
		trainee.setUsername(entity.getUsername());
		trainee.setPassword(entity.getPassword());
		trainee.setActive(entity.isActive());

		return trainee;
	}

}
