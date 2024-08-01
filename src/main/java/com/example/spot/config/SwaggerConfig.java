package com.example.spot.config;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.In;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI SpotAPI() {
        Info info = new Info()
                .title("SPOT API")
                .description("SPOT API 명세서");

        String jwtSchemeName = "accessToken";
        String refreshToken = "refreshToken";

        // Define SecurityRequirement with both schemes
        SecurityRequirement securityRequirement = new SecurityRequirement()
            .addList(jwtSchemeName)
            .addList(refreshToken);

        Components components = new Components()
            .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT"))
            .addSecuritySchemes(refreshToken, new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY) // APIKEY 타입 사용
                .in(SecurityScheme.In.HEADER)
                .name("refreshToken")
                .description("Refresh token"));

        return new OpenAPI()
                .addServersItem(new Server().url("/"))
                .info(info)
                .addSecurityItem(securityRequirement)
                .components(components);
    }
}