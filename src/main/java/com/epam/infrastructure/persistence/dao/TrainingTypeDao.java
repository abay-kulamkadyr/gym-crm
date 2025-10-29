package com.epam.infrastructure.persistence.dao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class TrainingTypeDao {

	@EqualsAndHashCode.Include
	private long trainingTypeId;

	private String trainingNameType;

	private long trainerId;

	private long trainingId;

}
