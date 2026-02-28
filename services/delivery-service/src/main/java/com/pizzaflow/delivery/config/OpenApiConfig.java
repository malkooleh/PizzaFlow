package com.pizzaflow.delivery.config;

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
                        .title("Delivery Service API")
                        .description("""
                                Delivery tracking and courier management for PizzaFlow.

                                Manages delivery assignments, real-time location tracking,
                                and courier availability. Integrates with Kafka for kitchen-ready events.

                                **Location Caching:** Redis stores courier positions for real-time lookup.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("PizzaFlow Team")
                                .email("dev@pizzaflow.com"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8087").description("Local"),
                        new Server().url("http://api-gateway:8080/delivery").description("Via Gateway")));
    }
}
