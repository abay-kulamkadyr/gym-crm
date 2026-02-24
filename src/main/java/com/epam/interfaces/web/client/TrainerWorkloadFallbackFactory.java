package com.epam.interfaces.web.client;

import java.util.Collections;

import com.epam.interfaces.web.client.response.TrainerSummaryResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TrainerWorkloadFallbackFactory implements FallbackFactory<TrainerWorkloadClient> {

    @Override
    public TrainerWorkloadClient create(Throwable cause) {
        return new TrainerWorkloadClient() {
            @Override
            public TrainerSummaryResponse getTrainerSummary(String username) {
                log.warn("Fallback: Unable to get trainer summary for {}, reason: {}", username, cause.getMessage());

                // Empty response
                return new TrainerSummaryResponse(
                        username, "Unavailable", "Unavailable", false, Collections.emptyList());
            }
        };
    }
}
