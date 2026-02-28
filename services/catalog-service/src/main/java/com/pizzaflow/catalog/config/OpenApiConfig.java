package com.pizzaflow.catalog.config;

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
                        .title("Catalog Service API")
                        .description("""
                                Menu and product catalog management for PizzaFlow.

                                Provides endpoints for browsing restaurant menus, searching items,
                                and managing menu content. Uses MongoDB for flexible document storage
                                with Redis caching for high-performance reads.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("PizzaFlow Team")
                                .email("dev@pizzaflow.com"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8084").description("Local"),
                        new Server().url("http://api-gateway:8080/catalog").description("Via Gateway")));
    }
}
