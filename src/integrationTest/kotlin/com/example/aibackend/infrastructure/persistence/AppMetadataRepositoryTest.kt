package com.example.aibackend.infrastructure.persistence

import com.example.aibackend.support.IntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class AppMetadataRepositoryTest
    @Autowired
    constructor(
        private val repository: AppMetadataRepository,
    ) : IntegrationTest() {
        /**
         * [Flyway 메타데이터 통합 테스트]
         * Flyway 마이그레이션으로 저장된 스키마 버전 메타데이터 조회 검증
         */
        @Test
        fun `loads metadata inserted by flyway migration`() {
            val metadata = repository.findByKey("schema_version")

            assertThat(metadata).isNotNull
            assertThat(metadata?.value).isEqualTo("1")
        }
    }
