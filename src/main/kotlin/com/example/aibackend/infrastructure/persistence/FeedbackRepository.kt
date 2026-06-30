package com.example.aibackend.infrastructure.persistence

import com.example.aibackend.domain.model.Feedback
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface FeedbackRepository : JpaRepository<Feedback, UUID>
