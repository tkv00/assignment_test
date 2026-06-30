package com.example.aibackend.api.controller

import com.example.aibackend.support.IntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@AutoConfigureMockMvc
class OpenApiIntegrationTest
    @Autowired
    constructor(
        private val mockMvc: MockMvc,
    ) : IntegrationTest() {
        /**
         * [OpenAPI 문서 통합 테스트]
         * OpenAPI JSON 문서의 기본 메타데이터 응답 검증
         */
        @Test
        fun `serves OpenAPI specification`() {
            mockMvc
                .get("/v3/api-docs")
                .andExpect {
                    status { isOk() }
                    jsonPath("$.openapi") { exists() }
                    jsonPath("$.info.title") { value("AI Backend API") }
                }
        }
    }
