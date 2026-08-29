# ADR-002: Vector Search와 SQL 검증을 결합한 AI 매칭 시스템

- 상태: 승인됨 (Accepted)
- 결정일: 2026-08-29
- 적용 범위: Talent/Request 자연어 검색, 후보 필터링, Ranking, Reputation 및 추천 이유 연동
- 구현 상태: `feature/ai-matching` 브랜치에 B Matching 기본 구현 존재, A Embedding 및 C Reputation 연동 필요

## 배경

사용자가 입력한 자연어와 의미적으로 가까운 Talent 또는 Request 게시글을 찾아 적합한 후보를 추천해야 한다.

SQL 조건 검색만 사용하면 표현이 다른 유사 게시글을 찾기 어렵다. 반대로 Vector Similarity Search 결과만 추천에 사용하면 삭제되거나 종료된 게시글, 가격이나 기간 조건이 맞지 않는 게시글, 갱신이 지연된 metadata가 최종 결과에 포함될 수 있다.

또한 LLM이 전체 검색과 순위를 결정하게 하면 결과의 재현성, 테스트 가능성 및 설명 가능성이 낮아지고 외부 API 비용과 응답 시간도 증가한다.

따라서 의미 기반 후보 검색과 정확한 원본 데이터 검증의 책임을 분리할 필요가 있다.

## 결정

매칭 시스템은 Vector Search와 SQL 원본 조회를 결합한 Hybrid Matching 구조를 사용한다.

```text
사용자 Query와 구조화 조건
→ targetType 확인(TALENT 또는 REQUEST)
→ Vector Similarity Search
→ Vector 후보의 targetId 추출
→ 원본 Entity 일괄 조회
→ 상태·카테고리·금액·기간·마감 조건 검증
→ Java 서버에서 MatchScore 계산
→ Reputation 반영
→ TOP 3~5 선정
→ LLM 추천 이유 생성
```

Vector Search는 의미적으로 유사한 후보를 넓게 찾는 역할만 담당한다. 최종 후보의 유효성과 정형 조건은 PostgreSQL의 원본 Entity를 기준으로 판단한다.

## 검색 대상

B Matching의 검색 대상은 다음 두 유형으로 제한한다.

```text
TALENT
REQUEST
```

Portfolio는 다른 AI 기능을 위해 임베딩할 수 있지만 Matching의 Vector Search와 Ranking에는 직접 사용하지 않는다. 추후 Portfolio가 추천 근거로 필요해지면 Talent 후보가 결정된 후 원본 관계를 통해 별도로 조회한다.

## Embedding 계약

Embedding 저장, 수정 및 삭제는 A 담당 영역이며 B는 VectorStore를 읽기 전용으로 사용한다.

### Document ID

```text
{TARGET_TYPE}:{TARGET_ID}

TALENT:{UUID}
REQUEST:{UUID}
```

B는 Document ID 문자열을 직접 파싱하지 않고 `metadata.targetId`를 사용한다.

### 필수 Metadata

| Key | 타입 | 용도 |
| --- | --- | --- |
| `targetType` | String | `TALENT`, `REQUEST` 검색 범위 구분 |
| `targetId` | String | 원본 Entity UUID 조회 |
| `userId` | String | 작성자와 Reputation 연동 |
| `categoryId` | String | 카테고리 후보 정보 |
| `status` | String | 후보 상태 정보 |

UUID와 enum은 문자열로 저장하며 null 값은 metadata에 넣지 않는다. Metadata는 검색 후보를 좁히기 위한 복제 정보이며 원본 데이터의 source of truth가 아니다.

가격, 예산, 기간 및 마감일은 MVP metadata에 저장하지 않고 SQL 원본 조회 단계에서 검증한다.

## 검색과 검증 책임

### Vector Search

- Spring AI `VectorStore` 또는 `VectorStoreRetriever`를 사용한다.
- `SearchRequest`에 `targetType` metadata filter를 적용한다.
- 초기에는 내부 후보를 20~30개 조회하고 최종 3~5개를 반환한다.
- 의미 유사도는 검색 결과의 `Document.getScore()`를 사용한다.
- metadata 누락, UUID 변환 실패 또는 score 누락 후보는 제외한다.

### SQL 원본 검증

