package com.example.aibackend.infrastructure.ai

import com.example.aibackend.application.port.out.AiClient
import com.example.aibackend.application.port.out.AiGenerationCommand
import com.example.aibackend.application.port.out.AiGenerationResult
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(prefix = "app.ai", name = ["provider"], havingValue = "fake", matchIfMissing = true)
class FakeAiClient : AiClient {
    /**
     * [가짜 AI 응답 생성]
     * 외부 API 키 없이 재현 가능한 AI 응답 생성
     *
     * @param command AI 생성에 필요한 프롬프트와 입력값 명령
     * @return 정규화된 입력값을 포함한 가짜 AI 생성 결과
     */
    override fun generate(command: AiGenerationCommand): AiGenerationResult {
        val normalizedPrompt = command.prompt.trim().replace(Regex("\\s+"), " ")
        val normalizedInput = command.input.trim().replace(Regex("\\s+"), " ")

        return AiGenerationResult(
            content = "FAKE_AI_RESPONSE[prompt=$normalizedPrompt,input=$normalizedInput]",
            provider = "fake",
        )
    }
}
