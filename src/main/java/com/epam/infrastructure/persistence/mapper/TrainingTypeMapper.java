package com.epam.infrastructure.persistence.mapper;

import com.epam.domain.model.TrainingType;
import com.epam.infrastructure.persistence.dao.TrainingTypeDAO;
import com.epam.infrastructure.persistence.exception.MappingException;
import org.springframework.lang.NonNull;

public final class TrainingTypeMapper {

	private TrainingTypeMapper() {
		throw new UnsupportedOperationException("Utility class");
	}

	public static TrainingTypeDAO toEntity(@NonNull TrainingType trainingType) {
		if (trainingType.getTrainingTypeName() == null || trainingType.getTrainingTypeName().isBlank()) {
			throw new MappingException("TrainingType name cannot be null or blank");
		}

		TrainingTypeDAO trainingTypeDAO = new TrainingTypeDAO();
		trainingTypeDAO.setTrainingTypeId(trainingType.getTrainingTypeId());
		trainingTypeDAO.setTrainingTypeName(trainingType.getTrainingTypeName());

		return trainingTypeDAO;
	}

	public static TrainingType toDomain(@NonNull TrainingTypeDAO trainingTypeDAO) {
		String typeName = trainingTypeDAO.getTrainingTypeName();
		if (typeName == null || typeName.isBlank()) {
			throw new MappingException(
					"Cannot map TrainingTypeDAO to TrainingType: training type name is null or blank");
		}

		TrainingType trainingType = new TrainingType(typeName);
		trainingType.setTrainingTypeId(trainingTypeDAO.getTrainingTypeId());

		return trainingType;
	}

}