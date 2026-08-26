# 채팅 → 거래 → 결제 테스트 가이드

채팅방 생성/목록/이력/나가기 + 거래 생성/결제/완료/취소(환불) 까지 다 붙은 상태 기준으로 작성됨.
아래 순서대로 진행하면 "게시글 → 채팅 → 거래 → 결제"까지 전체 흐름을 한 번에 검증할 수 있음.

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

## 1. 테스트 계정 준비 (2명 필요 — 1:1 채팅/거래라 최소 2명)

Swagger에서 `POST /users/signup`을 두 번 호출해서 계정 2개(A, B) 만들면 됨. 이미 만들어둔 계정이 있으면 건너뛰어도 됨.
아래부터는 **A = 요청글 작성자(의뢰인, 결제자)**, **B = 채팅 상대(전문가, 수취자)** 기준으로 서술함.

## 2. 요청글 작성 + 채팅 시작

이제 게시글 상세 화면의 "채팅 신청" 버튼이 실제로 연동돼 있어서 화면으로 바로 테스트 가능함 (Swagger로 직접 호출해도 됨, 3번 참고).

1. 계정 A로 로그인 → `#/request-new`에서 요청글 작성 (카테고리는 `GET /categories`로 확인된 것 중 아무거나)
2. 작성된 요청글 상세(`#/request/{id}`)로 이동 → 계정 B로 로그인해서 같은 요청글 상세 화면에서 **"Message Requester"(채팅 신청)** 버튼 클릭
   - 버튼 클릭 시 `startChat()` → `POST /chatrooms`가 호출되고 채팅방으로 자동 이동됨
   - 같은 글+같은 상대로 다시 눌러도 새 방이 안 생기고 기존 방으로 이동해야 함 (중복 생성 방지)

## 3. (대안) Swagger로 채팅방 직접 생성

화면 대신 API로 빠르게 세팅하고 싶을 때:

1. `POST /auth/login`으로 로그인 → 응답의 `accessToken` 복사
2. 오른쪽 위 **Authorize** 버튼 클릭 → `accessToken` 값 붙여넣기 (Bearer 없이 값만)
3. (요청글이 하나도 없으면) `POST /requests`로 하나 생성
4. `POST /chatrooms`, body: `{ "requestPostId": <3번 id>, "otherUserId": <상대방 user id> }`
   - ⚠️ 알려진 이슈: `requestPostId`/`otherUserId`가 실제로 존재하지 않는 값이면 FK 위반이 나는데, 에러 메시지가 `DUPLICATE_RESOURCE`("이미 존재하는 데이터입니다")로 잘못 뜸 — 진짜 중복이 아니라 참조 대상이 없다는 뜻이니 id들을 다시 확인할 것

## 4. 화면으로 채팅 확인

1. 계정 A로 채팅방(`#/chat/{id}`) 진입 → 메시지 입력 후 Send → 화면에 바로 말풍선으로 뜸 (WebSocket 실시간)
2. 다른 브라우저(시크릿 창)에서 계정 B로 로그인 → 같은 채팅방으로 진입 → 서로 메시지 주고받는 것 확인 (새로고침 없이 실시간 반영)

> ⚠️ 거래 생성은 **채팅 메시지가 최소 1개 이상 있어야** 가능함 (검증 규칙) — 5번으로 넘어가기 전에 이 단계에서 메시지를 최소 1개는 꼭 보내둘 것.

## 5. 채팅방 나가기 테스트

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
5. (선택) 해당 채팅방에 `trades` 레코드가 걸려 있는 상태라면, 양쪽이 다 나가도 방/메시지가 삭제되지 않고 남아있어야 함 — 8번(거래 있는 방 나가기)에서 별도로 검증

> 이 뒤 6~8번 테스트를 위해선 **아직 나가지 않은 새 채팅방**이 필요함 — 나가기 테스트는 별도 방에서 하거나, 순서를 바꿔 6~8번을 먼저 끝내고 마지막에 나가기를 테스트해도 됨.

## 6. 거래 생성 + 결제 테스트 (해피 패스)

사전 준비: 계정 A(결제자) 지갑에 잔액이 있어야 함 → `#/mypage`에서 "충전" 또는 `POST /wallet/charge` (body: `{ "amount": 100000 }`)로 미리 충전.

1. 계정 A로 채팅방(메시지 1개 이상 있는 방) 진입 → 우측 상단 **"결제하기"** 클릭 → `#/checkout?chatRoomId={id}`로 이동
2. 확인할 것:
   - [ ] 상대방 닉네임, 요청글 제목/내용/예산이 표시됨
   - [ ] "내 잔액"이 실제 지갑 잔액과 일치
   - [ ] 결제 금액 입력란에 요청글 예산(budgetMax)이 기본값으로 채워져 있음
3. 금액 확인/수정 후 **"결제 진행"** 클릭
4. 확인할 것:
   - [ ] "결제가 완료되었습니다" 메시지로 바뀜
   - [ ] A의 지갑 잔액이 결제 금액만큼 줄어듦 (`GET /wallet`로 확인)
   - [ ] `GET /trades`로 조회 시 해당 거래 `status`가 `PAID`
5. 계정 B로 같은 `#/checkout?chatRoomId={id}` 접속
   - [ ] "결제가 완료된 거래입니다" 화면이 보임 (B는 결제자가 아니므로 결제 폼 자체가 안 보여야 함)

## 7. 거래 완료 / 취소(환불) 테스트

지금은 화면에 완료/취소 버튼이 없어서 Swagger나 fetch로 직접 호출해야 함 (결제하기 화면만 이번 스코프).

