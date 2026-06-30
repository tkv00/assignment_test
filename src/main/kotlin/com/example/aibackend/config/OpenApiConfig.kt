package com.example.aibackend.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
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
    fun openAPI(): OpenAPI =
        OpenAPI()
            .info(
                Info()
                    .title("AI Backend API")
                    .description("Kotlin Spring Boot backend API documentation")
                    .version("v1")
                    .license(License().name("MIT")),
            )
}
