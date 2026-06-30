# AI Chatbot Backend API

Kotlin과 Spring Boot로 구현한 AI 챗봇 API 서버입니다. 제한 시간 3시간 안에서 고객 시연에 필요한 핵심 흐름을 우선 구현했습니다. 외부 AI API Key 없이도 실행할 수 있도록 FakeAiClient를 기본 Provider로 두었고, 실제 Provider 교체와 문서 기반 답변 확장은 AiClient 포트를 통해 이어갈 수 있도록 구성했습니다.

이 저장소는 공개 업로드를 전제로 작성했습니다. 특정 회사명, 담당자명, 과제 원문을 식별할 수 있는 정보는 코드와 문서에 포함하지 않았습니다.

⸻

## 1. 구현 범위 요약

구현 완료

* 회원가입
* 로그인
* JWT Access Token 발급
* Spring Security 없이 JWT 인증 인터셉터 구현
* PBKDF2 기반 비밀번호 해시 저장
* 대화 생성 API
* model 요청 옵션 수신
* isStreaming=true 요청 시 SSE 형태 응답
* 사용자별 30분 기준 스레드 생성/재사용
* 같은 스레드의 이전 대화 목록을 AI 요청 이력으로 전달
* 스레드 단위 대화 목록 조회
* 일반 사용자/관리자 권한에 따른 대화 조회 범위 분기
* PostgreSQL 15.8 기반 Flyway 스키마 관리
* Swagger/OpenAPI 문서 제공
* 외부 API Key 없이 실행 가능한 FakeAiClient 구현

제한 시간 내 미완성

* 실제 OpenAI API 연동
* 고객사 대외비 문서 학습
* Embedding 기반 Vector Search
* RAG 파이프라인
* 피드백 API 전체 구현
* 관리자 활동 기록 조회 API
* CSV 보고서 생성 API
* 스레드 삭제 API

피드백, 활동 기록, 보고서 기능은 요구사항에 포함되어 있었지만, 남은 시간 안에서 API 형태만 추가하는 방식은 피했습니다. 대신 고객 시연 흐름인 인증, 대화 생성, 스레드 유지, 대화 목록 조회를 우선했습니다.

⸻

## 2. 과제 분석

요구사항을 처음 읽었을 때 핵심은 단순한 챗봇 API 구현보다, 제한된 상황에서 구현 범위를 어떻게 조정하는지에 있다고 보았습니다.

주어진 조건은 다음과 같았습니다.

1. 고객사는 AI Provider의 존재는 알고 있지만 API Spec을 깊게 이해하지 못합니다.
2. 영업 직원과 고객사 담당자는 연락이 되지 않는 상황입니다.
3. 지원자는 유일한 개발자입니다.
4. 총 시간은 문서 작업을 포함해 3시간입니다.
5. 채점 기준에는 과제 분석, AI 활용 방식, 어려웠던 기능 설명이 포함되어 있습니다.
6. 구현량의 배점은 낮다고 명시되어 있습니다.

따라서 성공 기준을 “요구사항 전체 구현”이 아니라 “시연 가능한 핵심 API 흐름과 이후 확장 가능한 구조 제시”로 잡았습니다.

1차 시연 흐름은 다음과 같이 정의했습니다.

sequenceDiagram
    actor User as 사용자
    participant Auth as Auth API
    participant Chat as Chat API
    participant Thread as Thread Logic
    participant AI as AiClient
    participant DB as PostgreSQL
    User->>Auth: 회원가입 또는 로그인
    Auth->>DB: 사용자 저장 또는 조회
    Auth-->>User: JWT Access Token 발급
    User->>Chat: 질문 생성 요청 + Bearer Token
    Chat->>Thread: 최근 스레드 조회
    Thread->>DB: 사용자별 최신 활성 스레드 조회
    alt 첫 질문 또는 마지막 질문 후 30분 초과
        Thread->>DB: 새 스레드 생성
    else 마지막 질문 후 30분 이내
        Thread-->>Chat: 기존 스레드 재사용
    end
    Chat->>DB: 같은 스레드의 이전 대화 조회
    Chat->>AI: 질문 + 이전 대화 이력 전달
    AI-->>Chat: 답변 반환
    Chat->>DB: 질문/답변 저장
    Chat-->>User: 답변 응답

이 흐름을 먼저 구현하면 고객은 API를 통해 “인증된 사용자가 AI 응답 생성 흐름을 사용할 수 있다”는 목표를 확인할 수 있습니다. 동시에 스레드 단위 대화 이력을 저장하므로, 향후 실제 OpenAI API나 RAG 기반 문서 검색을 붙일 때 기존 API 흐름을 크게 바꾸지 않아도 됩니다.

