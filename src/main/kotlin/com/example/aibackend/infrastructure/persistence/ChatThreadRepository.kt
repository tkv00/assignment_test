package com.example.aibackend.infrastructure.persistence

import com.example.aibackend.domain.model.ChatThread
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface ChatThreadRepository : JpaRepository<ChatThread, UUID> {
    /**
     * [최근 스레드 조회]
     * 사용자 식별자에 해당하는 삭제되지 않은 최근 스레드 조회
     *
     * @param userId 조회할 사용자 식별자
     * @return 마지막 대화 일시가 가장 최근인 스레드 또는 null
     */
    @Query(
        """
        SELECT *
        FROM chat_threads
        WHERE user_id = :userId
          AND deleted_at IS NULL
        ORDER BY last_chatted_at DESC
        LIMIT 1
        """,
        nativeQuery = true,
    )
    fun findLatestActiveByUserId(
        @Param("userId") userId: UUID,
    ): ChatThread?

    /**
     * [사용자 스레드 조회]
     * 사용자 식별자와 스레드 식별자에 해당하는 삭제되지 않은 스레드 조회
     *
     * @param id 조회할 스레드 식별자
     * @param userId 조회할 사용자 식별자
     * @return 식별자와 사용자에 일치하는 스레드 또는 null
     */
    @Query(
        """
        SELECT *
        FROM chat_threads
        WHERE id = :id
          AND user_id = :userId
          AND deleted_at IS NULL
        """,
        nativeQuery = true,
    )
    fun findActiveByIdAndUserId(
        @Param("id") id: UUID,
        @Param("userId") userId: UUID,
    ): ChatThread?
}
