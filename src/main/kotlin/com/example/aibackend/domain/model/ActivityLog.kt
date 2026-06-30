package com.example.aibackend.domain.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "activity_logs")
class ActivityLog(
    activityType: String,
    user: UserAccount? = null,
    id: UUID? = null,
    createdAt: Instant = Instant.now(),
) {
    // 활동 유형
    @Column(name = "activity_type", nullable = false, length = 30)
    var activityType: String = activityType

    // 활동을 수행한 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: UserAccount? = user

    // 활동 로그 기본 키
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = id

    // 활동 발생 일시
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = createdAt
}
