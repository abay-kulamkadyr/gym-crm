package com.epam.integration.base;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.utility.DockerImageName;

public final class SharedContainers {

    public static final Network network = Network.newNetwork();

    public static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>(DockerImageName.parse(TestImages.POSTGRESQL)).withNetwork(network);

    public static final ConfluentKafkaContainer kafka = new ConfluentKafkaContainer(
                    DockerImageName.parse(TestImages.KAFKA))
            .withNetwork(network)
            .withNetworkAliases("kafka")
            .withListener("kafka:19092");

    public static final MongoDBContainer mongo = new MongoDBContainer(DockerImageName.parse(TestImages.MONGODB))
            .withNetwork(network)
            .withNetworkAliases("mongodb");

    public static final GenericContainer<?> workloadService = new GenericContainer<>(
                    DockerImageName.parse(TestImages.TRAINER_WORKLOAD))
            .withNetwork(network)
            .withExposedPorts(8081)
            .dependsOn(kafka, mongo)
            .withEnv("SPRING_KAFKA_BOOTSTRAP_SERVERS", "kafka:19092")
            .withEnv("SPRING_DATA_MONGODB_URI", "mongodb://mongodb:27017/workload")
            .waitingFor(Wait.forHttp("/actuator/health").forPort(8081));

    static {
        postgres.start();
        kafka.start();
        mongo.start();
        workloadService.start();
    }

    private SharedContainers() {}
}
