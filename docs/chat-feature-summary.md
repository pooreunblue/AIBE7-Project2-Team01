# 채팅(WebSocket) 기능 정리

`feature/chat` 브랜치에서 구현한 실시간 채팅 MVP에 대한 파일 설명, 구조, 예상 면접 질문 정리.

## 1. 새로 추가/수정된 파일

### 엔티티 (`domain/chat/entity/`)

| 파일 | 설명 |
| --- | --- |
| `ChatRoom.java` | 채팅방. `chat_room_id` PK, `request_post_id`/`talent_post_id`를 FK 값(Long)으로만 보유. `createdAt`/`updatedAt` 둘 다 있어 공통 `BaseEntity` 상속 |
| `ChatParticipant.java` | 채팅방 참여자. `chatRoom`/`user`와 `@ManyToOne` 연관관계, `joinedAt` 하나만 있어 `BaseEntity` 상속 안 함(불변 데이터) |
| `ChatMessage.java` | 채팅 메시지. `chatRoom`/`sender`(UserEntity) 연관관계, `content`, `messageType`(TEXT/IMAGE/SYSTEM enum), `createdAt`만 존재 |

### 리포지토리 (`domain/chat/repository/`)

| 파일 | 설명 |
| --- | --- |
| `ChatRoomRepository.java` | `JpaRepository<ChatRoom, Long>` 기본 CRUD만 |
| `ChatMessageRepository.java` | 〃 |
| `ChatParticipantRepository.java` | 〃 |

### DTO (`domain/chat/dto/`)

| 파일 | 설명 |
| --- | --- |
| `ChatSendRequest.java` | 클라이언트 → 서버. `chatRoomId`, `content`, `messageType` |
| `ChatMessageResponse.java` | 서버 → 클라이언트. `from(ChatMessage)` 정적 팩토리로 엔티티를 트랜잭션 안에서 DTO로 변환(LAZY 프록시 문제 방지) |

### 서비스/컨트롤러

| 파일 | 설명 |
| --- | --- |
| `ChatService.java` | 메시지 저장 담당. `chatRoomRepository`/`userRepository`로 존재 확인 후 `ChatMessage` 저장, `ChatMessageResponse`로 변환해 반환 |
| `ChatController.java` | `@MessageMapping("/chat.send")` — STOMP로 들어온 메시지를 `ChatService`에 위임하고, 결과를 `/topic/chat-rooms/{roomId}`로 브로드캐스트 |

### WebSocket 설정 (`domain/chat/websocket/`)

| 파일 | 설명 |
| --- | --- |
| `WebSocketConfig.java` | STOMP 엔드포인트(`/ws`, SockJS) 등록, 메시지 브로커(`/topic` 구독용, `/app` 발행용) 설정, 인증 인터셉터 등록 |
| `StompAuthChannelInterceptor.java` | STOMP `CONNECT` 프레임의 `Authorization` 헤더에서 JWT를 꺼내 검증하고, 성공하면 이메일을 `Principal`로 세션에 심음. 실패 시 연결 자체를 거부 |

### 기타

| 파일 | 설명 |
| --- | --- |
| `docs/schema.sql` | ERD(mermaid) 기준 PostgreSQL DDL 전체 (13개 테이블) |
| `backend/src/main/resources/static/chat-test.html` | 프론트엔드 완성 전, 백엔드가 직접 서빙하는 수동 테스트 페이지. 로그인 → JWT 발급 → STOMP 연결 → 메시지 송수신을 브라우저에서 바로 확인 가능 |
| `docs/chat-test-guide.md` | 위 테스트 페이지 사용법을 팀원 공유용으로 정리한 문서 |

### 기존 파일에 추가된 변경

- `build.gradle`: `spring-boot-starter-websocket` 의존성 추가
- `SecurityConfig.java`: `/ws/**`, `/chat-test.html`을 `permitAll`에 추가
- `ErrorCode.java`: `CHAT_ROOM_NOT_FOUND` 추가

## 2. 전체 구조

### 메시지가 오가는 흐름

```
[클라이언트]
   │ 1. SockJS로 /ws 접속 + STOMP CONNECT (Authorization: Bearer <JWT>)
   ▼
[StompAuthChannelInterceptor]  JWT 검증 → Principal(email) 세션에 설정
   │
   │ 2. SUBSCRIBE /topic/chat-rooms/{roomId}
   │ 3. SEND /app/chat.send  { chatRoomId, content, messageType }
   ▼
[ChatController.sendMessage]  Principal(email)을 그대로 서비스에 전달
   ▼
[ChatService.sendMessage]  (@Transactional)
   - chatRoomRepository.findById → 없으면 CHAT_ROOM_NOT_FOUND
   - userRepository.findByEmail → 없으면 USER_NOT_FOUND
   - ChatMessage 저장
   - ChatMessageResponse.from(saved) 로 DTO 변환 (트랜잭션 안에서 끝냄)
   ▼
[ChatController]  트랜잭션 커밋 후 반환된 DTO를
   messagingTemplate.convertAndSend("/topic/chat-rooms/{roomId}", dto)
   ▼
[SimpleBroker]  해당 topic을 구독 중인 모든 세션에 브로드캐스트
```

