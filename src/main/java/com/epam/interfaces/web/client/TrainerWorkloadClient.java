package com.epam.interfaces.web.client;

import com.epam.interfaces.web.client.response.TrainerSummaryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "trainer-workload-service", fallbackFactory = TrainerWorkloadFallbackFactory.class)
public interface TrainerWorkloadClient {
    @GetMapping("/api/workload/{username}")
    TrainerSummaryResponse getTrainerSummary(@PathVariable("username") String username);
}
