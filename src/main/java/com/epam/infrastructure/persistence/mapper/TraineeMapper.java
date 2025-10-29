package com.epam.infrastructure.persistence.mapper;

import com.epam.domain.model.Trainee;
import com.epam.infrastructure.persistence.dao.TraineeDao;

public class TraineeMapper {

	private TraineeMapper() {

	}

	public static TraineeDao toEntity(Trainee trainee) {
		if (trainee == null) {
			return null;
		}

		return TraineeDao.builder()
			.userId(trainee.getUserId())
			.firstName(trainee.getFirstName())
			.lastName(trainee.getLastName())
			.username(trainee.getUsername())
			.password(trainee.getPassword())
			.active(trainee.isActive())
			.dob(trainee.getDob())
			.address(trainee.getAddress())
			.trainingId(trainee.getTrainingId())
			.build();
	}

	public static Trainee toDomain(TraineeDao entity) {
		if (entity == null) {
			return null;
		}

		Trainee trainee = new Trainee(entity.getUserId(), entity.getFirstName(), entity.getLastName(), entity.getDob());

		trainee.setAddress(entity.getAddress());
		trainee.setTrainingId(entity.getTrainingId());
		trainee.setUsername(entity.getUsername());
		trainee.setPassword(entity.getPassword());
		trainee.setActive(entity.isActive());

		return trainee;
	}

}
