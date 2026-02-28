package com.pizzaflow.inventory.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Inventory Service API")
                        .description("""
                                Inventory and ingredient management for PizzaFlow.

                                Manages ingredient stock levels, reservations, and consumption.
                                Uses the **Transactional Outbox Pattern** for reliable Kafka event publishing.

                                **Outbox Scheduler:** Publishes events every 5 seconds, cleans up after 7 days.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("PizzaFlow Team")
                                .email("dev@pizzaflow.com"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8085").description("Local"),
                        new Server().url("http://api-gateway:8080/inventory").description("Via Gateway")));
    }
}
