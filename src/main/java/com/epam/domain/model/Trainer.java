package com.epam.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Trainer extends User {

    @EqualsAndHashCode.Include
    private Long trainerId;

    private TrainingType specialization;

    public Trainer(String firstName, String lastName, Boolean active, TrainingType specialization) {
        super(firstName, lastName, active);
        this.specialization = specialization;
    }
}
