package com.epam.infrastructure.persistence.mapper;

import com.epam.domain.model.TrainingType;
import com.epam.infrastructure.persistence.dao.TrainingTypeDao;

public class TrainingTypeMapper {

	private TrainingTypeMapper() {

	}

	public static TrainingTypeDao toEntity(TrainingType trainingType) {
		if (trainingType == null) {
			return null;
		}

		return TrainingTypeDao.builder()
			.trainingTypeId(trainingType.getTrainingTypeId())
			.trainingNameType(trainingType.getTrainingNameType())
			.trainingId(trainingType.getTrainingId())
			.trainerId(trainingType.getTrainerId())
			.build();
	}

	public static TrainingType toDomain(TrainingTypeDao entity) {
		if (entity == null) {
			return null;
		}
		return new TrainingType(entity.getTrainingTypeId(), entity.getTrainingNameType(), entity.getTrainerId(),
				entity.getTrainingId());
	}

}
