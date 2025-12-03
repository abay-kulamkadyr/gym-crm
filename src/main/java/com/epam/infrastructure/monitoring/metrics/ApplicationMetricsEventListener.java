package com.epam.infrastructure.monitoring.metrics;

import com.epam.application.event.TraineeRegisteredEvent;
import com.epam.application.event.TrainerRegisteredEvent;
import com.epam.application.event.UserLoginAttemptEvent;
import com.epam.application.event.UserLoginFailedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
class ApplicationMetricsEventListener {

	private final AppMetrics metrics;

	@Autowired
	ApplicationMetricsEventListener(AppMetrics metrics) {
		this.metrics = metrics;
	}

	@EventListener
	public void onTraineeRegistered(TraineeRegisteredEvent event) {
		metrics.incrementTraineeRegistered();
	}

	@EventListener
	public void onTrainerRegistered(TrainerRegisteredEvent event) {
		metrics.incrementTrainerRegistered();
	}

	@EventListener
	public void onUserLoginAttempt(UserLoginAttemptEvent event) {
		metrics.incrementUserLoginAttempts();
	}

	@EventListener
	public void onUserLoginFailed(UserLoginFailedEvent event) {
		metrics.incrementUserLoginFailed();
	}

}
