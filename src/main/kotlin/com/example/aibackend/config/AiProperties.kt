package com.example.aibackend.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.ai")
data class AiProperties(
    val provider: String = "fake",
    val apiKey: String? = null,
    val baseUrl: String? = null,
)
