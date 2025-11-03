package com.epam.infrastructure.persistence.dao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Builder
public class TrainingTypeDao {

	@EqualsAndHashCode.Include
	private Long trainingTypeId;

	private String trainingNameType;

	private Long trainerId;

	private Long trainingId;

}