### 계층 구조

- **Entity**: JPA 매핑, DB 컬럼명은 ERD 기준(`@Column(name=...)`)으로 명시 매핑, 자바 필드명은 관례(`id`) 유지
- **Repository**: Spring Data JPA, 파생 쿼리 최소화(현재는 CRUD만 필요)
- **DTO**: record 기반, `Request`/`Response` 접미사, 응답은 정적 팩토리(`from()`)
- **Service**: 클래스 레벨 `@Transactional(readOnly = true)`, 쓰기 메서드만 개별 `@Transactional`
- **Controller**: REST가 아닌 `@MessageMapping` 기반 STOMP 컨트롤러 (`@Controller`, `@RestController` 아님)

### 인증 구조 (HTTP vs WebSocket)

REST API는 `JwtFilter`(서블릿 필터, `OncePerRequestFilter`)가 매 요청마다 `Authorization` 헤더를 검사해 `SecurityContextHolder`에 인증 정보를 채운다. 반면 WebSocket은 **연결을 한 번 맺으면 그 위로 여러 STOMP 프레임(SUBSCRIBE, SEND 등)이 오가는 구조**라 서블릿 필터 방식이 그대로 적용되지 않는다. 그래서 STOMP 전용 `ChannelInterceptor`(`StompAuthChannelInterceptor`)를 만들어 **최초 CONNECT 프레임에서만** JWT를 검증하고, 그 결과(Principal)를 STOMP 세션에 심어서 이후 메시지들에서 `Principal`로 재사용하는 구조를 취했다.

### DB 설계에서 눈여겨볼 점

- `ChatRoom.requestPostId`/`talentPostId`: 대상 도메인(`request`, `talent`)이 아직 엔티티로 구현되지 않아서 `@ManyToOne` 연관관계가 아니라 **FK 값만 Long으로 보유**. 나중에 해당 도메인이 완성되면 연관관계로 전환 가능
- `ChatMessage`/`ChatParticipant`는 `updatedAt`이 없는(불변) 데이터라 공통 `BaseEntity`를 상속하지 않고 `@CreatedDate` 필드를 직접 선언 — 팀 컨벤션에 명시된 예외 케이스

## 3. 예상 면접 질문

### WebSocket / STOMP

1. **WebSocket과 HTTP 폴링/SSE의 차이는? 이 프로젝트에서 왜 WebSocket(+STOMP)을 선택했나?**
   - 채팅은 양방향 실시간 통신이 필요해 단방향인 SSE로는 부족하고, 매번 요청을 보내는 폴링은 지연·서버 부하 문제가 있음
2. **STOMP는 왜 썼나? 순수 WebSocket 프로토콜만으로는 안 되나?**
   - 순수 WebSocket은 메시지 포맷/라우팅을 직접 구현해야 함. STOMP는 `SEND`/`SUBSCRIBE`/`CONNECT` 같은 프레임과 destination 개념을 제공해 pub/sub 라우팅(`/topic`, `/app`)을 Spring이 대신 처리해줌
3. **`/app`과 `/topic` prefix의 역할 차이는?**
   - `/app`(`setApplicationDestinationPrefixes`)은 클라이언트가 서버 메서드(`@MessageMapping`)를 호출할 때, `/topic`(`enableSimpleBroker`)은 서버가 구독자들에게 브로드캐스트할 때 쓰는 경로
4. **SockJS는 왜 같이 썼나?**
   - 프록시/방화벽 등으로 WebSocket이 막힌 환경을 위한 fallback(long polling 등)을 자동 처리

### 인증/보안

5. **HTTP는 `JwtFilter`로 인증하는데, WebSocket에는 왜 별도의 `StompAuthChannelInterceptor`가 필요했나?**
   - WebSocket은 한 번 연결되면 그 위로 여러 STOMP 프레임이 오가는 지속 연결이라, 매 요청마다 도는 서블릿 필터 체인과는 인증 시점/방식이 다름. STOMP 메시지 채널에 개입하는 `ChannelInterceptor`가 필요
