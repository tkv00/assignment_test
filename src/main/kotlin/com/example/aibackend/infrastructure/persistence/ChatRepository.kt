package com.example.aibackend.infrastructure.persistence

import com.example.aibackend.domain.model.Chat
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface ChatRepository : JpaRepository<Chat, UUID> {
    /**
     * [스레드 대화 목록 조회]
     * 스레드 식별자에 해당하는 대화 목록을 생성일시 오름차순으로 조회
     *
     * @param threadId 조회할 스레드 식별자
     * @return 생성일시 오름차순 대화 목록
     */
    @Query(
        """
        SELECT *
        FROM chats
        WHERE thread_id = :threadId
        ORDER BY created_at ASC
        """,
        nativeQuery = true,
    )
    fun findAllByThreadIdOrderByCreatedAtAsc(
        @Param("threadId") threadId: UUID,
    ): List<Chat>
}
