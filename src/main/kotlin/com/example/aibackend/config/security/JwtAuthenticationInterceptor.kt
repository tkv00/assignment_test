package com.example.aibackend.config.security

import com.example.aibackend.api.error.ApiErrorResponse
import com.example.aibackend.infrastructure.security.JwtTokenProvider
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class JwtAuthenticationInterceptor(
    private val jwtTokenProvider: JwtTokenProvider,
    private val objectMapper: ObjectMapper,
) : HandlerInterceptor {
    /**
     * [요청 인증 전처리]
     * Authorization Bearer 토큰을 검증하고 인증 실패 시 요청 처리 중단
     *
     * @param request HTTP 요청
     * @param response HTTP 응답
     * @param handler 선택된 요청 핸들러
     * @return 요청 처리 계속 여부
     */
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        val token = extractBearerToken(request.getHeader(HttpHeaders.AUTHORIZATION))
        if (token == null || jwtTokenProvider.parseToken(token) == null) {
            writeUnauthorized(response)
            return false
        }

        return true
    }

    /**
     * [Bearer 토큰 추출]
     * Authorization 헤더에서 Bearer 토큰 값을 추출
     *
     * @param authorizationHeader Authorization 헤더 값
     * @return Bearer 토큰 문자열 또는 null
     */
    private fun extractBearerToken(authorizationHeader: String?): String? {
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            return null
        }

        return authorizationHeader.removePrefix(BEARER_PREFIX).trim().takeIf { token -> token.isNotBlank() }
    }

    /**
     * [인증 오류 응답 작성]
     * 인증 실패를 JSON 오류 응답으로 작성
     *
     * @param response HTTP 응답
     */
    private fun writeUnauthorized(response: HttpServletResponse) {
        response.status = HttpStatus.UNAUTHORIZED.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.writer.write(
            objectMapper.writeValueAsString(
                ApiErrorResponse(
                    status = HttpStatus.UNAUTHORIZED.value(),
                    error = HttpStatus.UNAUTHORIZED.reasonPhrase,
                    message = "Authentication token is required.",
                ),
            ),
        )
    }

    companion object {
        private const val BEARER_PREFIX = "Bearer "
    }
}
