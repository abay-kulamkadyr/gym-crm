package com.epam.infrastructure.monitoring.metrics;

public interface AppMetrics {

    void incrementTraineeRegistered();

    void incrementTrainerRegistered();

    void incrementUserLoginAttempts();

    void incrementUserLoginFailed();
}
