package com.redsport.backend.shared.infrastructure.documentation.openapi.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI docs. Once running, visit:
 *   http://localhost:8080/swagger-ui.html
 * to see and TEST every endpoint from a web UI.
 */
@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI redsportOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("RedSport Platform API")
                        .description("REST API for the RedSport sportswear platform")
                        .version("v1.0.0")
                        .license(new License().name("Apache 2.0")));
    }
}