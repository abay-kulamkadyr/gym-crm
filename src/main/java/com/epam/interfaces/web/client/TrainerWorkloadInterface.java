package com.epam.interfaces.web.client;

import com.epam.interfaces.web.client.request.TrainerWorkloadWebRequest;
import com.epam.interfaces.web.client.response.TrainerSummaryResponse;
import com.epam.interfaces.web.client.response.TrainerWorkloadResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "trainer-workload-service", fallback = TrainerWorkloadFallback.class)
public interface TrainerWorkloadInterface {
    @GetMapping("/api/workload/{username}")
    TrainerSummaryResponse getTrainerSummary(@PathVariable("username") String username);

    @PostMapping("/api/workload")
    TrainerWorkloadResponse processTrainerRequest(@RequestBody TrainerWorkloadWebRequest request);
}