⸻

## 3. 우선순위 결정

3시간 안에 모든 요구사항을 완성하는 것은 현실적이지 않다고 보았습니다. 그래서 기능을 세 단계로 나누었습니다.

### 1순위: 시연 흐름에 반드시 필요한 기능

* 회원가입
* 로그인
* JWT 인증
* 대화 생성
* AI 응답 생성 흐름
* 스레드 생성/재사용
* 대화 목록 조회

이 기능들은 고객이 API를 호출했을 때 챗봇 서비스의 핵심 흐름을 확인하는 최소 경로입니다.

### 2순위: 확장 가능성을 보여주는 구조

* AiClient 포트 분리
* FakeAiClient 구현
* ChatThread, Chat, ActivityLog, Feedback 스키마 구성
* Flyway 기반 DB 마이그레이션
* Swagger/OpenAPI 도입

이 항목들은 당장 모든 기능을 완성하지 못하더라도, 다음 개발자가 이어서 확장할 수 있는 기반을 남기기 위해 선택했습니다.

### 3순위: 시간이 남으면 구현할 기능

* 피드백 생성/조회
* 피드백 상태 변경
* 관리자 활동 기록 조회
* CSV 보고서 생성
* 스레드 삭제

결과적으로 3순위 기능은 완성하지 못했습니다. 다만 피드백과 활동 기록은 도메인 모델과 스키마 일부를 먼저 잡아두어 이후 API 구현으로 이어질 수 있게 했습니다.

⸻

## 4. 기술 스택

* Kotlin 1.9.25
* Spring Boot 3.3.5
* Java 17
* PostgreSQL 15.8
* Spring Data JPA
* Flyway
* Spring MVC HandlerInterceptor
* jjwt
* Springdoc OpenAPI
* Docker Compose
* Gradle Wrapper

Spring Security는 도입하지 않았습니다. 요구사항의 인증 범위가 단순했고, 제한 시간 안에서 인증 흐름을 직접 드러내는 편이 낫다고 보았습니다. 대신 JWT 검증은 HandlerInterceptor에서 수행하고, 비밀번호는 JDK 기본 API를 사용해 PBKDF2 방식으로 해시 저장했습니다.

⸻

## 5. 프로젝트 구조

src/main/kotlin/com/example/aibackend
  api/
    controller/        HTTP API Controller
    error/             공통 예외 응답 처리
  application/
    port/out/          외부 Provider 의존 포트
    service/           유스케이스 서비스
  config/              설정, 인증 인터셉터, OpenAPI, Clock
  domain/model/        JPA 엔티티와 도메인 모델
  infrastructure/
    ai/                AI Provider 어댑터
    persistence/       Spring Data JPA Repository
    security/          JWT, 비밀번호 해시
src/main/resources/db/migration
  Flyway SQL 마이그레이션

큰 틀은 레이어드 구조를 사용했습니다. 다만 AI Provider는 외부 연동 변경 가능성이 높다고 보고 application.port.out.AiClient로 분리했습니다.

flowchart LR
    Controller[API Controller]
    Service[Application Service]
    Domain[Domain Model]
    Repo[Repository]
    DB[(PostgreSQL)]
    Port[AiClient Port]
    Fake[FakeAiClient]
    OpenAI[Future OpenAI Adapter]
    RAG[Future RAG Adapter]
    Controller --> Service
    Service --> Domain
    Service --> Repo
    Repo --> DB
    Service --> Port
    Port --> Fake
    Port -.-> OpenAI
    Port -.-> RAG

⸻

## 6. 실행 방법

cp .env.example .env
docker compose up -d postgres
set -a
source .env
set +a
./gradlew bootRun

기본 포트는 8080입니다.

Swagger UI:   http://localhost:8080/swagger-ui.html
OpenAPI JSON: http://localhost:8080/v3/api-docs

.env에는 다음 값이 필요합니다.

JWT_SECRET=change-this-jwt-secret-at-least-32-bytes
JWT_TOKEN_TTL_SECONDS=3600

⸻

## 7. API 사용 예시

### 7.1 회원가입

curl -X POST http://localhost:8080/api/auth/signup \
  -H 'Content-Type: application/json' \
  -d '{
    "email": "member@example.com",
    "password": "password123",
    "name": "홍길동"
  }'

응답 예시:

