package com.pizzaflow.inventory.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    public static final String INVENTORY_RESERVED_TOPIC = "inventory.reserved";
    public static final String INVENTORY_RESERVATION_FAILED_TOPIC = "inventory.reservation.failed";
    public static final String INVENTORY_CONSUMED_TOPIC = "inventory.consumed";
    public static final String INVENTORY_RELEASED_TOPIC = "inventory.released";
    public static final String LOW_STOCK_ALERT_TOPIC = "inventory.low-stock.alert";

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public NewTopic inventoryReservedTopic() {
        return new NewTopic(INVENTORY_RESERVED_TOPIC, 3, (short) 1);
    }

    @Bean
    public NewTopic inventoryReservationFailedTopic() {
        return new NewTopic(INVENTORY_RESERVATION_FAILED_TOPIC, 3, (short) 1);
    }

    @Bean
    public NewTopic inventoryConsumedTopic() {
        return new NewTopic(INVENTORY_CONSUMED_TOPIC, 3, (short) 1);
    }

    @Bean
    public NewTopic inventoryReleasedTopic() {
        return new NewTopic(INVENTORY_RELEASED_TOPIC, 3, (short) 1);
    }

    @Bean
    public NewTopic lowStockAlertTopic() {
        return new NewTopic(LOW_STOCK_ALERT_TOPIC, 3, (short) 1);
    }
}
