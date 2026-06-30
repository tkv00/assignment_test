package com.example.aibackend.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
    /**
     * [OpenAPI 문서 설정]
     * Swagger UI와 OpenAPI JSON에 노출할 문서 메타데이터 생성
     *
     * @return OpenAPI 문서 메타데이터 설정
     */
    @Bean
    fun openAPI(): OpenAPI {
        {
            val securityScheme =
                SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")

            return OpenAPI()
                .info(
                    Info()
                        .title("AI Backend API")
                        .description("Kotlin Spring Boot backend API documentation")
                        .version("v1")
                        .license(License().name("MIT")),
                ).components(
                    Components()
                        .addSecuritySchemes(
                            BEARER_AUTH_SCHEME,
                            securityScheme,
                        ),
                ).addSecurityItem(SecurityRequirement().addList(BEARER_AUTH_SCHEME))
        }
    }

    companion object {
        private const val BEARER_AUTH_SCHEME = "bearerAuth"
    }
}
