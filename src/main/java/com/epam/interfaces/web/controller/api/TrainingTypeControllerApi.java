package com.epam.interfaces.web.controller.api;

import java.util.List;

import com.epam.interfaces.web.dto.response.TrainingTypeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Training Types", description = "Training type operations")
public interface TrainingTypeControllerApi {

    @Operation(summary = "Get Training Types", description = "Retrieve all available training types")
    ResponseEntity<List<TrainingTypeResponse>> getTrainingTypes();
}
