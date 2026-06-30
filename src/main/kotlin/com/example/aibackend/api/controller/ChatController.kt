package com.example.aibackend.api.controller

import com.example.aibackend.application.service.ChatCreationResult
import com.example.aibackend.application.service.ChatService
import com.example.aibackend.application.service.CreateChatCommand
import com.example.aibackend.config.security.JwtAuthenticationInterceptor
import com.example.aibackend.infrastructure.security.AuthenticatedUser
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.time.Instant
import java.util.UUID
import java.util.concurrent.CompletableFuture

@Tag(name = "Chats", description = "Chat creation")
@RestController
@RequestMapping("/api/chats")
class ChatController(
    private val chatService: ChatService,
) {
    /**
     * [대화 생성 요청]
     * 질문을 입력받아 AI 답변을 생성하고 일반 JSON 또는 스트리밍 형태로 응답
     *
     * @param request 대화 생성 요청 본문
     * @param httpRequest 인증 사용자 정보를 포함한 HTTP 요청
     * @return 대화 생성 응답
     */
    @Operation(summary = "Create chat")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Chat created"),
            ApiResponse(responseCode = "400", description = "Invalid request", content = [Content()]),
            ApiResponse(responseCode = "401", description = "Authentication token is required", content = [Content()]),
        ],
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createChat(
        @Valid @RequestBody request: CreateChatRequest,
        httpRequest: HttpServletRequest,
        httpResponse: HttpServletResponse,
    ): Any {
        val result =
            chatService.createChat(
                command = request.toCommand(),
                authenticatedUser = httpRequest.authenticatedUser(),
            )

        if (request.isStreaming == true) {
            httpResponse.contentType = MediaType.TEXT_EVENT_STREAM_VALUE
            return result.toSseEmitter()
        }

        return result.toResponse()
    }

    /**
     * [대화 생성 명령 변환]
     * API 대화 생성 요청을 애플리케이션 명령 객체로 변환
     *
     * @return 대화 생성 명령 객체
     */
    private fun CreateChatRequest.toCommand(): CreateChatCommand =
        CreateChatCommand(
            question = question,
            model = model,
        )

    /**
     * [대화 생성 응답 변환]
     * 애플리케이션 대화 생성 결과를 API 응답 객체로 변환
     *
     * @return 대화 생성 API 응답 객체
     */
    private fun ChatCreationResult.toResponse(): ChatResponse =
        ChatResponse(
            threadId = threadId,
            chatId = chatId,
            question = question,
            answer = answer,
            provider = provider,
            model = model,
            createdAt = createdAt,
        )

    /**
     * [SSE 응답 변환]
     * 대화 생성 결과의 답변을 Server-Sent Events emitter로 변환
     *
     * @return SSE 이벤트 emitter
     */
    private fun ChatCreationResult.toSseEmitter(): SseEmitter {
        val emitter = SseEmitter()
        CompletableFuture.runAsync {
            try {
                answer
                    .split(Regex("\\s+"))
                    .filter { chunk -> chunk.isNotBlank() }
                    .forEach { chunk ->
                        emitter.send(SseEmitter.event().data(chunk))
                    }
                emitter.send(SseEmitter.event().name("done").data("[DONE]"))
                emitter.complete()
            } catch (exception: Exception) {
                emitter.completeWithError(exception)
            }
        }

        return emitter
    }

    /**
     * [인증 사용자 조회]
     * HTTP 요청 속성에서 인증 사용자 정보를 조회
     *
     * @return 인증 사용자 정보
     */
    private fun HttpServletRequest.authenticatedUser(): AuthenticatedUser =
        getAttribute(JwtAuthenticationInterceptor.AUTHENTICATED_USER_ATTRIBUTE) as AuthenticatedUser
}

@Schema(description = "Create chat request")
data class CreateChatRequest(
    @field:NotBlank
    @field:Schema(example = "오늘 날씨에 어울리는 점심 메뉴를 추천해줘")
    val question: String,
    @field:Schema(example = "false")
    val isStreaming: Boolean? = false,
    @field:Schema(example = "gpt-4o-mini")
    val model: String? = null,
)

@Schema(description = "Chat response")
data class ChatResponse(
    @field:Schema(example = "6f32a3e2-f542-4e48-a364-a87d407a7fc5")
    val threadId: UUID,
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
