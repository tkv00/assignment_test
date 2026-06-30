package com.example.aibackend.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.sql.DataSource

@Tag(name = "Health", description = "Application and database health checks")
@RestController
@RequestMapping("/api")
class HealthController(
    private val dataSource: DataSource,
) {
    /**
     * [헬스 체크 조회]
     * 애플리케이션 상태와 데이터베이스 연결 상태 조회
     *
     * @return 애플리케이션 상태와 데이터베이스 상태 응답
     */
    @Operation(summary = "Check application health")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Application is reachable and returns database status",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = HealthResponse::class),
                    ),
                ],
            ),
        ],
    )
    @GetMapping("/health")
    fun health(): HealthResponse {
        val databaseStatus =
            dataSource.connection.use { connection ->
                if (connection.isValid(1)) "up" else "down"
            }

        return HealthResponse(
            status = if (databaseStatus == "up") "ok" else "degraded",
            database = databaseStatus,
        )
    }
}

@Schema(description = "Application health response")
data class HealthResponse(
    @field:Schema(example = "ok")
    val status: String,
    @field:Schema(example = "up")
    val database: String,
)
