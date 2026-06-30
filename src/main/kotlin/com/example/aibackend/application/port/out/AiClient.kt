package com.example.aibackend.application.port.out

interface AiClient {
    /**
     * [AI 생성 요청]
     * 입력 명령을 기반으로 AI 생성 결과 생성
     *
     * @param command AI 생성에 필요한 프롬프트와 입력값 명령
     * @return AI 공급자 정보와 생성 결과
     */
    fun generate(command: AiGenerationCommand): AiGenerationResult
}

data class AiGenerationCommand(
    val prompt: String,
    val input: String,
)

data class AiGenerationResult(
    val content: String,
    val provider: String,
)
