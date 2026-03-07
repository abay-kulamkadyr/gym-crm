package com.epam.infrastructure.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaProducerConfig {

    @Value("${app.kafka.topics.training-created}")
    private String trainingTopic;

    @Value("${app.kafka.topics.training-created-partitions:3}")
    private int partitions;

    @Bean
    public NewTopic trainerWorkloadTopic() {
        return TopicBuilder.name(trainingTopic)
                .partitions(partitions)
                .replicas(1)
                .build();
    }
}
