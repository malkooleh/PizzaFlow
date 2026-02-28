package com.pizzaflow.order.config;

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
                        .title("Order Service API")
                        .description("""
                                Manages the full order lifecycle for PizzaFlow.

                                **V1 API** — Standard order CRUD operations.

                                **V2 API (CQRS)** — Event-sourced order commands and queries.
                                Commands: `/api/v2/orders/commands/**`
                                Queries: `/api/v2/orders/queries/**`
                                """)
                        .version("2.0.0")
                        .contact(new Contact()
                                .name("PizzaFlow Team")
                                .email("dev@pizzaflow.com"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8081").description("Local"),
                        new Server().url("http://api-gateway:8080/orders").description("Via Gateway")));
    }
}
