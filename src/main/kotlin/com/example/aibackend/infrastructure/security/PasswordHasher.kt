package com.example.aibackend.infrastructure.security

import org.springframework.stereotype.Component
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

@Component
class PasswordHasher {
    private val secureRandom = SecureRandom()

    /**
     * [비밀번호 해시 생성]
     * 입력된 평문 비밀번호를 임의 솔트와 함께 PBKDF2 해시 문자열로 변환
     *
     * @param rawPassword 해시 처리할 평문 비밀번호
     * @return 솔트와 해시 값을 포함한 저장용 비밀번호 문자열
     */
    fun hash(rawPassword: String): String {
        val salt = ByteArray(SALT_BYTE_LENGTH)
        secureRandom.nextBytes(salt)
        val digest = digest(salt = salt, rawPassword = rawPassword)

        return "$ALGORITHM:$ITERATION_COUNT:${encode(salt)}:${encode(digest)}"
    }

    /**
     * [비밀번호 일치 검증]
     * 평문 비밀번호가 저장된 해시 문자열과 일치하는지 검증
     *
     * @param rawPassword 검증할 평문 비밀번호
     * @param storedPasswordHash 저장된 비밀번호 해시 문자열
     * @return 비밀번호 일치 여부
     */
    fun matches(
        rawPassword: String,
        storedPasswordHash: String,
    ): Boolean {
        val parts = storedPasswordHash.split(":")
        if (parts.size != STORED_PART_COUNT || parts[0] != ALGORITHM) {
            return false
        }

        val iterationCount = parts[1].toIntOrNull() ?: return false
        val salt = decode(parts[2])
        val expectedDigest = decode(parts[3])
        val actualDigest = digest(salt = salt, rawPassword = rawPassword, iterationCount = iterationCount)

        return MessageDigest.isEqual(expectedDigest, actualDigest)
    }

    /**
     * [비밀번호 다이제스트 생성]
     * 솔트와 평문 비밀번호를 결합하여 PBKDF2 다이제스트 생성
     *
     * @param salt 비밀번호 해시에 사용할 솔트
     * @param rawPassword 해시 처리할 평문 비밀번호
     * @param iterationCount PBKDF2 반복 횟수
     * @return PBKDF2 다이제스트 바이트 배열
     */
    private fun digest(
        salt: ByteArray,
        rawPassword: String,
        iterationCount: Int = ITERATION_COUNT,
    ): ByteArray =
        SecretKeyFactory
            .getInstance("PBKDF2WithHmacSHA256")
            .generateSecret(
                PBEKeySpec(
                    rawPassword.toCharArray(),
                    salt,
                    iterationCount,
                    KEY_BIT_LENGTH,
                ),
            ).encoded

    /**
     * [Base64 인코딩]
     * 바이트 배열을 URL 안전 Base64 문자열로 변환
     *
     * @param value 인코딩할 바이트 배열
     * @return 패딩 없는 URL 안전 Base64 문자열
     */
    private fun encode(value: ByteArray): String = Base64.getUrlEncoder().withoutPadding().encodeToString(value)

    /**
     * [Base64 디코딩]
     * URL 안전 Base64 문자열을 바이트 배열로 변환
     *
     * @param value 디코딩할 Base64 문자열
     * @return 디코딩된 바이트 배열
     */
    private fun decode(value: String): ByteArray = Base64.getUrlDecoder().decode(value)

    companion object {
        private const val ALGORITHM = "pbkdf2-sha256"
        private const val ITERATION_COUNT = 120_000
        private const val KEY_BIT_LENGTH = 256
        private const val SALT_BYTE_LENGTH = 16
        private const val STORED_PART_COUNT = 4
    }
}
