# API

## 인증

회원 가입과 로그인 요청을 제외한 모든 요청은 `Authorization` 헤더에 JWT Bearer 토큰이 필요합니다.
JWT 시크릿과 토큰 TTL은 실행 환경변수 `JWT_SECRET`, `JWT_TOKEN_TTL_SECONDS`로 설정합니다.

```text
Authorization: Bearer <accessToken>
```

토큰이 없거나 유효하지 않으면 다음 형식의 `401 Unauthorized` 응답을 반환합니다.

```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Authentication token is required."
}
```

## 회원 가입

```http
POST /api/auth/signup
Content-Type: application/json
```

요청 예시:

```json
{
  "email": "member@example.com",
  "password": "password123",
  "name": "홍길동"
}
```

응답 예시:

```json
{
  "userId": "6f32a3e2-f542-4e48-a364-a87d407a7fc5",
  "email": "member@example.com",
  "name": "홍길동",
  "role": "member",
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer"
}
```

이미 가입된 이메일이면 `409 Conflict`를 반환합니다.

## 로그인

```http
POST /api/auth/login
Content-Type: application/json
```

요청 예시:

```json
{
  "email": "member@example.com",
  "password": "password123"
}
```

응답 예시는 회원 가입과 동일합니다. 이메일 또는 비밀번호가 올바르지 않으면 `401 Unauthorized`를 반환합니다.

## 대화 생성

```http
POST /api/chats
Authorization: Bearer <accessToken>
Content-Type: application/json
```

요청 예시:

```json
{
  "question": "오늘 날씨에 어울리는 점심 메뉴를 추천해줘",
  "isStreaming": false,
  "model": "gpt-4o-mini"
}
```

응답 예시:

```json
{
  "threadId": "6f32a3e2-f542-4e48-a364-a87d407a7fc5",
  "chatId": "507f4049-9f13-4c6e-b89a-5dff90dbdd55",
  "question": "오늘 날씨에 어울리는 점심 메뉴를 추천해줘",
  "answer": "FAKE_AI_RESPONSE[prompt=...,input=...]",
  "provider": "fake",
  "model": "gpt-4o-mini",
  "createdAt": "2026-06-30T07:23:45.123Z"
}
```

스레드는 사용자별로 유지됩니다. 해당 사용자의 첫 질문이거나 마지막 질문 후 30분이 지난 뒤 질문하면 새 스레드를 생성하고, 마지막 질문 후 30분 이내 질문하면 기존 스레드에 대화를 추가합니다. AI 요청에는 같은 스레드의 지난 대화 목록이 함께 전달됩니다.

`isStreaming`이 `true`이면 저장은 동일하게 수행하고 응답 본문은 `text/event-stream`으로 반환합니다.

## 대화 목록 조회

```http
GET /api/chat-threads?page=0&size=20&direction=desc
Authorization: Bearer <accessToken>
```

요청 파라미터:

- `page`: 조회할 페이지 번호이며 기본값은 `0`입니다.
- `size`: 페이지 크기이며 기본값은 `20`입니다. 서버는 `1`부터 `100` 사이 값으로 보정합니다.
- `direction`: 스레드 생성일시 정렬 방향이며 `asc`, `desc`를 지원합니다.

응답 예시:

```json
{
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1,
  "threads": [
    {
      "threadId": "6f32a3e2-f542-4e48-a364-a87d407a7fc5",
      "userId": "3e1a9b60-00f5-47d2-ae8f-7f703c13dd32",
      "userEmail": "member@example.com",
      "userName": "홍길동",
      "createdAt": "2026-06-30T07:23:45.123Z",
      "lastChattedAt": "2026-06-30T07:25:12.456Z",
      "chats": [
        {
          "chatId": "507f4049-9f13-4c6e-b89a-5dff90dbdd55",
          "question": "오늘 날씨에 어울리는 점심 메뉴를 추천해줘",
          "answer": "FAKE_AI_RESPONSE[prompt=...,input=...]",
          "provider": "fake",
          "model": "gpt-4o-mini",
          "createdAt": "2026-06-30T07:23:45.123Z"
        }
      ]
    }
  ]
}
```

일반 회원은 본인이 생성한 스레드와 대화만 조회할 수 있습니다. 관리자는 삭제되지 않은 모든 사용자의 스레드와 대화를 조회할 수 있습니다.

## 헬스 체크

```http
GET /api/health
Authorization: Bearer <accessToken>
```

응답 예시:

```json
{
  "status": "ok",
  "database": "up"
}
```
