package com.example.aibackend.infrastructure.persistence

import com.example.aibackend.domain.model.AppMetadata
import org.springframework.data.jpa.repository.JpaRepository

interface AppMetadataRepository : JpaRepository<AppMetadata, Long> {
    /**
     * [메타데이터 단건 조회]
     * 메타데이터 키와 일치하는 애플리케이션 메타데이터 조회
     *
     * @param key 조회할 메타데이터 키
     * @return 키와 일치하는 메타데이터 또는 null
     */
    fun findByKey(key: String): AppMetadata?
}