{
  "userId": "6f32a3e2-f542-4e48-a364-a87d407a7fc5",
  "email": "member@example.com",
  "name": "홍길동",
  "role": "member",
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer"
}

### 7.2 로그인

curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{
    "email": "member@example.com",
    "password": "password123"
  }'

### 7.3 대화 생성

curl -X POST http://localhost:8080/api/chats \
  -H "Authorization: Bearer <accessToken>" \
  -H 'Content-Type: application/json' \
  -d '{
    "question": "이 API가 어떤 방식으로 AI 답변을 생성하는지 설명해줘",
    "isStreaming": false,
    "model": "gpt-4o-mini"
  }'

응답 예시:

{
  "threadId": "6f32a3e2-f542-4e48-a364-a87d407a7fc5",
  "chatId": "507f4049-9f13-4c6e-b89a-5dff90dbdd55",
  "question": "이 API가 어떤 방식으로 AI 답변을 생성하는지 설명해줘",
  "answer": "FAKE_AI_RESPONSE[prompt=...,input=...]",
  "provider": "fake",
  "model": "gpt-4o-mini",
  "createdAt": "2026-06-30T07:23:45.123Z"
}

### 7.4 대화 목록 조회

curl -X GET "http://localhost:8080/api/chat-threads?page=0&size=20&direction=desc" \
  -H "Authorization: Bearer <accessToken>"

일반 사용자는 본인의 스레드와 대화만 조회합니다. 관리자는 삭제되지 않은 모든 사용자의 스레드와 대화를 조회합니다.

⸻

## 8. 인증과 인가 동작 원리

회원가입과 로그인은 인증 없이 접근할 수 있습니다. 그 외 요청은 JWT Bearer Token이 필요합니다.

sequenceDiagram
    actor Client
    participant Interceptor as JwtAuthenticationInterceptor
    participant Jwt as JwtTokenProvider
    participant Controller
    Client->>Interceptor: API 요청 + Authorization Header
    alt 회원가입 또는 로그인
        Interceptor-->>Controller: 인증 검사 제외
    else 인증 대상 API
        Interceptor->>Jwt: 토큰 검증
        alt 토큰 유효
            Jwt-->>Interceptor: 사용자 ID, 이메일, 권한 반환
            Interceptor->>Controller: request attribute에 인증 사용자 저장
        else 토큰 없음 또는 만료
            Interceptor-->>Client: 401 Unauthorized
        end
    end

인가 처리는 서비스에서 명시적으로 분기합니다. 일반 사용자는 자신이 생성한 리소스만 조회할 수 있고, 관리자는 전체 리소스를 조회할 수 있습니다.

⸻

## 9. 대화 생성과 스레드 동작 원리

대화 생성 시 가장 중요한 규칙은 30분 기준 스레드 유지입니다.

* 사용자의 첫 질문이면 새 스레드를 생성합니다.
* 마지막 질문 후 30분이 지나면 새 스레드를 생성합니다.
* 마지막 질문 후 30분 이내면 기존 스레드를 재사용합니다.
* AI 요청에는 같은 스레드의 이전 대화 목록을 함께 전달합니다.

flowchart TD
    A[대화 생성 요청] --> B[JWT 인증 사용자 조회]
    B --> C[사용자 최신 활성 스레드 조회]
    C --> D{최근 스레드 존재?}
    D -- 없음 --> E[새 스레드 생성]
    D -- 있음 --> F{마지막 대화 후 30분 초과?}
    F -- 예 --> E
    F -- 아니오 --> G[기존 스레드 재사용]
    E --> H[이전 대화 목록 조회]
    G --> H
    H --> I[AiClient 호출]
    I --> J[질문/답변 저장]
    J --> K[ActivityLog 저장]
    K --> L[응답 반환]

현재 AI 응답은 FakeAiClient가 생성합니다. 이 선택은 외부 API Key 없이 로컬과 평가 환경에서 동일하게 실행되도록 하기 위한 결정입니다.

⸻

## 10. AI Provider 확장 구조

실제 AI Provider를 서비스 코드에 직접 연결하지 않았습니다. ChatService는 AiClient 포트만 알고, 현재 실행 환경에서는 FakeAiClient가 해당 포트를 구현합니다.

