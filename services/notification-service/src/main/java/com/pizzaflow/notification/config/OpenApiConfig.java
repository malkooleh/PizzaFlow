package com.pizzaflow.notification.config;

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
                        .title("Notification Service API")
                        .description("""
                                Multi-channel notification management for PizzaFlow.

                                Sends notifications via Email, SMS, Push, and In-App channels.
                                Listens to Kafka events from order, payment, booking, and delivery services.

                                **Channels Supported:** Email (SMTP), SMS (Twilio), Push (Firebase), In-App (WebSocket)
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("PizzaFlow Team")
                                .email("dev@pizzaflow.com"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8088").description("Local"),
                        new Server().url("http://api-gateway:8080/notifications").description("Via Gateway")));
    }
}
