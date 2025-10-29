package com.epam.infrastructure.persistence.dao;

import java.time.LocalDate;
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
public class TraineeDao {

	@EqualsAndHashCode.Include
	private long userId;

	private String firstName;

	private String lastName;

	private String username;

	private String password;

	private boolean active;

	private LocalDate dob;

	private String address;

	private long trainingId;

}
