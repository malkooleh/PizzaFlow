package com.pizzaflow.tests.infrastructure;

import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Shared Testcontainers infrastructure for PizzaFlow integration tests.
 *
 * <p>Uses static fields to start containers once across all test classes
 * in the same JVM run (Singleton pattern via Ryuk).
 *
 * <p>Services under test are started as external processes via their
 * fat JAR with Spring profiles pointing to these container addresses.
 * For simpler per-service tests, use {@link PerServiceContainers} instead.
 */
public final class SharedContainers {

    /** PostgreSQL — used by: order, payment, kitchen, inventory, booking, delivery, notification */
    public static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
                    .withDatabaseName("pizzaflow_test")
                    .withUsername("postgres")
                    .withPassword("postgres");

    /** Kafka — event bus for all services */
    public static final KafkaContainer KAFKA =
            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));

    /** MongoDB — used by catalog-service */
    public static final MongoDBContainer MONGO =
            new MongoDBContainer(DockerImageName.parse("mongo:7.0"));

    /** Redis — used by delivery-service (courier location cache) and catalog-service (menu cache) */
    @SuppressWarnings("resource")
    public static final GenericContainer<?> REDIS =
            new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
                    .withExposedPorts(6379);

    static {
        POSTGRES.start();
        KAFKA.start();
        MONGO.start();
        REDIS.start();
    }

    private SharedContainers() {}

    public static String postgresJdbcUrl() {
        return POSTGRES.getJdbcUrl();
    }

    public static String kafkaBootstrapServers() {
        return KAFKA.getBootstrapServers();
    }

    public static String mongoUri() {
        return MONGO.getConnectionString();
    }

    public static String redisHost() {
        return REDIS.getHost();
    }

    public static int redisPort() {
        return REDIS.getMappedPort(6379);
    }
}
