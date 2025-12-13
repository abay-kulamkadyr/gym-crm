package com.epam.infrastructure.monitoring.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnClass(io.micrometer.core.instrument.MeterRegistry.class)
class MicrometerAppMetrics implements AppMetrics {

    private final Counter traineeRegistered;

    private final Counter trainerRegistered;

    private final Counter userLoginAttempts;

    private final Counter userLoginFailed;

    @Autowired
    MicrometerAppMetrics(MeterRegistry registry) {
        this.traineeRegistered = registry.counter("trainee_registered_total");
        this.trainerRegistered = registry.counter("trainer_registered_total");
        this.userLoginAttempts = registry.counter("user_login_attempts_total");
        this.userLoginFailed = registry.counter("user_login_failed_total");
    }

    public void incrementTraineeRegistered() {
        traineeRegistered.increment();
    }

    public void incrementTrainerRegistered() {
        trainerRegistered.increment();
    }

    public void incrementUserLoginAttempts() {
        userLoginAttempts.increment();
    }

    public void incrementUserLoginFailed() {
        userLoginFailed.increment();
    }

}
