package com.example.aibackend.application.service

import com.example.aibackend.domain.model.ActivityLog
import com.example.aibackend.domain.model.UserAccount
import com.example.aibackend.domain.model.UserRole
import com.example.aibackend.infrastructure.persistence.ActivityLogRepository
import com.example.aibackend.infrastructure.persistence.UserAccountRepository
import com.example.aibackend.infrastructure.security.JwtTokenProvider
import com.example.aibackend.infrastructure.security.PasswordHasher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.Locale
import java.util.UUID

@Service
class AuthService(
    private val userAccountRepository: UserAccountRepository,
    private val activityLogRepository: ActivityLogRepository,
    private val passwordHasher: PasswordHasher,
    private val jwtTokenProvider: JwtTokenProvider,
) {
    /**
     * [회원 가입]
     * 이메일, 비밀번호, 이름으로 회원 계정을 생성하고 액세스 토큰 발급
     *
     * @param command 회원 가입 요청 값
     * @return 생성된 사용자와 액세스 토큰 정보
     */
    @Transactional
    fun signUp(command: SignUpCommand): AuthResult {
        val normalizedEmail = normalizeEmail(command.email)
        if (userAccountRepository.existsByEmail(normalizedEmail)) {
            throw DuplicateEmailException(normalizedEmail)
        }

        val user =
            userAccountRepository.save(
                UserAccount(
                    email = normalizedEmail,
                    passwordHash = passwordHasher.hash(command.password),
                    name = command.name.trim(),
                    role = UserRole.MEMBER,
                ),
            )
        activityLogRepository.save(ActivityLog(activityType = "signup", user = user))

        return AuthResult(
            userId = requireNotNull(user.id),
            email = user.email,
            name = user.name,
            role = user.role.databaseValue,
            accessToken = jwtTokenProvider.createToken(user),
            tokenType = TOKEN_TYPE,
        )
    }

    /**
     * [로그인]
     * 이메일과 비밀번호를 검증하고 액세스 토큰 발급
     *
     * @param command 로그인 요청 값
     * @return 로그인 사용자와 액세스 토큰 정보
     */
    @Transactional
    fun login(command: LoginCommand): AuthResult {
        val normalizedEmail = normalizeEmail(command.email)
        val user = userAccountRepository.findByEmail(normalizedEmail) ?: throw InvalidCredentialsException()
        if (!passwordHasher.matches(rawPassword = command.password, storedPasswordHash = user.passwordHash)) {
            throw InvalidCredentialsException()
        }

        activityLogRepository.save(ActivityLog(activityType = "login", user = user))

        return AuthResult(
            userId = requireNotNull(user.id),
            email = user.email,
            name = user.name,
            role = user.role.databaseValue,
            accessToken = jwtTokenProvider.createToken(user),
            tokenType = TOKEN_TYPE,
        )
    }

    /**
     * [이메일 정규화]
     * 인증 처리에 사용할 이메일을 소문자와 앞뒤 공백 제거 형태로 변환
     *
     * @param email 정규화할 이메일
     * @return 정규화된 이메일
     */
    private fun normalizeEmail(email: String): String = email.trim().lowercase(Locale.ROOT)

    companion object {
        private const val TOKEN_TYPE = "Bearer"
    }
}

data class SignUpCommand(
    val email: String,
    val password: String,
    val name: String,
)

data class LoginCommand(
    val email: String,
    val password: String,
)

data class AuthResult(
    val userId: UUID,
    val email: String,
    val name: String,
    val role: String,
    val accessToken: String,
    val tokenType: String,
)

class DuplicateEmailException(
    val email: String,
) : RuntimeException("Email already exists.")

class InvalidCredentialsException : RuntimeException("Invalid email or password.")
