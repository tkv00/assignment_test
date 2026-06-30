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
@Table(name = "chats")
class Chat(
    thread: ChatThread,
    user: UserAccount,
    question: String,
    answer: String,
    provider: String,
    model: String? = null,
    id: UUID? = null,
    createdAt: Instant = Instant.now(),
) {
    // 대화가 속한 스레드
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "thread_id", nullable = false)
    var thread: ChatThread = thread

    // 대화를 생성한 사용자
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    var user: UserAccount = user

    // 사용자 질문
    @Column(name = "question", nullable = false, columnDefinition = "text")
    var question: String = question

    // AI 답변
    @Column(name = "answer", nullable = false, columnDefinition = "text")
    var answer: String = answer

    // AI 공급자 이름
    @Column(name = "provider", nullable = false, length = 50)
    var provider: String = provider

    // AI 모델 이름
    @Column(name = "model", length = 100)
    var model: String? = model

    // 대화 기본 키
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = id

    // 대화 생성 일시
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = createdAt
}
