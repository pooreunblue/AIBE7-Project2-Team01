# 채팅(WebSocket) 로컬 테스트 가이드

## 테스트용 채팅방 만들기
#### **복붙하여 사용하면 됩니다!
```sql
INSERT INTO chat_rooms (request_post_id, talent_post_id) VALUES (NULL, NULL);
SELECT chat_room_id FROM chat_rooms ORDER BY chat_room_id DESC LIMIT 1;
```
#### **생성된 채팅방 번호를 기억해 둡니다

## 테스트 페이지

```
http://localhost:8080/chat-test.html
```
#### **맨 좌측에 생성한 채팅방 번호를 입력하면 됩니다
## DB에 실제 저장됐는지 확인

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
