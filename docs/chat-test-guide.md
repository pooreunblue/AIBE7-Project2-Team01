# 채팅(WebSocket) 로컬 테스트 가이드

`feature/chat` 브랜치에 있는 실시간 채팅 기능을 로컬에서 확인하는 방법입니다.
프론트엔드가 아직 없어서, 백엔드가 직접 서빙하는 임시 테스트 페이지(`chat-test.html`)로 확인합니다.

## 0. 브랜치 받기

```bash
git fetch origin feature/chat:feature/chat
git checkout feature/chat
```

## 1. .env에 Redis 설정 추가

기존 `.env`(DB, JWT_SECRET)에 아래 4개를 추가해야 합니다. 팀 공용 Redis 접속정보는 별도로 공유받으세요.

```
REDIS_HOST=
REDIS_PORT=
REDIS_USERNAME=
REDIS_PASSWORD=
```

## 2. 백엔드 실행

```bash
cd backend
./gradlew bootRun
```

`Started BackendApplication` 로그가 뜨면 정상 기동된 것입니다. (http://localhost:8080/health 접속 시 `Server is running` 확인)

## 3. 테스트 계정 2개 만들기 (Swagger)

`http://localhost:8080/swagger-ui/index.html` 접속.

1. `user-controller`의 `POST /users/signup` 펼치기 → **Try it out**
2. Request body를 아래처럼 채우고 **Execute** (email/nickname은 겹치지만 않으면 자유)

```json
{
  "email": "testA@example.com",
  "password": "Test1234!",
  "nickname": "testerA"
}
```

3. 같은 방식으로 두 번째 계정도 생성 (`testB@example.com` / `testerB`)

## 4. 로그인 (선택 — 6번 테스트 페이지에서 바로 해도 됨)

`chat-test.html`에 로그인 기능이 내장돼 있어서 이 단계는 건너뛰어도 됩니다.
Swagger에서 미리 토큰을 받아두고 싶으면 `auth-controller`의 `POST /auth/login`에 3번 계정 정보로 **Try it out** 하면 됩니다.

## 5. 테스트용 채팅방 만들기

채팅방 생성 REST API가 아직 없어서 DB에 직접 한 줄 넣어야 합니다. (DBeaver, Neon 콘솔 SQL 에디터 등 편한 걸로)

```sql
INSERT INTO chat_rooms (request_post_id, talent_post_id) VALUES (NULL, NULL);
SELECT chat_room_id FROM chat_rooms ORDER BY chat_room_id DESC LIMIT 1;
```

나온 `chat_room_id` 값을 기억해둡니다 (보통 처음이면 1).

## 6. 테스트 페이지 열기

브라우저에서 `http://localhost:8080/chat-test.html` 접속.

1. 상단 "채팅방 ID"에 5번에서 확인한 id 입력 (기본값 1)
2. User A 칸에 email/password 입력하고 **Login** → 토큰 입력란이 자동으로 채워짐 (Swagger에서 미리 받은 토큰이 있으면 직접 붙여넣어도 됨)
3. User B 칸도 마찬가지로 Login
4. 양쪽 다 **Connect**
5. 양쪽 로그에 `>>> CONNECTED`, `user-name:` 뒤에 본인 이메일이 뜨는지 확인 (JWT 인증이 제대로 됐다는 뜻)
6. A의 메시지 입력창에 아무 텍스트나 쓰고 Send → **A, B 양쪽 로그에 `<<< RECEIVED`** 로 같은 메시지가 뜨는지 확인
7. B에서도 반대로 보내서 확인

## 7. (선택) DB에 실제 저장됐는지 확인

```sql
SELECT chat_message_id, chat_room_id, user_id, content, message_type, created_at
FROM chat_messages
ORDER BY chat_message_id;
```

## 확인 체크리스트

- [ ] 백엔드 정상 기동 (Redis 연결 에러 없이)
- [ ] signup → login으로 JWT 발급됨
- [ ] `chat-test.html`에서 Connect 시 `user-name`이 로그인한 이메일과 일치
- [ ] 메시지 전송 시 상대방 쪽에도 실시간으로 도착
- [ ] `chat_messages` 테이블에 내용이 깨지지 않고 저장됨

문제가 있으면 백엔드 콘솔 로그(에러 스택트레이스)와 함께 알려주세요.
