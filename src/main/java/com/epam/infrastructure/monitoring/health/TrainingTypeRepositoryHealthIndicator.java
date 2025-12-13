package com.epam.infrastructure.monitoring.health;

import com.epam.domain.port.TrainingTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("training-types")
class TrainingTypeRepositoryHealthIndicator implements HealthIndicator {

    private final TrainingTypeRepository trainingTypeRepository;

    @Autowired
    TrainingTypeRepositoryHealthIndicator(TrainingTypeRepository repository) {
        this.trainingTypeRepository = repository;
    }

    @Override
    public Health health() {
        final int MIN_TYPES = 5;

        try {
            int trainingTypesCount = trainingTypeRepository.getTrainingTypes().size();

            if (trainingTypesCount < MIN_TYPES) {
                return Health
                        .down()
                        .withDetail("Training types count", trainingTypesCount)
                        .withDetail("Required minimum", MIN_TYPES)
                        .build();
            }

            return Health
                    .up()
                    .withDetail("Training types count", trainingTypesCount)
                    .withDetail("Required minimum", MIN_TYPES)
                    .build();

        }
        catch (Exception e) {
            return Health
                    .down(e)
                    .withDetail("Reason", "Failed to access TrainingTypeRepository")
                    .withDetail("Required minimum", MIN_TYPES)
                    .build();
        }
    }

}