classDiagram
    class ChatService {
        +createChat(command, authenticatedUser)
        +findChatThreads(query, authenticatedUser)
    }
    class AiClient {
        <<interface>>
        +generate(command) AiGenerationResult
    }
    class FakeAiClient {
        +generate(command) AiGenerationResult
    }
    class OpenAiClient {
        +generate(command) AiGenerationResult
    }
    class RagAiClient {
        +generate(command) AiGenerationResult
    }
    ChatService --> AiClient
    AiClient <|.. FakeAiClient
    AiClient <|.. OpenAiClient
    AiClient <|.. RagAiClient

향후 실제 OpenAI API를 붙일 경우 OpenAiClient를 추가하면 됩니다. 고객사의 대외비 문서를 활용해야 한다면 RagAiClient 또는 별도의 Retrieval 계층을 추가해 관련 문서를 검색한 뒤 AiGenerationCommand.history 또는 prompt context에 포함하는 구조로 확장할 수 있습니다.

⸻

## 11. AI 활용 방식

이번 과제에서 AI는 구현을 맡기는 용도가 아니라, 요구사항 분해, 설계 검토, 코드 점검, 문서 정리에 사용했습니다. 최종 구현 범위와 제외 범위는 제한 시간, 시연 가능성, 유지보수성을 기준으로 직접 결정했습니다.

### 11.1 초기 세팅 검토

초기에는 Kotlin, Spring Boot, PostgreSQL, Flyway, Swagger 조합으로 시작했습니다. AI를 활용해 H2와 PostgreSQL 중 어떤 선택이 과제에 맞는지 비교했습니다.

H2는 빠르게 시작할 수 있지만, 사용자, 스레드, 대화, 피드백, 활동 기록처럼 관계가 있는 데이터가 많았습니다. 그래서 실제 운영 환경에 가까운 PostgreSQL과 Flyway를 선택했습니다.

Swagger도 도입했습니다. 고객이나 평가자가 API 요청/응답을 바로 확인할 수 있다는 장점이 있었기 때문입니다. 다만 이 선택은 초기 구현 시간을 줄이는 데는 불리했습니다.

### 11.2 요구사항 분해

AI를 활용해 요구사항을 기능 단위로 나누었습니다.

* 회원가입
* 로그인
* JWT 인증
* 대화 생성
* 30분 기준 스레드 생성/유지
* 스레드 단위 대화 목록 조회
* 피드백 관리
* 관리자 활동 기록
* CSV 보고서
* 향후 대외비 문서 학습

이 중 3시간 안에 반드시 보여줘야 할 흐름은 “API를 통해 AI 답변을 생성할 수 있다”는 부분이라고 보았습니다. 그래서 인증, 대화 생성, 스레드 유지, 대화 목록 조회를 우선 구현했습니다.

피드백, 관리자 보고서, 실제 OpenAI 연동, RAG는 후순위로 분리했습니다. 구현량을 늘리기보다 동작 가능한 핵심 흐름을 먼저 만드는 편이 낫다고 보았습니다.

### 11.3 인증/인가 방식 검토

Spring Security는 도입하지 않았습니다. 대신 Spring MVC HandlerInterceptor 기반 인증 흐름을 검토했습니다.

최종 구조는 다음과 같습니다.

1. 회원가입과 로그인은 인증 없이 허용합니다.
2. 로그인 성공 시 JWT를 발급합니다.
3. 이후 요청은 Authorization: Bearer {token} 헤더를 사용합니다.
4. 인터셉터가 토큰을 검증합니다.
5. 검증된 사용자 ID와 권한을 요청 객체에 저장합니다.
6. 각 API는 이 정보를 기준으로 본인 리소스 접근 여부를 판단합니다.

비밀번호는 평문 저장을 피했습니다. Spring Security 없이 처리하기 위해 JDK의 PBKDF2WithHmacSHA256 기반 해시 방식을 적용했습니다.

### 11.4 AI Provider 구조 검토

실제 OpenAI API 연동은 제외했습니다. 3시간 안에 API Key, 네트워크 오류, Provider 응답 포맷, 예외 처리까지 안정적으로 다루기 어렵다고 보았습니다.

대신 AiClient 포트를 만들고, 기본 구현체로 FakeAiClient를 두었습니다. 이 구조를 통해 외부 API Key 없이도 대화 생성 흐름을 확인할 수 있습니다.

현재 구조에서는 다음 흐름을 검증할 수 있습니다.

1. 사용자가 질문을 보냅니다.
2. 서버가 스레드를 결정합니다.
3. 같은 스레드의 이전 대화 목록을 조회합니다.
4. AiClient에 질문, 모델, 이전 대화 목록을 전달합니다.
5. 생성된 답변을 저장합니다.

