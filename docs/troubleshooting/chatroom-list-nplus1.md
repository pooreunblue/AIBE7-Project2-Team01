# 채팅방 목록 조회 N+1 트러블슈팅

## 개요

`GET /chatrooms`(내 채팅방 목록)에서 채팅방 수에 비례해 쿼리가 늘어나는 N+1 문제를 제거했다. 코드리뷰가 지적한 "게시글 제목 개별 조회" 외에도, 같은 메서드 안에 LAZY 연관관계 때문에 발생하던 N+1이 2개 더 있어 함께 정리했다. 결과적으로 방 개수와 무관하게 고정 쿼리 수(4개)로 동작하며, 쿼리 횟수를 검증하는 단위 테스트로 회귀를 막았다.

## S: Situation | 상황

### 코드리뷰 지적 — 게시글 제목 개별 조회

`ChatService.getMyRooms()`가 채팅방마다 `resolvePostTitle(room)`을 호출하고, 방이 연결된 게시글(재능글/의뢰글)의 제목을 건별로 조회했다.

```java
return myParticipations.stream()
        .map(ChatParticipant::getChatRoom)
        .map(room -> ChatRoomResponse.from(room, otherUserByRoomId.get(room.getId()), resolvePostTitle(room)))
        .toList();

private String resolvePostTitle(ChatRoom room) {
    if (room.getTalentPostId() != null) {
        return talentPostRepository.findById(room.getTalentPostId()).map(TalentPostEntity::getTitle).orElse(null);
    }
    if (room.getRequestPostId() != null) {
        return requestPostRepository.findById(room.getRequestPostId()).map(RequestPostEntity::getTitle).orElse(null);
    }
    return null;
}
```

```text
채팅방 30개
→ 채팅방 목록 조회 1회
→ 게시글 제목 조회 최대 30회
```

### 리뷰가 지적하지 않은 N+1이 2개 더 있었음

`getMyRooms()` 전체를 SQL 로그 관점으로 다시 읽으니, `ChatParticipant`의 LAZY 연관관계가 DTO 변환 시점에 초기화되면서 발생하는 N+1이 두 곳 더 있었다.

```java
@ManyToOne(fetch = FetchType.LAZY)
private ChatRoom chatRoom;   // (B) ChatRoomResponse.from()에서 getCreatedAt() 등 접근 시 초기화

@ManyToOne(fetch = FetchType.LAZY)
private UserEntity user;      // (C) from()에서 상대방 getNickname() 접근 시 초기화
```

| # | 위치 | 원인 | 방 N개일 때 |
| --- | --- | --- | --- |
| A | `resolvePostTitle(room)` | 방마다 `talent/requestPostRepository.findById` | N회 (리뷰 지적) |
| B | `.map(ChatParticipant::getChatRoom)` | `ChatParticipant.chatRoom`(LAZY) 프록시 초기화 | N회 (미지적) |
| C | 상대방 `UserEntity` | `ChatParticipant.user`(LAZY) 프록시 초기화 | N회 (미지적) |

→ 방 30개 기준 `1 + 1 + 30 × 3 ≈ 92` 쿼리

### N+1의 정의를 넓게 봐야 했던 지점

N+1은 명시적인 반복 조회(`findById` 루프)만이 아니라, **LAZY 프록시를 나중에 접근하는 순간**에도 발생한다. 리뷰는 전자만 지적했지만, 후자를 놓치면 A만 고쳐도 방 30개에 60여 쿼리가 남는다.

## T: Task | 과제

1. 리뷰 지적사항(게시글 제목 N+1)을 반영한다.
2. 같은 메서드의 나머지 N+1(B, C)도 함께 제거한다.
3. 채팅방 개수와 무관하게 쿼리 수가 일정하도록 만든다.
4. 성능 개선이 이후 리팩터링으로 되돌아가지 않도록 회귀 테스트로 고정한다.
5. 기존 `ChatRoom`의 도메인 분리 설계(게시글 FK를 연관관계가 아닌 UUID 값으로 보유)는 유지한다.

## A: Action | 행동

### A — 게시글 제목 일괄 조회 (`findByIdIn`)

`ChatRoom`은 `talentPostId`/`requestPostId`를 **JPA 연관관계가 아닌 UUID 값**으로 보유한다(도메인 분리 설계). 연관이 없으니 `join fetch`가 불가능하므로, ID를 종류별로 모아 `findByIdIn`으로 한 번에 조회하고 `Map`으로 매핑했다.

