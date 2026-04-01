package com.pizzaflow.payment.config;

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
                                                .title("Payment Service API")
                                                .description("""
                                                                Payment processing and transaction management for PizzaFlow.

                                                                Handles payment processing, refunds, and payment method management.
                                                                Integrates with Kafka to react to order events and publish payment results.

                                                                **Test Cards:**
                                                                - `****-0000` → Payment declined
                                                                - `****-1111` → Payment approved
                                                                - `****-9999` → Gateway timeout
                                                                """)
                                                .version("1.0.0")
                                                .contact(new Contact()
                                                                .name("PizzaFlow Team")
                                                                .email("dev@pizzaflow.com"))
                                                .license(new License()
                                                                .name("MIT")
                                                                .url("https://opensource.org/licenses/MIT")))
                                .servers(List.of(
                                                new Server().url("http://localhost:8083").description("Local"),
                                                new Server().url("http://api-gateway:8080/payments")
                                                                .description("Via Gateway")));
        }
}
