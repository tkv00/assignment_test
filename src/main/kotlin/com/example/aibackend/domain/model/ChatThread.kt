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
@Table(name = "chat_threads")
class ChatThread(
    user: UserAccount,
    lastChattedAt: Instant = Instant.now(),
    id: UUID? = null,
    createdAt: Instant = Instant.now(),
    deletedAt: Instant? = null,
) {
    // 스레드를 소유한 사용자
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    var user: UserAccount = user

    // 마지막 대화 생성 일시
    @Column(name = "last_chatted_at", nullable = false)
    var lastChattedAt: Instant = lastChattedAt

    // 스레드 기본 키
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = id

    // 스레드 생성 일시
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = createdAt

    // 스레드 삭제 일시
    @Column(name = "deleted_at")
    var deletedAt: Instant? = deletedAt
}
