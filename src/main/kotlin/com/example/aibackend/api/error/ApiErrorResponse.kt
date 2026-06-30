package com.example.aibackend.api.error

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "API error response")
data class ApiErrorResponse(
    @field:Schema(example = "401")
    val status: Int,
    @field:Schema(example = "Unauthorized")
    val error: String,
    @field:Schema(example = "Authentication token is required.")
    val message: String,
)