6. **JWT를 쿼리 파라미터가 아니라 STOMP CONNECT의 헤더로 보낸 이유는?**
   - 쿼리 파라미터는 URL에 노출되어 로그/브라우저 히스토리에 남을 위험이 있음. STOMP native header는 프레임 본문처럼 취급되어 상대적으로 노출 위험이 적음
7. **Access token이 연결 도중 만료되면 어떻게 되나? (현재 구현의 한계)**
   - 현재는 CONNECT 시점에만 검증하므로, 연결된 이후 토큰이 만료돼도 그 세션은 끊기지 않음. 실제 서비스라면 주기적 재인증이나 만료 시 서버가 세션을 강제 종료하는 로직이 필요
8. **처음에는 `?username=` 쿼리 파라미터 기반 임시 인증을 썼는데, 왜 그렇게 설계했고 나중에 어떻게 교체했나?**
   - 로그인 기능이 아직 없을 때 채팅 기능을 먼저 검증하기 위한 임시 조치. 컨트롤러/서비스는 `Principal`이라는 추상화만 바라보게 설계해서, 실제 로그인이 완성된 뒤 인터셉터 구현체만 교체하고 상위 계층은 건드리지 않았음(의존성 역전 활용 사례로 설명 가능)

### 데이터/트랜잭션

9. **LAZY 연관관계를 DTO로 변환할 때 왜 트랜잭션 안에서 끝내야 하나?**
   - 트랜잭션(영속성 컨텍스트)이 끝난 뒤 LAZY 필드에 접근하면 `LazyInitializationException` 발생. `ChatMessageResponse.from()`을 서비스 메서드(트랜잭션 범위) 안에서 호출해 이를 방지
10. **`ChatService`에 `@Transactional(readOnly = true)`를 클래스 레벨에, 쓰기 메서드에만 `@Transactional`을 따로 붙인 이유는?**
    - 기본을 읽기 전용으로 최적화(dirty checking 생략 등)하고, 실제 쓰기가 필요한 메서드만 명시적으로 쓰기 트랜잭션으로 오버라이드하는 패턴
11. **메시지 저장과 브로드캐스트 사이에 트랜잭션 커밋 순서가 왜 중요한가?**
    - 커밋 전에 브로드캐스트하면, 수신자가 메시지를 보고 바로 조회했을 때 아직 DB에 반영 안 된 상태(즉시 일관성 깨짐)일 수 있음. 현재 구조는 `ChatService.sendMessage()`가 반환(=트랜잭션 커밋)된 뒤 컨트롤러가 브로드캐스트하므로 순서상 안전
12. **`ChatRoom`이 `request_post_id`/`talent_post_id`를 연관관계가 아니라 Long으로만 갖는 게 문제되지 않나?**
    - 참조 무결성(FK 제약)은 DB 레벨에서 보장되지만, JPA cascade나 fetch join 등 ORM 차원의 이점은 없음. 대상 도메인이 완성되면 연관관계로 리팩터링할 여지를 남겨둔 임시 설계

### 확장성

13. **`enableSimpleBroker`(인메모리 브로커)의 한계는? 서버를 여러 대로 스케일하면 어떻게 되나?**
    - 인메모리라 인스턴스 간 상태 공유가 안 됨. 인스턴스 A에 연결된 유저와 B에 연결된 유저는 서로 메시지를 못 받음. 해결하려면 외부 브로커(RabbitMQ `enableStompBrokerRelay`) 또는 Redis pub/sub으로 브로드캐스트를 중계해야 함
14. **채팅방 생성/목록조회 API, 읽음 처리 등은 왜 아직 없나? 어떤 순서로 만들 계획인가?**
    - 지금은 메시지 송수신 핵심 플로우(가장 리스크 큰 부분: WebSocket 인증)를 먼저 검증. REST API들은 그 위에 얹는 상대적으로 단순한 작업이라 우선순위를 낮춤

### 테스트

15. **프론트엔드 없이 이 기능을 어떻게 검증했나?**
    - 백엔드가 직접 서빙하는 정적 HTML 테스트 페이지(SockJS+StompJS)로 두 사용자를 시뮬레이션해 로그인→연결→송수신까지 수동 검증. 정식 자동화 테스트는 `WebSocketStompClient` 기반 `@SpringBootTest` 통합 테스트로 대체 가능
16. **자동화된 통합 테스트를 작성한다면 무엇을 검증해야 하나?**
    - 유효하지 않은/만료된 JWT로 CONNECT 시 연결 거부, 정상 토큰으로 SEND 시 구독자에게 브로드캐스트되는지, 존재하지 않는 채팅방/유저에 대한 예외 처리(`CHAT_ROOM_NOT_FOUND`, `USER_NOT_FOUND`) 등