**완료 (정산):**
1. `PATCH /trades/{tradeId}/complete` — **A(결제자) 토큰**으로 호출
2. 확인할 것:
   - [ ] 응답 `status`가 `COMPLETED`
   - [ ] B의 지갑 잔액이 결제 금액만큼 늘어남 (`GET /wallet`, B 토큰)
   - [ ] B 토큰으로 다시 `complete` 호출 시 403(`TRADE_ACCESS_DENIED`) — 결제자만 완료 처리 가능

**취소 (환불, 별도 거래로 테스트 — 완료된 거래는 취소 불가):**
1. 새 거래를 만들어 결제까지 진행 (6번 반복, 다른 채팅방/금액으로)
2. `PATCH /trades/{tradeId}/cancel` — A 또는 B 토큰 아무거나
3. 확인할 것:
   - [ ] 응답 `status`가 `CANCELLED`
   - [ ] A의 지갑 잔액이 결제 전 금액으로 원복됨 (환불)
   - [ ] 이미 `COMPLETED`된 거래를 취소 시도하면 400(`INVALID_TRADE_STATUS`)

## 8. 검증 가드(예외 케이스) 체크

| 케이스 | 호출 | 기대 결과 |
|---|---|---|
| 메시지 없는 채팅방에서 거래 생성 | `POST /chatrooms/{id}/trades` | 400 `CHAT_MESSAGE_NOT_FOUND` |
| 이미 진행 중(PENDING/PAID) 거래가 있는 채팅방에서 또 생성 | `POST /chatrooms/{id}/trades` | 409 `TRADE_ALREADY_IN_PROGRESS` |
| 채팅방 참가자가 아닌 사람이 거래 생성 시도 | `POST /chatrooms/{id}/trades` | 403 `CHAT_ROOM_ACCESS_DENIED` |
| 결제자가 아닌 사람이 `/pay` 호출 | `POST /trades/{id}/pay` | 403 `TRADE_ACCESS_DENIED` |
| 잔액보다 큰 금액 결제 시도 | `POST /trades/{id}/pay` | 400 `INSUFFICIENT_BALANCE` |
| talentPostId로 거래 생성 시도 | `POST /chatrooms/{id}/trades` | 400 `TALENT_TRADE_NOT_SUPPORTED` (talent 도메인 미구현으로 의도적 차단) |
| 거래 있는 채팅방에서 양쪽 다 나가기 | `DELETE /chatrooms/{id}` (양쪽) | 방/메시지가 삭제되지 않고 유지됨 (5번-5 참고) |

## 9. Swagger로 API만 따로 확인하고 싶을 때

http://localhost:8080/swagger-ui/index.html 에서 Authorize 후:
- `POST /chatrooms`, `GET /chatrooms`, `GET /chatrooms/{id}`, `DELETE /chatrooms/{id}`
- `POST /chatrooms/{chatRoomId}/trades`, `GET /trades`, `GET /trades/{id}`, `POST /trades/{id}/pay`, `PATCH /trades/{id}/complete`, `PATCH /trades/{id}/cancel`
- `GET /wallet`, `POST /wallet/charge`

> Swagger로는 REST API까지만 확인 가능. 실시간 메시지 송수신(WebSocket)은 4번처럼 화면으로 확인해야 함.

## 10. DB에 실제 반영됐는지 확인 (선택)

```sql
SELECT chat_message_id, chat_room_id, user_id, content, message_type, created_at
FROM chat_messages ORDER BY chat_message_id DESC LIMIT 20;

SELECT chat_room_id, request_post_id, talent_post_id, created_at
FROM chat_rooms ORDER BY chat_room_id DESC LIMIT 10;

-- 나가기 이후엔 나간 사람의 row가 없어야 함
SELECT chat_participant_id, chat_room_id, user_id, joined_at
FROM chat_participants WHERE chat_room_id = <테스트한 chatRoomId>;

SELECT trade_id, chat_room_id, payer_id, payee_id, amount, status, paid_at, completed_at, cancelled_at
FROM trades ORDER BY trade_id DESC LIMIT 10;

SELECT wallet_transaction_id, wallet_id, trade_id, transaction_type, amount, balance_after, created_at
FROM wallet_transactions ORDER BY wallet_transaction_id DESC LIMIT 20;
```

## 레거시: raw WebSocket 테스트 페이지

`http://localhost:8080/chat-test.html` — SockJS/StompJS로 직접 연결해보는 저수준 테스트 페이지. 프론트 화면 없이 WebSocket 자체만 빠르게 찔러보고 싶을 때 사용 (배포 전 제거 예정 — 별도 확인 항목으로 관리 중).

## 확인 체크리스트 (전체, 순서대로)

- [ ] 백엔드 정상 기동 (DB 연결 에러 없이)
- [ ] signup → login으로 JWT 발급됨
- [ ] 게시글 상세의 "채팅 신청" 버튼으로 채팅방 진입됨 (중복 생성 안 됨)
- [ ] 메시지 전송 시 상대방 화면에도 실시간으로 도착 (새로고침 없이)
- [ ] 한쪽이 나가도 반대쪽 대화 내용은 그대로 유지됨, 양쪽 다 나가면 완전 삭제됨
- [ ] 결제 페이지에 채팅방/요청글/잔액 정보가 정확히 표시됨
- [ ] 결제 진행 시 지갑 잔액이 정확히 차감되고 거래 상태가 `PAID`가 됨
- [ ] 결제자만 완료/취소 가능, 완료 시 상대방 잔액 정산, 취소(PAID 상태) 시 환불
- [ ] 8번 표의 예외 케이스가 전부 명세대로 실패함
- [ ] 거래가 걸린 채팅방은 양쪽이 나가도 삭제되지 않음
