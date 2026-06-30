package com.example.aibackend.domain.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "app_metadata")
class AppMetadata(
    key: String,
    value: String,
    id: Long? = null,
    createdAt: Instant = Instant.now(),
) {
    // 메타데이터 식별 키
    @Column(name = "metadata_key", nullable = false, unique = true, length = 100)
    var key: String = key

    // 메타데이터 값
    @Column(name = "metadata_value", nullable = false, columnDefinition = "text")
    var value: String = value

    // 메타데이터 기본 키
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = id

    // 메타데이터 생성 일시
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = createdAt
}
