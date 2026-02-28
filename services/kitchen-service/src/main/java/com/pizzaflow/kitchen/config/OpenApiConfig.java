package com.pizzaflow.kitchen.config;

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
                        .title("Kitchen Service API")
                        .description("""
                                Kitchen queue management and order preparation for PizzaFlow.

                                Manages the kitchen display system (KDS). Orders flow in via Kafka
                                from the payment service and are tracked through preparation stages.

                                **Real-time Updates:** WebSocket at `/ws` → topic `/topic/kitchen/{restaurantId}/orders`
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
                        new Server().url("http://api-gateway:8080/kitchen").description("Via Gateway")));
    }
}
