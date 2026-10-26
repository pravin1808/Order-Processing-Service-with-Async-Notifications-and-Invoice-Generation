package com.pravinmali.orderprocessing.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI / Swagger UI configuration.
 *
 * <p>Swagger UI will be available at {@code http://localhost:8080/swagger-ui.html}.
 * Raw OpenAPI JSON is at {@code http://localhost:8080/api-docs}.</p>
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI orderProcessingOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Order Processing Service API")
                        .description("""
                                REST API for managing customer orders with async domain event
                                notifications and PDF invoice generation.
                                
                                **Features:**
                                - Create and manage orders with multiple line items
                                - Automatic subtotal / tax (18% GST) / total calculation
                                - Async domain events (OrderCreated, OrderConfirmed, OrderShipped)
                                - PDF invoice download for confirmed/shipped/delivered orders
                                - Paginated order listing
                                """)
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Pravin Mali")
                                .email("pravinmali@example.com"))
                        .license(new License()
                                .name("Assignment Project — Ikaansh Edutech Private Limited")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local development server")
                ));
    }
}
