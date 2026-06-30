package com.example.aibackend.domain.model

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "users")
class UserAccount(
    email: String,
    passwordHash: String,
    name: String,
    role: UserRole = UserRole.MEMBER,
    id: UUID? = null,
    createdAt: Instant = Instant.now(),
) {
    // 사용자 이메일 주소
    @Column(name = "email", nullable = false, unique = true, length = 320)
    var email: String = email

    // 해시 처리된 사용자 비밀번호
    @Column(name = "password_hash", nullable = false, columnDefinition = "text")
    var passwordHash: String = passwordHash

    // 사용자 표시 이름
    @Column(name = "name", nullable = false, length = 100)
    var name: String = name

    // 사용자 권한
    @Convert(converter = UserRoleConverter::class)
    @Column(name = "role", nullable = false, length = 20)
    var role: UserRole = role

    // 사용자 기본 키
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = id

    // 사용자 생성 일시
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = createdAt
}
