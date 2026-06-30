package com.example.aibackend.infrastructure.security

import com.example.aibackend.config.AuthProperties
import com.example.aibackend.domain.model.UserAccount
import com.example.aibackend.domain.model.UserRole
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.Instant
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    private val authProperties: AuthProperties,
    private val clock: Clock = Clock.systemUTC(),
) {
    private val signingKey: SecretKey = Keys.hmacShaKeyFor(authProperties.jwtSecret.toByteArray(Charsets.UTF_8))

    /**
     * [JWT 토큰 발급]
     * 사용자 식별자, 이메일, 권한을 포함한 Bearer 토큰 문자열 생성
     *
     * @param user 토큰을 발급할 사용자 계정
     * @return JWT 액세스 토큰 문자열
     */
    fun createToken(user: UserAccount): String {
        val userId = requireNotNull(user.id) { "User id is required to create token." }
        val issuedAt = Instant.now(clock)
        val expiresAt = issuedAt.plusSeconds(authProperties.tokenTtlSeconds)

        return Jwts
            .builder()
            .subject(userId.toString())
            .claim("email", user.email)
            .claim("role", user.role.databaseValue)
            .issuedAt(Date.from(issuedAt))
            .expiration(Date.from(expiresAt))
            .signWith(signingKey)
            .compact()
    }

    /**
     * [JWT 토큰 파싱]
     * JWT 액세스 토큰을 검증하고 인증 사용자 정보로 변환
     *
     * @param token 검증할 JWT 액세스 토큰
     * @return 토큰에 포함된 인증 사용자 정보 또는 null
     */
    fun parseToken(token: String): AuthenticatedUser? =
        try {
            val claims =
                Jwts
                    .parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .payload

            AuthenticatedUser(
                id = UUID.fromString(claims.subject),
                email = claims["email"] as String,
                role = UserRole.fromDatabaseValue(claims["role"] as String),
            )
        } catch (_: JwtException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        } catch (_: ClassCastException) {
            null
        }
}

data class AuthenticatedUser(
    val id: UUID,
    val email: String,
    val role: UserRole,
)
