package com.pravinmali.orderprocessing.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI / Swagger UI configuration.
 *
 * <p>Swagger UI available at {@code http://localhost:8080/swagger-ui.html}.
 * Raw OpenAPI JSON at {@code http://localhost:8080/api-docs}.</p>
 *
 * <p>Registers the {@code bearerAuth} security scheme so the Swagger UI
 * shows an "Authorize" button — paste your JWT token to test secured endpoints.</p>
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
                                notifications, PDF invoice generation, and JWT-based RBAC.
                                
                                **Authentication:** Register at `/api/v1/auth/register`, then
                                login at `/api/v1/auth/login` to receive a Bearer token.
                                Click **Authorize** above and paste the token.
                                
                                **Roles:**
                                - `ADMIN` — full access to all orders, status updates, export
                                - `CUSTOMER` — create orders, view own orders and invoices
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
                ))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Paste your JWT token from /auth/login")));
    }
}
