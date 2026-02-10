package com.epam.domain.model;

import java.time.LocalDate;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Trainee extends User {

    @EqualsAndHashCode.Include
    private Long traineeId;

    private LocalDate dob;

    private String address;

    public Trainee(String firstName, String lastName, Boolean active) {
        super(firstName, lastName, active);
    }
}
