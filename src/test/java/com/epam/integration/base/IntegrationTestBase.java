package com.epam.integration.base;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.main.banner-mode=off")
public abstract class IntegrationTestBase {

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", SharedContainers.postgres::getJdbcUrl);
        registry.add("spring.datasource.username", SharedContainers.postgres::getUsername);
        registry.add("spring.datasource.password", SharedContainers.postgres::getPassword);
        registry.add("spring.data.mongodb.uri", SharedContainers.mongo::getReplicaSetUrl);
        registry.add("spring.kafka.bootstrap-servers", SharedContainers.kafka::getBootstrapServers);
        registry.add(
                "spring.cloud.openfeign.client.config.trainer-workload-service.url",
                () -> "http://localhost:" + SharedContainers.workloadService.getMappedPort(8081));
    }
}
