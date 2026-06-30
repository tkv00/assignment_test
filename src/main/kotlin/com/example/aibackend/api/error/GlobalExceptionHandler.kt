package com.example.aibackend.api.error

import com.example.aibackend.application.service.DuplicateEmailException
import com.example.aibackend.application.service.InvalidCredentialsException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    /**
     * [이메일 중복 예외 처리]
     * 이미 가입된 이메일 요청을 충돌 오류 응답으로 변환
     *
     * @param exception 이메일 중복 예외
     * @return 충돌 오류 응답
     */
    @ExceptionHandler(DuplicateEmailException::class)
    fun handleDuplicateEmail(exception: DuplicateEmailException): ResponseEntity<ApiErrorResponse> =
        errorResponse(
            status = HttpStatus.CONFLICT,
            message = "Email already exists.",
        )

    /**
     * [인증 실패 예외 처리]
     * 로그인 인증 실패를 인증 오류 응답으로 변환
     *
     * @return 인증 오류 응답
     */
    @ExceptionHandler(InvalidCredentialsException::class)
    fun handleInvalidCredentials(): ResponseEntity<ApiErrorResponse> =
        errorResponse(
            status = HttpStatus.UNAUTHORIZED,
            message = "Invalid email or password.",
        )

    /**
     * [요청 검증 예외 처리]
     * Bean Validation 실패를 잘못된 요청 오류 응답으로 변환
     *
     * @param exception 요청 검증 예외
     * @return 잘못된 요청 오류 응답
     */
    @ExceptionHandler(MethodArgumentNotValidException::class, BindException::class)
    fun handleValidation(exception: Exception): ResponseEntity<ApiErrorResponse> =
        errorResponse(
            status = HttpStatus.BAD_REQUEST,
            message = "Request validation failed.",
        )

    /**
     * [오류 응답 생성]
     * HTTP 상태와 메시지를 표준 API 오류 응답으로 변환
     *
     * @param status HTTP 상태
     * @param message 오류 메시지
     * @return 표준 API 오류 응답
     */
    private fun errorResponse(
        status: HttpStatus,
        message: String,
    ): ResponseEntity<ApiErrorResponse> =
        ResponseEntity
            .status(status)
            .body(
                ApiErrorResponse(
                    status = status.value(),
                    error = status.reasonPhrase,
                    message = message,
                ),
            )
}