```java
private Map<UUID, String> resolvePostTitles(List<ChatRoom> rooms) {
    Set<UUID> talentPostIds = rooms.stream()
            .map(ChatRoom::getTalentPostId).filter(Objects::nonNull).collect(Collectors.toSet());
    Set<UUID> requestPostIds = rooms.stream()
            .map(ChatRoom::getRequestPostId).filter(Objects::nonNull).collect(Collectors.toSet());

    Map<UUID, String> talentTitles = talentPostIds.isEmpty() ? Map.of()
            : talentPostRepository.findByIdIn(talentPostIds).stream()
                    .collect(Collectors.toMap(TalentPostEntity::getId, TalentPostEntity::getTitle));
    Map<UUID, String> requestTitles = requestPostIds.isEmpty() ? Map.of()
            : requestPostRepository.findByIdIn(requestPostIds).stream()
                    .collect(Collectors.toMap(RequestPostEntity::getId, RequestPostEntity::getTitle));

    Map<UUID, String> titleByRoomId = new HashMap<>();
    for (ChatRoom room : rooms) {
        String title = null;
        if (room.getTalentPostId() != null) {
            title = talentTitles.get(room.getTalentPostId());
        } else if (room.getRequestPostId() != null) {
            title = requestTitles.get(room.getRequestPostId());
        }
        titleByRoomId.put(room.getId(), title);
    }
    return titleByRoomId;
}
```

`createRoom()`의 단건 호출은 방 1개당 1쿼리라 무해하므로 기존 `resolvePostTitle(ChatRoom)`을 그대로 뒀다.

### B, C — fetch join 쿼리

`ChatParticipantRepository`의 파생 쿼리(`findByUserId`, `findByChatRoomIdInAndUserIdNot`)를 `@Query` + `join fetch`로 교체했다. 두 메서드는 `getMyRooms()`에서만 쓰였으므로 대체가 안전했다.

```java
@Query("select p from ChatParticipant p join fetch p.chatRoom where p.user.id = :userId")
List<ChatParticipant> findByUserIdWithRoom(@Param("userId") UUID userId);

@Query("""
        select p from ChatParticipant p
        join fetch p.user
        join fetch p.chatRoom
        where p.chatRoom.id in :chatRoomIds and p.user.id <> :userId
        """)
List<ChatParticipant> findOthersWithUserByChatRoomIdIn(
        @Param("chatRoomIds") Collection<UUID> chatRoomIds,
        @Param("userId") UUID userId
);
```

`p.user`, `p.chatRoom` 둘 다 `@ManyToOne`(ToOne)이라 fetch join을 2개 걸어도 결과 row 수가 늘지 않는다(카테시안 폭발 없음, `distinct` 불필요). 폭발은 컬렉션(`@OneToMany`)을 2개 이상 fetch join할 때 발생한다.

### 빈 목록 가드

참여한 방이 없으면 `List.of()`로 조기 반환해, 빈 `IN ()` 쿼리와 불필요한 게시글 조회를 막았다.

```java
List<ChatParticipant> myParticipations = chatParticipantRepository.findByUserIdWithRoom(user.getId());
if (myParticipations.isEmpty()) {
    return List.of();
}
```

### 회귀 방지 테스트

팀 컨벤션(Mockito 단위 테스트)에 맞춰 `ChatServiceTest`를 작성하고, **쿼리 호출 횟수 자체를 검증**했다.

```java
// 게시글 종류별로 딱 1회씩만, 방마다 개별 조회(findById) 없음
verify(talentPostRepository, times(1)).findByIdIn(anyCollection());
verify(requestPostRepository, times(1)).findByIdIn(anyCollection());
verify(talentPostRepository, never()).findById(any());
verify(requestPostRepository, never()).findById(any());
```

이후 누군가 다시 루프 조회를 넣으면 이 테스트가 깨진다.

## R: Result | 결과

- 쿼리 수: **방 N개 기준 `1 + 3N` → 고정 4개** (참여자+방 / 상대방+유저 / 재능글 제목 / 요청글 제목). 방 개수와 무관.
- 전체 테스트 통과(기존 + 신규 2), 컴파일·앱 기동(JPQL 검증) 정상.
- 리뷰 지적(A)을 반영하면서 미지적 항목(B·C)까지 같은 PR에서 정리.
- 새로 추가된 `CONVENTION.md`에 맞춰 테스트 메서드명(언더스코어→camelCase)·변수명(축약어 지양)도 별도 커밋으로 정리.

## 배운 점 / 재발 방지

- **N+1은 명시적 반복 조회만이 아니라 LAZY 프록시 접근에서도 발생한다.** DTO 변환 코드에서 `entity.getXxx()`를 호출하는 순간 쿼리가 나갈 수 있음을 항상 의심할 것.
- 리스트 응답 메서드는 "결과 크기에 비례해 쿼리가 늘어나는 지점"(제목·작성자·썸네일 등 부가 정보 조회)을 리뷰 체크리스트에 넣는다.
- 해결책 우선순위: (1) JPA 연관이면 `join fetch`, (2) 연관이 아닌 FK 값이나 다른 도메인이면 ID를 모아 `findByIdIn` 일괄 조회 후 `Map` 조회.
- `IN` 절에 빈 컬렉션이 들어가지 않도록 가드.
- 성능 개선은 **회귀 테스트로 못박는다.** 쿼리 횟수를 `verify(times/never)`로 검증하면 이후 재발 시 CI에서 걸린다.

## 관련

- 브랜치: `feature/chat-nplus1-fix`
- 커밋: `refactor: 채팅방 목록 조회 N+1 제거 (코드리뷰 반영)`, `style: ChatServiceTest 네이밍을 코드 컨벤션에 맞춰 정리`
- 변경 파일: `ChatService.java`, `ChatParticipantRepository.java`, `ChatServiceTest.java`
