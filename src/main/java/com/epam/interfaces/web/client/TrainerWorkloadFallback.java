package com.epam.interfaces.web.client;

import com.epam.interfaces.web.client.request.TrainerWorkloadWebRequest;
import com.epam.interfaces.web.client.response.TrainerSummaryResponse;
import com.epam.interfaces.web.client.response.TrainerWorkloadResponse;
import org.apache.commons.lang3.concurrent.CircuitBreakingException;
import org.springframework.stereotype.Component;

@Component
public class TrainerWorkloadFallback implements TrainerWorkloadInterface {

    @Override
    public TrainerSummaryResponse getTrainerSummary(String username) {
        throw new CircuitBreakingException();
    }

    @Override
    public TrainerWorkloadResponse processTrainerRequest(TrainerWorkloadWebRequest request) {
        throw new CircuitBreakingException();
    }
}
