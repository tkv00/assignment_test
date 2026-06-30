package com.example.aibackend.api.controller

import com.example.aibackend.support.IntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@AutoConfigureMockMvc
class HealthControllerTest
    @Autowired
    constructor(
        private val mockMvc: MockMvc,
    ) : IntegrationTest() {
        /**
         * [헬스 체크 통합 테스트]
         * 애플리케이션 상태와 데이터베이스 상태 응답 검증
         */
        @Test
        fun `returns application and database health`() {
            mockMvc
                .get("/api/health")
                .andExpect {
                    status { isOk() }
                    jsonPath("$.status") { value("ok") }
                    jsonPath("$.database") { value("up") }
                }
        }
    }
