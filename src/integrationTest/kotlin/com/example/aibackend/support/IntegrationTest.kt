package com.example.aibackend.support

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer

@SpringBootTest
@ActiveProfiles("test")
abstract class IntegrationTest {
    companion object {
        private val postgres =
            PostgreSQLContainer<Nothing>("postgres:15.8").apply {
                withDatabaseName("ai_backend_test")
                withUsername("test")
                withPassword("test")
                start()
            }

        /**
         * [테스트 데이터소스 등록]
         * PostgreSQL Testcontainers 연결 정보를 Spring 테스트 프로퍼티로 등록
         *
         * @param registry 동적 테스트 프로퍼티 레지스트리
         */
        @JvmStatic
        @DynamicPropertySource
        fun registerDataSource(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }
    }
}
