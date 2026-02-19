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

    @Bean
    public NewTopic trainerWorkloadTopic() {
        return TopicBuilder.name(trainingTopic).partitions(3).replicas(1).build();
    }
}
