package com.example.aibackend.config.security

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebMvcAuthConfig(
    private val jwtAuthenticationInterceptor: JwtAuthenticationInterceptor,
) : WebMvcConfigurer {
    /**
     * [인증 인터셉터 등록]
     * 회원 가입과 로그인을 제외한 모든 요청에 JWT 인증 인터셉터 적용
     *
     * @param registry MVC 인터셉터 레지스트리
     */
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry
            .addInterceptor(jwtAuthenticationInterceptor)
            .addPathPatterns("/**")
            .excludePathPatterns(
                "/api/auth/signup",
                "/api/auth/login",
            )
    }
}
