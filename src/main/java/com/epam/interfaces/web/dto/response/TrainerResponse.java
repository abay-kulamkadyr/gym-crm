package com.epam.interfaces.web.dto.response;

import java.util.List;
import java.util.Optional;

import com.epam.domain.model.TrainingTypeEnum;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_EMPTY)
public record TrainerResponse(
        Optional<String> username,
        String firstName,
        String lastName,
        TrainingTypeEnum specialization,
        Boolean active,
        List<EmbeddedTraineeResponse> trainees) {}
