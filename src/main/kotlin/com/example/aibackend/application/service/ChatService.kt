package com.example.aibackend.application.service

import com.example.aibackend.application.port.out.AiChatMessage
import com.example.aibackend.application.port.out.AiClient
import com.example.aibackend.application.port.out.AiGenerationCommand
import com.example.aibackend.domain.model.ActivityLog
import com.example.aibackend.domain.model.Chat
import com.example.aibackend.domain.model.ChatThread
import com.example.aibackend.domain.model.UserAccount
import com.example.aibackend.infrastructure.persistence.ActivityLogRepository
import com.example.aibackend.infrastructure.persistence.ChatRepository
import com.example.aibackend.infrastructure.persistence.ChatThreadRepository
import com.example.aibackend.infrastructure.persistence.UserAccountRepository
import com.example.aibackend.infrastructure.security.AuthenticatedUser
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.UUID

@Service
class ChatService(
    private val userAccountRepository: UserAccountRepository,
    private val chatThreadRepository: ChatThreadRepository,
    private val chatRepository: ChatRepository,
    private val activityLogRepository: ActivityLogRepository,
    private val aiClient: AiClient,
    private val clock: Clock,
) {
    /**
     * [대화 생성]
     * 인증 사용자 질문을 기준으로 스레드를 결정하고 AI 답변과 함께 대화 저장
     *
     * @param command 대화 생성 요청 값
     * @param authenticatedUser 인증된 사용자 정보
     * @return 저장된 대화 생성 결과
     */
    @Transactional
    fun createChat(
        command: CreateChatCommand,
        authenticatedUser: AuthenticatedUser,
    ): ChatCreationResult {
        val user =
            userAccountRepository
                .findById(authenticatedUser.id)
                .orElseThrow(::AuthenticatedUserNotFoundException)
        val now = Instant.now(clock)
        val thread = findOrCreateThread(userId = authenticatedUser.id, user = user, now = now)
        val previousChats = chatRepository.findAllByThreadIdOrderByCreatedAtAsc(requireNotNull(thread.id))
        val aiResult =
            aiClient.generate(
                AiGenerationCommand(
                    prompt = CHAT_PROMPT,
                    input = command.question.trim(),
                    model = command.model,
                    history = previousChats.map { chat -> chat.toAiChatMessage() },
                ),
            )
        val chat =
            chatRepository.save(
                Chat(
                    thread = thread,
                    user = user,
                    question = command.question.trim(),
                    answer = aiResult.content,
                    provider = aiResult.provider,
                    model = aiResult.model ?: command.model?.trim()?.takeIf { model -> model.isNotEmpty() },
                    createdAt = now,
                ),
            )

        thread.lastChattedAt = now
        activityLogRepository.save(ActivityLog(activityType = "chat_created", user = user, createdAt = now))

        return ChatCreationResult(
            threadId = requireNotNull(thread.id),
            chatId = requireNotNull(chat.id),
            question = chat.question,
            answer = chat.answer,
            provider = chat.provider,
            model = chat.model,
            createdAt = chat.createdAt,
        )
    }

    /**
     * [스레드 결정]
     * 최근 스레드가 없거나 30분을 초과하면 새 스레드 생성
     *
     * @param userId 인증 사용자 식별자
     * @param user 스레드를 소유할 사용자
     * @param now 현재 시각
     * @return 대화를 저장할 스레드
     */
    private fun findOrCreateThread(
        userId: UUID,
        user: UserAccount,
        now: Instant,
    ): ChatThread {
        val latestThread = chatThreadRepository.findLatestActiveByUserId(userId)
        if (latestThread == null || latestThread.lastChattedAt.plus(THREAD_KEEP_ALIVE).isBefore(now)) {
            return chatThreadRepository.save(
                ChatThread(
                    user = user,
                    lastChattedAt = now,
                    createdAt = now,
                ),
            )
        }

        return latestThread
    }

    /**
     * [AI 대화 메시지 변환]
     * 저장된 대화를 AI 요청 이력 메시지로 변환
     *
     * @return AI 요청에 포함할 대화 이력 메시지
     */
    private fun Chat.toAiChatMessage(): AiChatMessage =
        AiChatMessage(
            question = question,
            answer = answer,
        )

    companion object {
        private val THREAD_KEEP_ALIVE: Duration = Duration.ofMinutes(30)
        private const val CHAT_PROMPT = "Answer the user question using previous chats in the same thread."
    }
}

data class CreateChatCommand(
    val question: String,
    val model: String?,
)

data class ChatCreationResult(
    val threadId: UUID,
    val chatId: UUID,
    val question: String,
    val answer: String,
    val provider: String,
    val model: String?,
    val createdAt: Instant,
)

class AuthenticatedUserNotFoundException : RuntimeException("Authenticated user was not found.")
