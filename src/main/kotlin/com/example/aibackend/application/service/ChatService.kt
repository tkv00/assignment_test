package com.example.aibackend.application.service

import com.example.aibackend.application.port.out.AiChatMessage
import com.example.aibackend.application.port.out.AiClient
import com.example.aibackend.application.port.out.AiGenerationCommand
import com.example.aibackend.domain.model.ActivityLog
import com.example.aibackend.domain.model.Chat
import com.example.aibackend.domain.model.ChatThread
import com.example.aibackend.domain.model.UserAccount
import com.example.aibackend.domain.model.UserRole
import com.example.aibackend.infrastructure.persistence.ActivityLogRepository
import com.example.aibackend.infrastructure.persistence.ChatRepository
import com.example.aibackend.infrastructure.persistence.ChatThreadRepository
import com.example.aibackend.infrastructure.persistence.UserAccountRepository
import com.example.aibackend.infrastructure.security.AuthenticatedUser
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
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
     * [스레드별 대화 목록 조회]
     * 인증 사용자 권한에 따라 조회 가능한 스레드와 대화 목록을 페이지 단위로 조회
     *
     * @param query 대화 목록 조회 조건
     * @param authenticatedUser 인증된 사용자 정보
     * @return 스레드 단위로 그룹화된 대화 목록 페이지
     */
    @Transactional(readOnly = true)
    fun findChatThreads(
        query: ChatThreadListQuery,
        authenticatedUser: AuthenticatedUser,
    ): ChatThreadPageResult {
        val pageable =
            PageRequest.of(
                query.page.coerceAtLeast(0),
                query.size.coerceIn(MIN_PAGE_SIZE, MAX_PAGE_SIZE),
                Sort.by(query.direction.toSortDirection(), "createdAt"),
            )
        val threadPage =
            if (authenticatedUser.role == UserRole.ADMIN) {
                chatThreadRepository.findActivePage(pageable)
            } else {
                chatThreadRepository.findActivePageByUserId(
                    userId = authenticatedUser.id,
                    pageable = pageable,
                )
            }
        val threads = threadPage.content
        val threadIds = threads.mapNotNull(ChatThread::id)
        val chatsByThreadId =
            if (threadIds.isEmpty()) {
                emptyMap()
            } else {
                chatRepository
                    .findAllByThreadIdsOrderByCreatedAtAsc(threadIds)
                    .groupBy { chat -> requireNotNull(chat.thread.id) }
            }

        return ChatThreadPageResult(
            page = threadPage.number,
            size = threadPage.size,
            totalElements = threadPage.totalElements,
            totalPages = threadPage.totalPages,
            threads =
                threads.map { thread ->
                    thread.toResult(
                        chats = chatsByThreadId[requireNotNull(thread.id)].orEmpty(),
                    )
                },
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

    /**
     * [스레드 결과 변환]
     * 스레드 엔티티와 대화 목록을 스레드 목록 응답 결과로 변환
     *
     * @param chats 스레드에 포함된 대화 목록
     * @return 스레드 목록 응답 결과
     */
    private fun ChatThread.toResult(chats: List<Chat>): ChatThreadResult =
        ChatThreadResult(
            threadId = requireNotNull(id),
            userId = requireNotNull(user.id),
            userEmail = user.email,
            userName = user.name,
            createdAt = createdAt,
            lastChattedAt = lastChattedAt,
            chats = chats.map { chat -> chat.toResult() },
        )

    /**
     * [대화 결과 변환]
     * 대화 엔티티를 스레드 목록의 대화 응답 결과로 변환
     *
     * @return 대화 응답 결과
     */
    private fun Chat.toResult(): ChatResult =
        ChatResult(
            chatId = requireNotNull(id),
            question = question,
            answer = answer,
            provider = provider,
            model = model,
            createdAt = createdAt,
        )

    /**
     * [정렬 방향 변환]
     * API 정렬 방향 문자열을 Spring Data 정렬 방향으로 변환
     *
     * @return Spring Data 정렬 방향
     */
    private fun String.toSortDirection(): Sort.Direction =
        when (lowercase()) {
            "asc" -> Sort.Direction.ASC
            else -> Sort.Direction.DESC
        }

    companion object {
        private val THREAD_KEEP_ALIVE: Duration = Duration.ofMinutes(30)
        private const val CHAT_PROMPT = "Answer the user question using previous chats in the same thread."
        private const val MIN_PAGE_SIZE = 1
        private const val MAX_PAGE_SIZE = 100
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

data class ChatThreadListQuery(
    val page: Int,
    val size: Int,
    val direction: String,
)

data class ChatThreadPageResult(
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val threads: List<ChatThreadResult>,
)

data class ChatThreadResult(
    val threadId: UUID,
    val userId: UUID,
    val userEmail: String,
    val userName: String,
    val createdAt: Instant,
    val lastChattedAt: Instant,
    val chats: List<ChatResult>,
)

data class ChatResult(
    val chatId: UUID,
    val question: String,
    val answer: String,
    val provider: String,
    val model: String?,
    val createdAt: Instant,
)