추후 실제 OpenAI 연동이 필요하면 AiClient 구현체만 교체하면 됩니다. 대외비 문서 학습이 필요할 경우에도 AiClient 호출 전에 문서 검색 계층을 추가해 RAG 구조로 확장할 수 있습니다.

### 11.5 스레드 로직 점검

대화 생성에서 가장 많이 검토한 부분은 30분 기준 스레드 유지 로직입니다.

조건은 다음과 같이 정리했습니다.

* 첫 질문이면 새 스레드를 생성합니다.
* 최근 스레드가 없으면 새 스레드를 생성합니다.
* 마지막 질문 후 30분이 지나면 새 스레드를 생성합니다.
* 30분 이내라면 기존 스레드를 재사용합니다.
* AI 요청 시 같은 스레드의 이전 대화 목록을 함께 전달합니다.
* 삭제된 스레드는 조회와 재사용 대상에서 제외합니다.

이 로직이 중요한 이유는 스레드가 대화 맥락의 단위이기 때문입니다. 스레드가 잘못 나뉘면 이전 대화가 AI 요청에 포함되지 않습니다. 반대로 너무 오래 유지되면 불필요한 맥락이 포함됩니다.

### 11.6 코드 점검과 문서화

구현 중간에는 다음 항목을 점검했습니다.

* 회원가입과 로그인 외 API가 인증 대상인지
* Swagger와 Health Check 경로를 인증에서 제외할지
* 일반 사용자가 다른 사용자의 대화를 조회할 수 없는지
* 관리자가 전체 대화를 조회할 수 있는지
* 삭제된 스레드가 조회되지 않는지
* 비밀번호가 해시되어 저장되는지
* AI Provider가 서비스 로직에 직접 묶이지 않았는지
* model, isStreaming 요청 값이 DTO에 반영되어 있는지
* README에 구현 범위와 제한사항이 명확히 적혔는지

문서에는 단순 실행 방법만 적지 않고, 채점 기준에 맞춰 과제 분석, 우선순위, 구현 과정, 어려웠던 기능, 부족했던 점, 보완 계획을 함께 정리했습니다.

### 11.7 활용 중 어려웠던 점

가장 어려웠던 점은 제안 범위가 쉽게 커진다는 점이었습니다. 요구사항 전체를 기준으로 보면 피드백, 관리자 보고서, 실제 OpenAI 연동, RAG, Vector DB, 테스트 코드까지 모두 구현 대상으로 보입니다.

하지만 이 과제는 3시간 안에 구현과 문서화를 모두 끝내야 했습니다. 그래서 제안된 기능을 모두 구현하지 않고, 시연 가능한 핵심 흐름을 먼저 완성하는 쪽으로 범위를 줄였습니다.

결과적으로 AI는 다음 용도로 제한해 사용했습니다.

* 요구사항 분해
* 우선순위 검토
* 인증 흐름 점검
* 스레드 조건 점검
* 코드 리뷰
* README 구조화

최종 구현 범위, 제외 범위, 기술 선택은 직접 결정했습니다.

⸻

## 12. 구현 중 가장 어려웠던 기능

가장 어려웠던 기능은 대화 생성 시 사용자별 스레드를 자동으로 결정하는 로직이었습니다.

요구사항은 단순히 질문과 답변을 저장하는 것이 아니었습니다. 사용자의 첫 질문이거나 마지막 질문 후 30분이 지난 경우 새 스레드를 만들고, 30분 이내의 질문은 기존 스레드를 유지해야 했습니다. 또한 AI Provider 요청 시 같은 스레드의 이전 대화 목록을 함께 전달해야 했습니다.

이 로직이 어려웠던 이유는 다음과 같습니다.

1. 사용자별로 최신 활성 스레드를 찾아야 합니다.
2. 마지막 대화 시각과 현재 시각을 비교해야 합니다.
3. 새 스레드 생성과 기존 스레드 재사용이 한 트랜잭션 안에서 결정되어야 합니다.
4. AI 요청 이전에 같은 스레드의 대화 이력을 조회해야 합니다.
5. 저장 이후 스레드의 lastChattedAt을 갱신해야 합니다.

이 부분을 ChatService.createChat()의 핵심 흐름으로 두었습니다. 컨트롤러는 요청/응답 변환만 담당하고, 스레드 결정, AI 호출, 대화 저장, 활동 로그 저장은 서비스에서 처리하도록 했습니다.

⸻

## 13. 부족했던 점과 원인 분석

이번 과제에서 가장 부족했던 점은 초반 시간 배분입니다.

