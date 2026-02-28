package com.pizzaflow.booking.config;

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
                        .title("Booking Service API")
                        .description("""
                                Table reservation and capacity management for PizzaFlow.

                                Enables customers to reserve tables at partner restaurants.
                                Supports hybrid orders (pre-ordering food with a booking).
                                Uses pessimistic locking to prevent double-booking.

                                **Automated Reminders:** Sent 1 hour before booking time via Notification Service.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("PizzaFlow Team")
                                .email("dev@pizzaflow.com"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8086").description("Local"),
                        new Server().url("http://api-gateway:8080/bookings").description("Via Gateway")));
    }
}
