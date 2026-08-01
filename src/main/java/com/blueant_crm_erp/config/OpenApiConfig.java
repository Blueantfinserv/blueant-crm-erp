package com.blueant_crm_erp.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI blueAntOpenAPI() {

        final String securitySchemeName = "Bearer Authentication";

        return new OpenAPI()

                .info(new Info()

                        .title("BlueAnt CRM ERP API")

                        .description("""
                                Enterprise CRM & Sales Management System

                                Features:
                                • Authentication & Authorization
                                • User & Role Management
                                • Lead Management
                                • Follow-up Management
                                • Meeting Management
                                • Client Onboarding
                                • CRM Operations
                                • Transaction Management
                                • Dashboard & Reports
                                • Notification Services
                                """)

                        .version("v1.0.0")

                        .contact(new Contact()
                                .name("BlueAnt Development Team")
                                .email("support@blueant.in")
                                .url("https://blueant.in"))

                        .license(new License()
                                .name("BlueAnt Proprietary License")))

                .servers(List.of(

                        new Server()
                                .url("http://localhost:8080/api")
                                .description("Local Development"),

                        new Server()
                                .url("https://crm.blueant.in/api")
                                .description("Production Server")
                ))

                .addSecurityItem(
                        new SecurityRequirement()
                                .addList(securitySchemeName))

                .schemaRequirement(
                        securitySchemeName,

                        new SecurityScheme()

                                .name(securitySchemeName)

                                .type(SecurityScheme.Type.HTTP)

                                .scheme("bearer")

                                .bearerFormat("JWT"))

                .externalDocs(
                        new ExternalDocumentation()
                                .description("BlueAnt CRM Documentation")
                                .url("https://blueant.in"));
    }

}