- Vector 결과의 `targetId`를 UUID로 변환한다.
- 후보마다 Repository를 호출하지 않고 `findAllById` 등으로 일괄 조회한다.
- Vector 결과 순서와 score를 보존하기 위해 ID 기반 Map으로 원본 Entity를 결합한다.
- VectorStore에만 남은 stale Document는 원본 Entity가 없으면 제외한다.
- metadata와 원본 값이 다르면 원본 DB 값을 사용한다.

## Filtering과 Ranking

반드시 만족해야 하는 조건은 점수를 낮추는 방식이 아니라 후보 제외 조건으로 처리한다.

### Filtering 대상

- Talent의 활성 상태
- Request의 모집 가능 상태와 마감 여부
- 사용자가 명시한 카테고리
- Talent 가격 상한과 소요 기간 조건
- Request 예산 범위와 마감일 조건

### 초기 Ranking 요소

- semantic similarity
- 명시적인 금액 조건이 있을 때 price/budget fit
- C 연동 후 reputation score

초기 MVP는 semantic similarity 중심의 단순한 가중치로 시작한다. 실제 검색 데이터와 테스트 결과 없이 복잡한 추천 공식을 추가하지 않는다.

Ranking은 외부 의존성이 없는 Java 코드로 계산하여 동일 입력에 동일 결과가 나오도록 하고 단위 테스트가 가능하게 유지한다.

## LLM의 책임

LLM은 최종 순위를 직접 결정하지 않는다.

LLM이 담당할 수 있는 기능은 다음으로 제한한다.

- 자연어 Query에서 semantic query와 검증 가능한 구조화 조건 추출
- 서버가 선정한 최종 후보에 대한 추천 이유 생성

LLM이 임의 SQL, 내부 UUID, VectorStore filter 또는 최종 Ranking을 생성하지 않도록 한다. 초기 MVP에서는 구조화된 조건을 API 요청으로 직접 받고, 기본 매칭 흐름이 안정된 후 자연어 조건 분석을 추가한다.

이 구조는 검색 결과를 원본 데이터로 보강해 답변하는 RAG 성격을 가지지만, 범용 문서 질의응답 RAG에 전체 흐름을 위임하지 않는다. 매칭 서비스가 검색, 원본 검증 및 Ranking 순서를 명시적으로 제어한다.

## 서비스 책임

| 구성요소 | 책임 |
| --- | --- |
| `AiMatchingController` | 매칭 요청 수신과 응답 반환 |
| `AiMatchingService` | 전체 검색·검증·Ranking 흐름 조정 |
| `VectorSearchService` | VectorStore 검색과 metadata 후보 변환 |
| `MatchCandidateFactory` | 원본 Entity와 Vector score를 후보 모델로 조합 |
| `MatchCandidateFilter` | 원본 데이터 기준 필수 조건 검사 |
| `MatchRankingService` | MatchScore 계산과 정렬 |
| `MatchConditionValidator` | targetType별 요청 조건 검증 |
| `ReputationService` | C가 제공하는 사용자 Reputation 일괄 조회 |

B는 Embedding 저장 코드를 수정하지 않고 Review Repository에도 직접 의존하지 않는다.

## 장애 처리

- VectorStore 장애 시 정상 매칭 결과를 만들 수 없으므로 명확한 검색 실패로 처리한다.
- 잘못되거나 누락된 개별 Vector Document는 해당 후보만 제외하고 전체 검색은 계속한다.
- VectorStore와 원본 DB의 일시적 불일치는 SQL 원본 검증 단계에서 방어한다.
- C Reputation 조회 실패 시 Reputation을 제외한 기본 점수로 결과를 반환한다.
- 추천 이유 생성 실패 시 이미 계산된 후보와 점수는 유지하고 추천 이유만 생략할 수 있어야 한다.
- AI 검색 장애가 Talent 또는 Request 원본 CRUD를 롤백시키지 않도록 한다.

## 검토한 대안

### SQL 키워드 및 조건 검색만 사용

- 장점: 구현과 결과 해석이 단순하다.
- 단점: 검색어와 게시글의 표현이 다르면 의미적으로 유사한 결과를 찾기 어렵다.
- 결론: 의미 검색 요구를 충족하지 못해 채택하지 않는다.

