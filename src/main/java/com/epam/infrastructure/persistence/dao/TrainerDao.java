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
public class TrainerDao {

	@EqualsAndHashCode.Include
	private long userId;

	private String firstName;

	private String lastName;

	private String username;

	private String password;

	private boolean active;

	private String specialization;

	private long trainingId;

	private long trainingTypeId;

}
