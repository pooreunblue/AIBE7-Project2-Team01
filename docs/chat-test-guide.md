# 채팅 기능 테스트 가이드

채팅방 생성/목록/이력/나가기 REST API + WebSocket 실시간 송수신까지 붙은 상태 기준으로 작성됨.

## 0. 서버 실행

```bash
# 백엔드 (working directory가 backend/ 여야 .env 인식됨)
cd backend
./gradlew bootRun

# 프론트엔드 (새 터미널)
cd frontend
npm install   # 최초 1회
npm start
```

- 프론트: http://localhost:3000 (Express가 `/api` 요청을 백엔드로 프록시함 — `python3 -m http.server`가 아님, CLAUDE.md 구버전 안내는 무시)
- 백엔드: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui/index.html

## 1. 테스트 계정 준비 (2명 필요 — 1:1 채팅이라 최소 2명)

Swagger에서 `POST /users/signup`을 두 번 호출해서 계정 2개 만들면 됨. 이미 만들어둔 계정이 있으면 건너뛰어도 됨.

## 2. 채팅방 만들기 (게시글 상세 "채팅 신청" 버튼은 아직 미연동 — Swagger로 직접 생성)

현재 `RequestDetailPage`/`TalentDetailPage`가 아직 mock 데이터를 쓰고 있어서 실제 게시글 상세 화면에서 채팅을 시작할 수 없음. Swagger로 대신 만듦:

1. `POST /auth/login`으로 로그인 → 응답의 `accessToken` 복사
2. 오른쪽 위 **Authorize** 버튼 클릭 → `accessToken` 값 붙여넣기 (Bearer 없이 값만)
3. (요청글이 하나도 없으면) `POST /requests`로 아무 요청글이나 하나 생성 — `categoryId`는 `GET /categories`로 확인한 값 사용
4. `POST /chatrooms`, body: `{ "requestPostId": <3번에서 만든 id>, "otherUserId": <상대방 user id> }`
   - `otherUserId`는 계정 B로 `GET /users/me` 또는 로그인 응답의 `sub`(JWT payload)로 확인 가능
   - 응답의 `chatRoomId`를 기억해 둘 것
   - ⚠️ 알려진 이슈: `requestPostId`/`otherUserId`가 실제로 존재하지 않는 값이면 FK 위반이 나는데, 에러 메시지가 `DUPLICATE_RESOURCE`("이미 존재하는 데이터입니다")로 잘못 뜸 — 진짜 중복이 아니라 참조 대상이 없다는 뜻이니 id들을 다시 확인할 것

## 3. 화면으로 채팅 확인

1. 브라우저에서 `http://localhost:3000` 접속 → 계정 A로 로그인
2. 주소창 해시를 `#/chat/{2번에서 만든 chatRoomId}`로 직접 이동 (예: `http://localhost:3000/#/chat/11`)
3. 확인할 것:
   - [ ] 왼쪽 목록에 상대방 이름이 보임
   - [ ] 메시지 입력 후 Send → 화면에 바로 말풍선으로 뜸 (WebSocket 실시간)
4. 다른 브라우저(시크릿 창)에서 계정 B로 로그인 → 같은 채팅방으로 들어가서 서로 메시지 주고받는 것 확인 (실시간 반영, 새로고침 불필요)

## 4. 채팅방 나가기 테스트 (신규)

"나가기"는 카톡처럼 **나간 사람 기준으로만** 지워짐 — 상대방 화면/메시지는 그대로 남아있어야 정상.

1. 계정 A로 채팅방에 들어가서 우측 상단 **"채팅방 나가기"** 클릭 → 확인 다이얼로그에서 확인
2. 확인할 것:
   - [ ] A의 채팅방 목록에서 해당 방이 사라짐 (`#/chat`로 이동됨)
   - [ ] **B는 계속 그 방이 보이고, 이전 메시지도 그대로 남아있음**
   - [ ] B 쪽 방 목록에 상대방 이름 대신 **"(상대방이 나감)"**으로 표시됨
3. 이어서 계정 B도 같은 방에서 "채팅방 나가기" 클릭
4. 확인할 것:
   - [ ] B의 목록에서도 방이 사라짐 (양쪽 다 나감)
   - [ ] 이 시점부터 그 `chatRoomId`로 `GET /chatrooms/{id}` (메시지 조회)를 호출하면 403(`CHAT_ROOM_ACCESS_DENIED`)이 떠야 함 — 방+메시지가 DB에서 완전히 삭제된 것
5. (선택, 결제/거래 연동 확인용) 만약 해당 채팅방에 `trades` 레코드가 걸려 있는 상태라면, 양쪽이 다 나가도 방/메시지가 삭제되지 않고 남아있어야 함 — 거래 증빙 보존 목적

## 5. Swagger로 API만 따로 확인하고 싶을 때

http://localhost:8080/swagger-ui/index.html 에서 Authorize 후:
- `POST /chatrooms` — 방 생성/재사용
- `GET /chatrooms` — 내 방 목록
- `GET /chatrooms/{id}` — 메시지 이력
- `DELETE /chatrooms/{id}` — 나가기 (204 응답, 본문 없음)

> Swagger로는 REST API까지만 확인 가능. 실시간 메시지 송수신(WebSocket)은 3번처럼 화면으로 확인해야 함.

## 6. DB에 실제 반영됐는지 확인 (선택)

```sql
SELECT chat_message_id, chat_room_id, user_id, content, message_type, created_at
FROM chat_messages
ORDER BY chat_message_id DESC
LIMIT 20;

SELECT chat_room_id, request_post_id, talent_post_id, created_at
FROM chat_rooms
ORDER BY chat_room_id DESC
LIMIT 10;

-- 나가기 이후엔 나간 사람의 row가 없어야 함
SELECT chat_participant_id, chat_room_id, user_id, joined_at
FROM chat_participants
WHERE chat_room_id = <테스트한 chatRoomId>;
```

## 레거시: raw WebSocket 테스트 페이지

`http://localhost:8080/chat-test.html` — SockJS/StompJS로 직접 연결해보는 저수준 테스트 페이지. 프론트 화면 없이 WebSocket 자체만 빠르게 찔러보고 싶을 때 사용 (배포 전 제거 예정 — 별도 확인 항목으로 관리 중).

## 확인 체크리스트 (전체)

- [ ] 백엔드 정상 기동 (DB 연결 에러 없이)
- [ ] signup → login으로 JWT 발급됨
- [ ] 같은 요청글+상대방 조합으로 방을 두 번 만들어도 중복 생성 안 됨 (`POST /chatrooms` 재호출 시 기존 방 반환)
- [ ] 메시지 전송 시 상대방 화면에도 실시간으로 도착 (새로고침 없이)
- [ ] 한쪽이 나가도 반대쪽 대화 내용은 그대로 유지됨
- [ ] 양쪽 다 나가면 방+메시지가 DB에서 완전히 삭제됨 (단, 연결된 거래가 있으면 보존됨)
- [ ] `chat_messages`/`chat_rooms` 테이블에 내용이 깨지지 않고 저장됨
