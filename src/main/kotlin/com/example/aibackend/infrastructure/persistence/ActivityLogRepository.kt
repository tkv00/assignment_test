package com.example.aibackend.infrastructure.persistence

import com.example.aibackend.domain.model.ActivityLog
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ActivityLogRepository : JpaRepository<ActivityLog, UUID>
