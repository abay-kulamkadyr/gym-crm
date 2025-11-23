package com.epam.domain.model;

import jakarta.persistence.EntityNotFoundException;

public enum TrainingTypeEnum {

	CARDIO, STRENGTH, YOGA, CROSSFIT, PILATES, BOXING;

	public static TrainingTypeEnum fromString(String value) {
		if (value == null) {
			throw new IllegalArgumentException("Training type cannot be null");
		}

		String normalized = value.trim().toUpperCase();

		try {
			return TrainingTypeEnum.valueOf(normalized);
		}
		catch (IllegalArgumentException ex) {
			throw new EntityNotFoundException("Unknown training type: " + value);
		}
	}

}
