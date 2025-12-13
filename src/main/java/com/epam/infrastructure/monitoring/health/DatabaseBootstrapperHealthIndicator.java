package com.epam.infrastructure.monitoring.health;

import com.epam.infrastructure.bootstrap.DatabaseBootstrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component("bootstrap")
@Profile("local")
class DatabaseBootstrapperHealthIndicator implements HealthIndicator {

    private final DatabaseBootstrapper bootstrapper;

    @Autowired
    DatabaseBootstrapperHealthIndicator(DatabaseBootstrapper bootstrapper) {
        this.bootstrapper = bootstrapper;
    }

    @Override
    public Health health() {
        if (bootstrapper.getLastError() != null) {
            return Health.down().withDetail("status", "FAILED").withException(bootstrapper.getLastError()).build();
        }

        if (bootstrapper.isInitialized()) {
            Health.Builder builder = Health.up();

            if (bootstrapper.isSkipped()) {
                builder.withDetail("status", "SKIPPED_ALREADY_POPULATED");
            }
            else {
                builder.withDetail("status", "BOOTSTRAP_COMPLETED");
            }

            return builder
                    .withDetail("loadedUsers", bootstrapper.getUserCount())
                    .withDetail("loadedTrainers", bootstrapper.getTrainerCount())
                    .withDetail("loadedTrainees", bootstrapper.getTraineeCount())
                    .withDetail("loadedTrainingTypes", bootstrapper.getTrainingTypeCount())
                    .build();
        }

        return Health.unknown().withDetail("status", "INITIALIZATION_PENDING").build();
    }

}

@Component("bootstrap")
@Profile("!local")
class BootstrapDisabledHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        return Health
                .outOfService()
                .withDetail("status", "BOOTSTRAP_DISABLED")
                .withDetail("reason", "The DatabaseBootstrapper component is not loaded in this profile.")
                .build();
    }

}
