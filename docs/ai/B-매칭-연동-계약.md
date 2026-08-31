# B Matching 연동 계약: A(Embedding)

## 1. 목적

이 문서는 B의 Matching 기능과 연동하기 위해 A가 제공해야 하는 데이터 계약과 책임 경계를 정의한다.

```text
A: 원본 게시글 -> Document -> VectorStore 저장
                           |
B: 자연어 검색 -> Vector 후보 -> SQL 원본 검증 -> Ranking
```

현재 B Matching은 구현되어 있다. A의 Document lifecycle은 이 계약에 맞춰 연동한다. Review/Reputation은 MVP 범위에서 제외한다.

## 2. 책임 요약

| 담당 | 소유 범위 | 담당하지 않는 범위 |
| --- | --- | --- |
| A | Document 생성, 임베딩, VectorStore 저장·수정·삭제 | Similarity Search, SQL 조건 검증, Ranking |
| B | VectorStore 읽기, SQL 원본 검증, Filtering, Ranking, 최종 추천 | Document 저장, Review/Reputation |

공통 계약의 key, 값 타입, 점수 범위는 담당자 한 명이 임의로 변경하지 않는다.

## 3. A -> B Embedding 계약

### 3.1 검색 대상

B가 검색하는 타입은 다음 두 개뿐이다.

```text
TALENT
REQUEST
```

`PORTFOLIO`는 A가 다른 기능을 위해 저장할 수 있지만 B Matching 검색에서는 사용하지 않는다.

### 3.2 Document ID

```text
{TARGET_TYPE}:{TARGET_ID}

TALENT:{talentPostId UUID}
REQUEST:{requestPostId UUID}
PORTFOLIO:{portfolioId UUID}
```

- 생성과 수정은 같은 Document ID를 사용한다.
- 수정은 동일 ID로 upsert한다.
- 삭제와 상태 종료는 동일 ID로 Document를 삭제한다.
- 합성 ID를 사용하려면 PgVectorStore의 ID 타입과 DB 컬럼을 `TEXT`로 맞춰야 한다.
- B는 Document ID를 파싱하지 않고 `metadata.targetId`를 사용한다.

### 3.3 필수 Metadata

| Key | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `targetType` | String | 필수 | `TALENT`, `REQUEST`, `PORTFOLIO` |
| `targetId` | String | 필수 | 원본 Entity UUID 문자열 |
| `userId` | String | 필수 | 작성자 UUID 문자열 |
| `categoryId` | String | Talent/Request 필수 | Category UUID 문자열 |
| `status` | String | Talent/Request 필수 | 원본 status enum 이름 |

규칙:

- key 이름과 대소문자를 변경하지 않는다.
- UUID와 enum은 문자열로 저장한다.
- null 값은 저장하지 않고 key를 생략한다.
- 가격, 예산, 기간, 마감일은 metadata에 저장하지 않는다.
- metadata는 source of truth가 아니다.
- 현재 B의 VectorStore filter는 `targetType`만 사용한다.
- B는 상태, 카테고리, 금액, 기간, 마감일을 SQL 원본으로 다시 검증한다.

예시:

```json
{
  "targetType": "TALENT",
  "targetId": "550e8400-e29b-41d4-a716-446655440000",
  "userId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "categoryId": "7d444840-9dc0-11d1-b245-5ffdce74fad2",
  "status": "ACTIVE"
}
```

### 3.4 Document Text

```text
Talent
[TYPE] 재능 제공
[TITLE] TalentPostEntity.title
[CATEGORY] CategoryEntity.name
[DESCRIPTION] TalentPostEntity.content

Request
[TYPE] 재능 요청
[TITLE] RequestPostEntity.title
[CATEGORY] CategoryEntity.name
[DESCRIPTION] RequestPostEntity.content
```

- Markdown 이미지 문법과 이미지 URL은 제거한다.
- 본문의 의미, 기술명, 업무 내용은 유지한다.
- 가격, UUID, 작성자 닉네임, 상태, 날짜, 파일명은 제외한다.
- Talent에 연결된 Portfolio 본문은 Talent text에 합치지 않는다.

### 3.5 Lifecycle

| 원본 변경 | A의 처리 |
| --- | --- |
| Talent `ACTIVE` 생성 | Document 저장 |
| Talent 제목·본문·카테고리 수정 | 동일 ID로 upsert |
| Talent `INACTIVE` 또는 삭제 | Document 삭제 |
| Request `OPEN` 생성 | Document 저장 |
| Request 제목·본문·카테고리 수정 | 동일 ID로 upsert |
| Request `CLOSED`, `CANCELLED` 또는 삭제 | Document 삭제 |

AI API나 VectorStore 장애가 원본 CRUD를 롤백시키지 않도록 한다. 실패 대상은 로그 또는 재처리 데이터로 남기고 재임베딩할 수 있어야 한다.

### 3.6 B의 방어 처리

B는 다음 데이터를 최종 후보에서 제외한다.

- `targetId`가 없거나 UUID로 변환되지 않는 Document
- similarity score가 없는 Document
- VectorStore에는 있지만 SQL 원본이 삭제된 Document
- SQL 원본 상태가 `INACTIVE`, `CLOSED`, `CANCELLED`인 게시글
- SQL 원본이 요청한 카테고리·가격·예산·기간·마감 조건을 만족하지 않는 게시글

## 4. 통합 테스트 완료 조건

- [ ] `TALENT` 검색에 Request와 Portfolio가 섞이지 않는다.
- [ ] `REQUEST` 검색에 Talent와 Portfolio가 섞이지 않는다.
- [ ] 동일 Document를 수정해도 데이터가 중복되지 않는다.
- [ ] 종료·삭제된 게시글의 Document가 제거된다.
- [ ] stale Document가 남아도 B의 SQL 검증에서 제외된다.
- [ ] metadata의 `targetId`와 원본 Entity UUID가 일치한다.

## 5. 변경 규칙

다음 변경은 관련 담당자 합의와 문서·테스트 갱신이 필요하다.

- A/B 합의: Document ID, metadata key·타입, enum 값, text 구성, embedding model·dimension
- A/B 공유: `build.gradle`, `application-ai.yaml`, AI 모델 및 VectorStore 설정

각 담당자는 다른 담당 영역의 Repository나 Service 구현을 직접 수정하기 전에 먼저 변경 이유와 필요한 계약을 공유한다.
