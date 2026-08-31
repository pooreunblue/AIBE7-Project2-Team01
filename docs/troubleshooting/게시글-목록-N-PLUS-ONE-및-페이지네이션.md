# 게시글 목록 N+1 및 무제한 조회 개선 트러블슈팅

## 개요

재능글과 요청글 목록 API에서 전체 데이터를 한 번에 조회하고 연관 엔티티를 지연 로딩하면서 발생할 수 있는 N+1 쿼리와 메모리 부담을 개선했다. 목록·검색 조회를 `Page` 기반으로 통일하고, 응답에 필요한 작성자·카테고리·포트폴리오를 `EntityGraph`로 함께 조회하도록 변경했다.

## S: Situation | 상황

### 목록 API가 모든 게시글을 한 번에 조회함

기존 목록 서비스는 `findAll()` 결과를 `List`로 반환했다.

```text
GET /talents  → talent_posts 전체 조회
GET /requests → request_posts 전체 조회
```

게시글이 증가하면 다음 비용이 요청 한 번에 함께 커진다.

- 애플리케이션 메모리에 전체 Entity 적재
- 전체 Entity의 DTO 변환
- 큰 응답 JSON 직렬화와 네트워크 전송
- 데이터베이스에서 불필요한 전체 행 조회

### 목록 DTO가 여러 지연 로딩 연관관계를 사용함

목록 응답은 게시글마다 다음 정보를 읽는다.

- 작성자 닉네임과 프로필 이미지
- 카테고리 이름
- 재능글에 연결된 포트폴리오 ID

연관관계가 지연 로딩이면 게시글 본문 조회 후 Entity마다 추가 조회가 발생할 수 있다.

```text
게시글 1회 조회
→ 작성자 N회 조회
→ 카테고리 N회 조회
→ 카테고리 N회 조회
→ 재능글 포트폴리오 N회 조회
```

### 검색 API와 목록 API의 응답 방식이 달랐음

검색은 이미 `Page`를 사용하지만 일반 목록은 `List`를 사용해 클라이언트가 두 응답 형식을 다르게 처리해야 했다. 목록과 검색의 페이지 크기·정렬·페이지 번호 정책도 일관되게 적용하기 어려웠다.

## T: Task | 과제

다음 조건을 만족하도록 목록 조회를 개선하는 것이 목표였다.

1. 재능글·요청글 전체 조회에서 무제한 `findAll()`을 제거한다.
2. 목록 API와 검색 API를 `Page` 기반 응답으로 통일한다.
3. 기본 페이지 크기와 정렬 기준을 서버에서 제공한다.
4. 목록 DTO에 필요한 연관 엔티티를 가능한 한 목록 조회와 함께 로딩한다.
5. 기존 상세 조회·등록·수정·삭제 API에는 영향을 주지 않는다.
6. 프론트엔드가 `Page.content`를 사용해 기존 목록 화면을 계속 렌더링할 수 있게 한다.

## A: Action | 행동

### 1. Repository 목록 조회에 Pageable 적용

두 Repository에 `Pageable`을 받는 `findAll(Pageable)`을 재정의했다.

```java
@Override
@EntityGraph(attributePaths = {"user", "category"})
Page<RequestPostEntity> findAll(Pageable pageable);
```

재능글은 목록 응답에서 포트폴리오 ID도 사용하므로 `portfolio`까지 함께 조회한다.

### 2. 검색 조회에 EntityGraph 적용

검색 쿼리에도 다음 연관관계 즉시 로딩을 적용했다.

- 요청글: `user`, `category`
- 재능글: `user`, `category`, `portfolio`

`EntityGraph`는 목록 DTO 변환 시 연관 Entity를 개별 조회하는 상황을 줄이는 용도로 사용했다. 검색 조건과 결과 페이지 계산은 기존 JPQL과 Spring Data `Page` 동작을 유지한다.

### 3. Service와 Controller 응답을 Page로 통일

서비스의 목록 메서드는 `List` 대신 `Page`와 `Pageable`을 받도록 변경했다.

```java
public Page<TalentPostEntity> readAll(Pageable pageable) {
    return talentPostRepository.findAll(pageable);
}
```

Controller는 검색 API와 같은 방식으로 `Page.map()`을 사용해 DTO를 변환한다.

```java
return ApiResponse.ok(posts.map(TalentPostResponseDto::toDto));
```

기본 목록 요청은 페이지 크기 20, `createdAt` 내림차순으로 동작한다. 클라이언트는 `page`, `size`, `sort` 파라미터로 페이지를 요청할 수 있다.

### 4. 응답 계약 영향 확인

프론트엔드 목록 API는 이미 `data.content`를 처리할 수 있는 구조이므로 목록 응답을 `Page`로 변경해도 카드 렌더링 데이터는 `content`에서 계속 가져온다. 상세 조회 응답과 게시글 저장 응답은 변경하지 않았다.

## R: Result | 결과 및 배움

### 개선 결과

- 목록 API가 한 번에 전체 게시글을 읽지 않고 페이지 단위로 조회한다.
- 페이지 크기를 제한해 DB 결과, Entity 메모리 사용량, JSON 응답 크기를 줄였다.
- 목록·검색 API가 동일한 `Page` 응답 구조를 사용한다.
- 작성자·카테고리·재능글 포트폴리오의 목록 변환 과정에서 발생할 수 있는 추가 조회를 줄였다.
- 기본 정렬 기준을 서버에서 통일해 페이지 간 결과 변동을 줄였다.

### 배운 점

- `@Transactional`만으로 지연 로딩에 따른 N+1 조회가 사라지는 것은 아니므로 조회 목적에 맞는 fetch 전략이 필요하다.
- 전체 개수가 필요하지 않은 화면은 이후 `Slice`로 전환하면 count 쿼리까지 줄일 수 있다.
- 목록 화면은 상세 화면보다 필요한 필드가 적으므로, 트래픽이 더 커지면 목록 전용 DTO Projection을 도입하는 것이 적절하다.
- `Page` 전환 시 서버뿐 아니라 프론트엔드의 `content` 처리와 페이지네이션 UI를 함께 확인해야 한다.

## 남은 과제

- 목록 전용 Projection으로 `content` 전체와 불필요한 컬럼 조회 제거
- 대표 이미지 조회가 별도 요청으로 발생하는 경우 목록 전용 이미지 조회 최적화
- 전체 개수가 필요 없는 API의 `Slice` 전환 검토
- 실제 PostgreSQL에서 목록·검색 쿼리 수를 검증하는 통합 테스트 추가
- `EXPLAIN ANALYZE`로 인덱스와 페이지 정렬 성능 확인
