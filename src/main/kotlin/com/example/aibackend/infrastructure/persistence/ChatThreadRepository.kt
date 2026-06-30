package com.example.aibackend.infrastructure.persistence

import com.example.aibackend.domain.model.ChatThread
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
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

    /**
     * [전체 활성 스레드 페이지 조회]
     * 삭제되지 않은 모든 스레드를 페이지 조건으로 조회
     *
     * @param pageable 페이지와 정렬 조건
     * @return 삭제되지 않은 전체 스레드 페이지
     */
    @Query(
        """
        SELECT thread
        FROM ChatThread thread
        WHERE thread.deletedAt IS NULL
        """,
    )
    fun findActivePage(pageable: Pageable): Page<ChatThread>

    /**
     * [사용자 활성 스레드 페이지 조회]
     * 사용자 식별자에 해당하는 삭제되지 않은 스레드를 페이지 조건으로 조회
     *
     * @param userId 조회할 사용자 식별자
     * @param pageable 페이지와 정렬 조건
     * @return 사용자의 삭제되지 않은 스레드 페이지
     */
    @Query(
        """
        SELECT thread
        FROM ChatThread thread
        WHERE thread.user.id = :userId
          AND thread.deletedAt IS NULL
        """,
    )
    fun findActivePageByUserId(
        @Param("userId") userId: UUID,
        pageable: Pageable,
    ): Page<ChatThread>
}
