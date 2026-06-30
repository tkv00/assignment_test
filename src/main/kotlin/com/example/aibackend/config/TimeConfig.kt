package com.example.aibackend.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock

@Configuration
class TimeConfig {
    /**
     * [시계 설정]
     * 애플리케이션에서 현재 시각 계산에 사용할 UTC 시계 생성
     *
     * @return UTC 기준 시스템 시계
     */
    @Bean
    fun clock(): Clock = Clock.systemUTC()
}
