package com.example.aibackend.infrastructure.persistence

import com.example.aibackend.domain.model.UserAccount
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UserAccountRepository : JpaRepository<UserAccount, UUID> {
    /**
     * [사용자 이메일 조회]
     * 이메일과 일치하는 사용자 계정 조회
     *
     * @param email 조회할 사용자 이메일
     * @return 이메일과 일치하는 사용자 계정 또는 null
     */
    fun findByEmail(email: String): UserAccount?
}
