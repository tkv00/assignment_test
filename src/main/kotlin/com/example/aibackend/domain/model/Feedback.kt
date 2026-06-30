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
@Table(name = "feedback")
class Feedback(
    user: UserAccount,
    chat: Chat,
    positive: Boolean,
    status: String = "pending",
    id: UUID? = null,
    createdAt: Instant = Instant.now(),
) {
    // 피드백을 생성한 사용자
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    var user: UserAccount = user

    // 피드백 대상 대화
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chat_id", nullable = false)
    var chat: Chat = chat

    // 긍정 피드백 여부
    @Column(name = "positive", nullable = false)
    var positive: Boolean = positive

    // 피드백 처리 상태
    @Column(name = "status", nullable = false, length = 20)
    var status: String = status

    // 피드백 기본 키
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = id

    // 피드백 생성 일시
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = createdAt
}
