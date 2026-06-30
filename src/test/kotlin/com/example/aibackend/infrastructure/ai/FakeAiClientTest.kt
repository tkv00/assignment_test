package com.example.aibackend.infrastructure.ai

import com.example.aibackend.application.port.out.AiGenerationCommand
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class FakeAiClientTest {
    private val aiClient = FakeAiClient()

    /**
     * [가짜 AI 응답 단위 테스트]
     * 공백이 정규화된 결정적 가짜 AI 응답 생성 검증
     */
    @Test
    fun `returns deterministic fake AI response`() {
        val command =
            AiGenerationCommand(
                prompt = "  Summarize   this text ",
                input = " Example   input ",
            )

        val result = aiClient.generate(command)

        assertThat(result.provider).isEqualTo("fake")
        assertThat(result.content).isEqualTo(
            "FAKE_AI_RESPONSE[prompt=Summarize this text,input=Example input]",
        )
    }
}
