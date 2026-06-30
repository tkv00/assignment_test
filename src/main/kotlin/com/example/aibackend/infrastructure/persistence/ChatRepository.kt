package com.example.aibackend.infrastructure.persistence

import com.example.aibackend.domain.model.Chat
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ChatRepository : JpaRepository<Chat, UUID>