초반에 “지속적으로 확장 개발 가능해야 한다”는 조건을 강하게 의식했습니다. 그래서 API의 실제 완성보다 확장 기반을 먼저 다지는 데 많은 시간을 사용했습니다.

구체적으로는 다음 작업에 시간이 많이 들어갔습니다.

* 도메인 스키마를 먼저 넓게 설계
* UserAccount, ChatThread, Chat, Feedback, ActivityLog, AppMetadata 엔티티 구성
* Flyway 마이그레이션 정리
* 세부 패키지 구조 분리
* Swagger/OpenAPI 설정
* Docker Compose, Makefile, secret scan 등 제출 환경 정리
* 외부 AI Provider 교체를 고려한 AiClient 포트 설계

이 선택은 장점도 있었습니다. 프로젝트가 단순 데모 코드처럼 보이지 않고, 이후 기능을 붙일 수 있는 기반은 생겼습니다. 그러나 3시간 과제에서는 이 판단이 일부 패착이었습니다.

가장 큰 이유는 요구사항의 채점 기준에 “구현의 양은 배점이 낮다”는 문장이 있었기 때문입니다. 이 문장을 “구조와 문서가 중요하다”로 해석했지만, 실제로는 “많은 기능보다 핵심 흐름을 먼저 안정적으로 완성하라”는 의미에 더 가까웠다고 봅니다.

결과적으로 피드백 API, 관리자 활동 기록, CSV 보고서, 스레드 삭제는 완성하지 못했습니다. 초반에 스키마와 폴더 구조를 다듬는 데 사용한 시간을 줄이고, 인증 → 대화 생성 → 대화 목록 조회 → 스레드 삭제 → 피드백 생성 순서로 더 빠르게 수직 절단했다면 더 많은 요구사항을 동작 가능한 API로 제출할 수 있었을 것입니다.

다음에 같은 상황이라면 다르게 할 점

다음에는 처음 30분을 구조 설계에 모두 쓰지 않고, 15분 안에 다음 수직 흐름을 먼저 완성하겠습니다.

회원가입 → 로그인 → JWT 인증 → 대화 생성 → 대화 목록 조회

그 뒤 남은 시간에 피드백, 관리자 보고, 문서화를 붙이는 방식이 더 적절합니다. 확장성은 초반부터 모든 스키마를 넓게 잡는 방식보다, 핵심 흐름에 변경 지점을 명확히 남기는 방식으로 확보하는 편이 더 낫다고 판단했습니다.

flowchart TD
    A[초기 판단] --> B[확장성 우선]
    B --> C[스키마와 폴더 구조 선설계]
    B --> D[Swagger와 실행 환경 정리]
    C --> E[핵심 API 구현 시간 감소]
    D --> E
    E --> F[피드백/보고서/삭제 API 미완성]
    G[개선 방향] --> H[수직 흐름 먼저 완성]
    H --> I[회원가입]
    I --> J[로그인]
    J --> K[대화 생성]
    K --> L[대화 목록 조회]
    L --> M[피드백/보고서 확장]

⸻

## 14. 이후 보완 계획

남은 요구사항은 다음 순서로 보완할 수 있습니다.

1. 스레드 삭제 API
    * 일반 사용자는 본인 스레드만 삭제
    * 관리자는 전체 스레드 삭제 가능
    * 물리 삭제 대신 deletedAt 기반 soft delete 유지
2. 피드백 API
    * 특정 대화에 대한 피드백 생성
    * user_id + chat_id unique constraint로 중복 방지
    * 일반 사용자는 본인 피드백만 조회
    * 관리자는 전체 피드백 조회
3. 피드백 상태 변경 API
    * 관리자만 pending에서 resolved로 변경 가능
4. 관리자 활동 기록 API
    * 최근 24시간 기준 회원가입, 로그인, 대화 생성 수 집계
    * 이미 저장 중인 ActivityLog를 기준으로 구현 가능
5. CSV 보고서 생성 API
    * 최근 24시간의 전체 사용자 대화 목록을 CSV로 반환
    * 사용자 ID, 이메일, 이름, 질문, 답변, 생성일시 포함
6. 실제 AI Provider 연동
    * OpenAiClient 구현체 추가
    * API Key는 환경변수로만 주입
    * timeout, retry, provider error mapping 추가
7. 대외비 문서 학습 확장
    * 문서 업로드 API
    * 문서 접근 권한
    * chunking
    * embedding
    * vector search
    * 검색 결과 기반 prompt context 구성

