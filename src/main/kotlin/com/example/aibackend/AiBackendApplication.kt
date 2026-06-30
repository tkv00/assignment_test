package com.example.aibackend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class AiBackendApplication

/**
 * [애플리케이션 실행]
 * Spring Boot 애플리케이션 컨텍스트 시작
 *
 * @param args 애플리케이션 실행 인자 배열
 */
fun main(args: Array<String>) {
    runApplication<AiBackendApplication>(*args)
}
