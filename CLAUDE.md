# CLAUDE.md

Claude Code가 이 저장소에서 작업할 때 참고할 프로젝트 컨텍스트입니다.

## 프로젝트 개요

- **이름**: AIBE7-Project2-Team01 (TalentPulse) — 재능 거래 플랫폼
- **Backend**: Spring Boot **4.0.7** (팀 공통 고정 버전, 임의로 올리지 말 것), Java 17, Gradle
- **DB**: PostgreSQL (Neon serverless, 원격)
- **Frontend**: 바닐라 HTML/CSS/JS, 빌드 도구 없음 (`python3 -m http.server 5173 -d frontend`)
- **담당 역할**: 나는 백엔드, 특히 WebSocket 기반 실시간 채팅 기능 담당. 로그인/인증은 다른 팀원 담당 (auth 도메인 건드릴 때 조율 필요).

## 브랜치 / Git 규칙

- `merge` 브랜치는 팀 합의 없이 임의로 push 금지 (보호 브랜치)
- 개인 feature 브랜치에서 작업 후 PR로 팀 합의 거쳐 병합
- `.idea/`는 gitignore 대상 — 개인 IDE 설정(Run Configuration 등)이 커밋되면 안 됨
- 커밋 메시지: `type: 한글 설명` (예: `feat: 채팅 WebSocket 기본 구조 추가`)

## 실행 방법

```bash
# Backend (working directory가 backend/ 여야 .env 인식됨 — IntelliJ Run Config 필수 확인)
cd backend
./gradlew bootRun

# Frontend
python3 -m http.server 5173 -d frontend
```

- Swagger: http://localhost:8080/swagger-ui/index.html
- `backend/.env`에 Neon DB 접속정보 필요 (`.env.sample` 참고, 실제 값은 git에 커밋 금지)

## DB 스키마

- ERD가 최근 재확정됨 — PK 컬럼명이 `user_id`, `chat_room_id`처럼 **테이블명 접두사 + `_id`** 규칙을 따름
- 최신 `schema.sql`을 DB에 반영해야 함 (아직 안 했으면 우선 처리)
- 자바 엔티티는 필드명은 관례상 `id`로 유지하고 `@Column(name = "...")`으로 DB 컬럼명만 매핑하는 방식 채택 (Spring Data JPA 파생 쿼리 깨짐 방지 목적)

## ⚠️ 현재 막혀있는 이슈 — 다른 팀원 확인 필요

`UserEntity.java`의 PK가 현재 `id`로 되어 있는데, 확정된 ERD는 `USERS.user_id`. 이 부분은 auth/user 도메인 담당 팀원이 `@Column(name = "user_id")`로 맞춰야 채팅 기능의 FK 참조가 정상 동작함. **아직 확인 안 된 상태 — 이 이슈가 해결됐는지 먼저 확인하고 이어서 작업할 것.**

## WebSocket 채팅 기능 — 진행 상황

### 완료된 것
- `build.gradle`에 `spring-boot-starter-websocket` 추가
- `domain/chat/entity/`: `ChatRoom`, `ChatMessage`, `ChatParticipant` 엔티티 (최신 ERD 컬럼명 반영)
- `domain/chat/repository/`: `ChatRoomRepository`, `ChatMessageRepository`, `ChatParticipantRepository`
- `domain/chat/dto/`: `ChatSendRequest`(클라이언트→서버), `ChatMessageResponse`(서버→클라이언트)
- `domain/chat/service/ChatService`: 메시지 저장 + DTO 변환 (트랜잭션 내에서 LAZY 프록시 문제 방지)
- `domain/chat/controller/ChatController`: `@MessageMapping("/chat.send")`
- `domain/chat/websocket/WebSocketConfig`: STOMP 설정, `/ws` 엔드포인트, `/topic`·`/app` prefix
- `domain/chat/websocket/TempUserHandshakeInterceptor` + `StompPrincipalChannelInterceptor`: **로그인 기능 완성 전까지 임시로** `?username=` 쿼리 파라미터로 사용자를 식별하는 임시 인증. **로그인 기능 완성되면 반드시 실제 Security 인증으로 교체해야 함** (컨트롤러/서비스 코드는 `Principal`만 사용하므로 이 부분만 교체하면 됨 — 이게 설계 의도)
- `SecurityConfig`에 `/ws/**` permitAll 추가

### 아직 안 한 것
- 프론트엔드 연동 (`frontend/src/chat.js` 신규 작성 필요 — SockJS + StompJS로 연결, `pages.js`의 `chatPage()` 목업 로직 교체)
- 채팅방 생성 REST API (지금은 테스트용으로 DB에 직접 INSERT 해서 사용 중)
- WebSocketStompClient 기반 통합 테스트
- 읽음 처리(`ChatParticipant.lastReadMessageId` 관련 REST API) — 우선순위 낮음

### 로컬 테스트 방법
- 브라우저 개발자도구 콘솔에서 SockJS/StompJS로 직접 연결해서 확인 (프론트 완성 전 임시 검증용)
- 여러 사용자 테스트: 브라우저 창 A(일반) + B(시크릿), 각각 `?username=userA`, `?username=userB`로 연결
- 정식 검증은 `WebSocketStompClient` 기반 `@SpringBootTest` 통합 테스트로

## 코드 컨벤션 (참고 프로젝트 tale-teller 방식 일부 채택)

- DTO는 `Request`/`Response` 접미사, `@Builder` + 정적 팩토리(`from()`) 패턴
- 엔티티를 컨트롤러 응답으로 직접 반환 금지, 항상 DTO로 변환
- 서비스는 기본 `@Transactional(readOnly = true)`, 쓰기 메서드만 개별로 `@Transactional`
- LAZY 연관관계 DTO 변환은 반드시 트랜잭션 안에서 끝낼 것
- 예외는 `CustomException` + `ErrorCode` enum 패턴 사용, `GlobalExceptionHandler`에서 일괄 처리
- 공통 `BaseEntity`(`common.entity.BaseEntity`, createdAt/updatedAt) 상속이 팀 컨벤션 — 단, 불변 데이터(예: `ChatParticipant.joinedAt`)처럼 안 맞는 경우는 예외적으로 상속 안 함

## 참고 문서

- `DEV_GUIDELINES.md` (프로젝트 루트, 이미 존재한다면) — tale-teller 프로젝트를 참고한 개발 규칙 상세본
- `SETUP.md` — 로컬 환경 세팅 시 겪었던 이슈(Gradle 인식 문제, Working directory, git reset 등) 정리