### Vector Search 결과를 그대로 추천

- 장점: 구현 단계가 적고 응답 흐름이 단순하다.
- 단점: 상태, 가격, 예산, 기간 및 stale metadata를 정확히 검증할 수 없다.
- 결론: 최종 결과의 정확성과 정합성이 부족해 채택하지 않는다.

### SQL 조건으로 전체 후보를 먼저 조회한 후 애플리케이션에서 Vector 비교

- 장점: 정형 조건을 먼저 강하게 적용할 수 있다.
- 단점: 후보 전체의 embedding을 애플리케이션으로 가져와 비교해야 하므로 데이터가 늘면 비효율적이다.
- 결론: pgvector의 검색 기능을 활용하는 현재 구조보다 장점이 작아 채택하지 않는다.

### LLM이 검색과 Ranking을 모두 결정

- 장점: 복잡한 자연어 요구를 유연하게 해석할 수 있다.
- 단점: 결과 재현성, 테스트 가능성, 비용, 응답 시간 및 설명 가능성이 나빠진다.
- 결론: LLM은 조건 추출과 추천 이유에만 사용한다.

### 별도 Vector Database 도입

- 장점: 대규모 벡터 검색에 특화된 운영 기능을 사용할 수 있다.
- 단점: 현재 데이터 규모와 MVP 기간에 비해 인프라 및 운영 복잡도가 커진다.
- 결론: 기존 PostgreSQL과 pgvector를 사용하고 규모가 커질 때 재검토한다.

## 결과

### 긍정적 영향

- 자연어의 의미적 유사성과 원본 데이터의 정확한 조건을 함께 반영할 수 있다.
- stale Vector Document가 최종 추천으로 노출되는 위험을 줄인다.
- Ranking이 Java 코드에 있어 결과를 재현하고 단위 테스트할 수 있다.
- A Embedding, B Matching, C Reputation의 책임 경계가 명확해진다.
- Spring AI 표준 API와 기존 PostgreSQL 인프라를 재사용한다.

### 부정적 영향 및 비용

- 한 번의 매칭 요청에 VectorStore 검색과 원본 DB 조회가 모두 필요하다.
- VectorStore와 원본 DB 사이에 짧은 불일치 기간이 생길 수 있다.
- 모델이나 Document text 규칙이 바뀌면 기존 데이터를 재임베딩해야 한다.
- topK, similarity threshold 및 Ranking 가중치는 실제 데이터로 조정해야 한다.
- Gemini 및 외부 네트워크 장애가 의미 검색과 추천 이유 생성에 영향을 줄 수 있다.

## 구현 위치

다음 코드는 현재 `feature/ai-matching` 브랜치에 존재하며 통합 브랜치 반영이 필요하다.

- Controller: `backend/src/main/java/org/example/link/ai/matching/controller/AiMatchingController.java`
- DTO: `backend/src/main/java/org/example/link/ai/matching/dto/`
- 전체 흐름: `backend/src/main/java/org/example/link/ai/matching/service/AiMatchingService.java`
- Vector 검색: `backend/src/main/java/org/example/link/ai/matching/service/search/VectorSearchService.java`
- 후보 생성·검증: `backend/src/main/java/org/example/link/ai/matching/service/candidate/`, `service/filter/`, `service/condition/`
- Ranking: `backend/src/main/java/org/example/link/ai/matching/service/ranking/MatchRankingService.java`
- 테스트: `backend/src/test/java/org/example/link/ai/matching/`

## 후속 규칙

- A/B가 합의한 Document ID, metadata key와 값 타입을 임의로 변경하지 않는다.
- B Matching은 VectorStore에 Document를 저장하거나 삭제하지 않는다.
- Metadata만 믿고 최종 후보를 반환하지 않으며 항상 원본 Entity를 검증한다.
- 후보 원본 조회는 일괄 처리하여 N+1 조회를 만들지 않는다.
- C Reputation은 Service/DTO 계약으로 연동하고 Review Repository를 직접 조회하지 않는다.
- LLM이 최종 순위를 결정하게 하지 않는다.
- Portfolio를 검색 대상에 추가하거나 Vector Database를 교체할 때는 이 ADR을 다시 검토한다.
