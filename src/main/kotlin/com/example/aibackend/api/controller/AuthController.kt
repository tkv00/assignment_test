package com.example.aibackend.api.controller

import com.example.aibackend.application.service.AuthResult
import com.example.aibackend.application.service.AuthService
import com.example.aibackend.application.service.LoginCommand
import com.example.aibackend.application.service.SignUpCommand
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Tag(name = "Auth", description = "User registration and JWT login")
@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
) {
    /**
     * [회원 가입 요청]
     * 이메일, 비밀번호, 이름으로 회원 가입을 처리하고 JWT 액세스 토큰 응답
     *
     * @param request 회원 가입 요청 본문
     * @return 생성된 사용자 정보와 JWT 액세스 토큰 응답
     */
    @Operation(summary = "Sign up")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "User registered"),
            ApiResponse(responseCode = "400", description = "Invalid request", content = [Content()]),
            ApiResponse(responseCode = "409", description = "Email already exists", content = [Content()]),
        ],
    )
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    fun signUp(
        @Valid @RequestBody request: SignUpRequest,
    ): AuthResponse =
        authService
            .signUp(request.toCommand())
            .toResponse()

    /**
     * [로그인 요청]
     * 이메일과 비밀번호를 검증하고 JWT 액세스 토큰 응답
     *
     * @param request 로그인 요청 본문
     * @return 로그인 사용자 정보와 JWT 액세스 토큰 응답
     */
    @Operation(summary = "Log in")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "User logged in"),
            ApiResponse(responseCode = "400", description = "Invalid request", content = [Content()]),
            ApiResponse(responseCode = "401", description = "Invalid credentials", content = [Content()]),
        ],
    )
    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginRequest,
    ): AuthResponse =
        authService
            .login(request.toCommand())
            .toResponse()

    /**
     * [회원 가입 명령 변환]
     * API 회원 가입 요청을 애플리케이션 명령 객체로 변환
     *
     * @return 회원 가입 명령 객체
     */
    private fun SignUpRequest.toCommand(): SignUpCommand =
        SignUpCommand(
            email = email,
            password = password,
            name = name,
        )

    /**
     * [로그인 명령 변환]
     * API 로그인 요청을 애플리케이션 명령 객체로 변환
     *
     * @return 로그인 명령 객체
     */
    private fun LoginRequest.toCommand(): LoginCommand =
        LoginCommand(
            email = email,
            password = password,
        )

    /**
     * [인증 응답 변환]
     * 애플리케이션 인증 결과를 API 응답 객체로 변환
     *
     * @return 인증 API 응답 객체
     */
    private fun AuthResult.toResponse(): AuthResponse =
        AuthResponse(
            userId = userId,
            email = email,
            name = name,
            role = role,
            accessToken = accessToken,
            tokenType = tokenType,
        )
}

@Schema(description = "Sign up request")
data class SignUpRequest(
    @field:Email
    @field:NotBlank
    @field:Schema(example = "member@example.com")
    val email: String,
    @field:NotBlank
    @field:Schema(example = "password123")
    val password: String,
    @field:NotBlank
    @field:Schema(example = "홍길동")
    val name: String,
)

@Schema(description = "Login request")
data class LoginRequest(
    @field:Email
    @field:NotBlank
    @field:Schema(example = "member@example.com")
    val email: String,
    @field:NotBlank
    @field:Schema(example = "password123")
    val password: String,
)

@Schema(description = "Authentication response")
data class AuthResponse(
    @field:Schema(example = "6f32a3e2-f542-4e48-a364-a87d407a7fc5")
    val userId: UUID,
    @field:Schema(example = "member@example.com")
    val email: String,
    @field:Schema(example = "홍길동")
    val name: String,
    @field:Schema(example = "member")
    val role: String,
    @field:Schema(example = "eyJhbGciOiJIUzI1NiJ9...")
    val accessToken: String,
    @field:Schema(example = "Bearer")
    val tokenType: String,
)
