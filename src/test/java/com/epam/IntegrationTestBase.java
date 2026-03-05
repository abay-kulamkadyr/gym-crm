package com.epam;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@EmbeddedKafka(
        partitions = 1,
        topics = {"test-topic"},
        brokerProperties = {"log.dir=target/embedded-kafka", "auto.create.topics.enable=true"})
@TestPropertySource(properties = "spring.main.banner-mode=off")
public class IntegrationTestBase {}
