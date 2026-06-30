package com.example.aibackend.api.controller

import com.example.aibackend.application.service.ChatResult
import com.example.aibackend.application.service.ChatService
import com.example.aibackend.application.service.ChatThreadListQuery
import com.example.aibackend.application.service.ChatThreadPageResult
import com.example.aibackend.application.service.ChatThreadResult
import com.example.aibackend.config.security.JwtAuthenticationInterceptor
import com.example.aibackend.infrastructure.security.AuthenticatedUser
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.util.UUID

@Tag(name = "Chat Threads", description = "Thread grouped chat queries")
@RestController
@RequestMapping("/api/chat-threads")
class ChatThreadController(
    private val chatService: ChatService,
) {
    /**
     * [스레드별 대화 목록 요청]
     * 인증 사용자의 권한에 따라 조회 가능한 스레드와 대화 목록을 페이지 단위로 응답
     *
     * @param page 조회할 페이지 번호
     * @param size 페이지 크기
     * @param direction 생성일시 정렬 방향
     * @param httpRequest 인증 사용자 정보를 포함한 HTTP 요청
     * @return 스레드별 대화 목록 페이지 응답
     */
    @Operation(summary = "List chat threads")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Chat threads returned"),
            ApiResponse(responseCode = "401", description = "Authentication token is required", content = [Content()]),
        ],
    )
    @GetMapping
    fun findChatThreads(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "desc") direction: String,
        httpRequest: HttpServletRequest,
    ): ChatThreadPageResponse =
        chatService
            .findChatThreads(
                query =
                    ChatThreadListQuery(
                        page = page,
                        size = size,
                        direction = direction,
                    ),
                authenticatedUser = httpRequest.authenticatedUser(),
            ).toResponse()

    /**
     * [스레드 페이지 응답 변환]
     * 애플리케이션 스레드 페이지 결과를 API 응답 객체로 변환
     *
     * @return 스레드 페이지 API 응답 객체
     */
    private fun ChatThreadPageResult.toResponse(): ChatThreadPageResponse =
        ChatThreadPageResponse(
            page = page,
            size = size,
            totalElements = totalElements,
            totalPages = totalPages,
            threads = threads.map { thread -> thread.toResponse() },
        )

    /**
     * [스레드 응답 변환]
     * 애플리케이션 스레드 결과를 API 응답 객체로 변환
     *
     * @return 스레드 API 응답 객체
     */
    private fun ChatThreadResult.toResponse(): ChatThreadResponse =
        ChatThreadResponse(
            threadId = threadId,
            userId = userId,
            userEmail = userEmail,
            userName = userName,
            createdAt = createdAt,
            lastChattedAt = lastChattedAt,
            chats = chats.map { chat -> chat.toResponse() },
        )

    /**
     * [대화 응답 변환]
     * 애플리케이션 대화 결과를 API 응답 객체로 변환
     *
     * @return 대화 API 응답 객체
     */
    private fun ChatResult.toResponse(): ChatItemResponse =
        ChatItemResponse(
            chatId = chatId,
            question = question,
            answer = answer,
            provider = provider,
            model = model,
            createdAt = createdAt,
        )

    /**
     * [인증 사용자 조회]
     * HTTP 요청 속성에서 인증 사용자 정보를 조회
     *
     * @return 인증 사용자 정보
     */
    private fun HttpServletRequest.authenticatedUser(): AuthenticatedUser =
        getAttribute(JwtAuthenticationInterceptor.AUTHENTICATED_USER_ATTRIBUTE) as AuthenticatedUser
}

@Schema(description = "Chat thread page response")
data class ChatThreadPageResponse(
    @field:Schema(example = "0")
    val page: Int,
    @field:Schema(example = "20")
    val size: Int,
    @field:Schema(example = "42")
    val totalElements: Long,
    @field:Schema(example = "3")
    val totalPages: Int,
    val threads: List<ChatThreadResponse>,
)

@Schema(description = "Chat thread response")
data class ChatThreadResponse(
    @field:Schema(example = "6f32a3e2-f542-4e48-a364-a87d407a7fc5")
    val threadId: UUID,
    @field:Schema(example = "3e1a9b60-00f5-47d2-ae8f-7f703c13dd32")
    val userId: UUID,
    @field:Schema(example = "member@example.com")
    val userEmail: String,
    @field:Schema(example = "홍길동")
    val userName: String,
    @field:Schema(example = "2026-06-30T07:23:45.123Z")
    val createdAt: Instant,
    @field:Schema(example = "2026-06-30T07:25:12.456Z")
    val lastChattedAt: Instant,
    val chats: List<ChatItemResponse>,
)

@Schema(description = "Chat item response")
data class ChatItemResponse(
    @field:Schema(example = "507f4049-9f13-4c6e-b89a-5dff90dbdd55")
    val chatId: UUID,
    @field:Schema(example = "오늘 날씨에 어울리는 점심 메뉴를 추천해줘")
    val question: String,
    @field:Schema(example = "FAKE_AI_RESPONSE[prompt=...,input=...]")
    val answer: String,
    @field:Schema(example = "fake")
    val provider: String,
    @field:Schema(example = "gpt-4o-mini")
    val model: String?,
    @field:Schema(example = "2026-06-30T07:23:45.123Z")
    val createdAt: Instant,
)
