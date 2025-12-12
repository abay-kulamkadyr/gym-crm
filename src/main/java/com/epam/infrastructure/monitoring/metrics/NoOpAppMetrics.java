package com.epam.infrastructure.monitoring.metrics;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnMissingBean(AppMetrics.class)
class NoOpAppMetrics implements AppMetrics {

	@Override
	public void incrementTraineeRegistered() {

	}

	@Override
	public void incrementTrainerRegistered() {

	}

	@Override
	public void incrementUserLoginAttempts() {

	}

	@Override
	public void incrementUserLoginFailed() {

	}

}
