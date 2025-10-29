package com.epam.infrastructure.persistence.mapper;

import com.epam.domain.model.Training;
import com.epam.infrastructure.persistence.dao.TrainingDao;

public class TrainingMapper {

	private TrainingMapper() {

	}

	public static TrainingDao toEntity(Training training) {
		if (training == null) {
			return null;
		}

		return TrainingDao.builder()
			.trainingId(training.getTrainingId())
			.traineeId(training.getTraineeId())
			.trainerId(training.getTrainerId())
			.trainingTypeId(training.getTrainingTypeId())
			.trainingDate(training.getTrainingDate())
			.trainingDuration(training.getTrainingDuration())
			.build();
	}

	public static Training toDomain(TrainingDao trainingDao) {
		if (trainingDao == null) {
			return null;
		}
		return new Training(trainingDao.getTrainingId(), trainingDao.getTrainerId(), trainingDao.getTraineeId(),
				trainingDao.getTrainingDate(), trainingDao.getTrainingDuration());
	}

}
