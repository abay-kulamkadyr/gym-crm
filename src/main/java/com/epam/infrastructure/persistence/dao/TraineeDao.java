package com.epam.infrastructure.persistence.dao;

import java.time.LocalDate;
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
public class TraineeDao {

	@EqualsAndHashCode.Include
	private Long userId;

	private String firstName;

	private String lastName;

	private String username;

	private String password;

	private boolean active;

	private LocalDate dob;

	private String address;

	private Long trainingId;

}
