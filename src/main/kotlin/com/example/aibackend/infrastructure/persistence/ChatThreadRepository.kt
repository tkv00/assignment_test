package com.example.aibackend.infrastructure.persistence

import com.example.aibackend.domain.model.ChatThread
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ChatThreadRepository : JpaRepository<ChatThread, UUID>
