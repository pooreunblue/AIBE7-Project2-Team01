# ADR-0001: Spring AI 기반 검색·매칭 아키텍처 도입

- 상태: Accepted
- 결정일: 2026-08-28
- 대상: AI 게시글 생성, Embedding, Matching/RAG, Review/Reputation
- 관련 문서:
  - [AI 기능 개발 가이드](../ai/ai-development-guide.md)
  - [A-B Embedding 연동 계약](../ai/a-b-embedding-contract.md)
  - [B Matching 연동 계약](../ai/matching-integration-contract.md)
  - [Spring AI 개발 환경 설정 가이드](../ai/spring-ai-setup-guide.md)

## 1. 배경

프로젝트는 Talent, Request 게시글을 자연어로 검색하고, 검색 의도와 조건에 맞는 후보를 추천하는 기능이 필요하다. 이후 AI 게시글 생성, Review 분석, 사용자 Reputation을 추천 결과에 반영할 예정이다.

현재 백엔드는 Spring Boot 4.0.7, Java 17, PostgreSQL, Spring Data JPA를 사용한다. AI 초기 환경에는 Spring AI 2.0.1, Google GenAI Gemini, PostgreSQL pgvector가 설정되어 있다.

AI 기능은 다음 세 영역을 병렬 개발한다.

| 담당 | 영역 | 책임 |
| --- | --- | --- |
| A | Generation / Embedding | AI 게시글 생성, Document 생성, VectorStore lifecycle |
| B | Matching / RAG | Vector Search, 원본 검증, Filtering, Ranking, 추천 응답 |
| C | Review / Reputation | Review CRUD·분석, 사용자 Reputation 제공 |

공통 데이터 계약과 책임 경계를 미리 정하지 않으면 Document metadata 불일치, 중복 저장 계층, 원본 DB와 VectorStore 간 정합성 문제, 담당자 간 코드 충돌이 발생할 수 있다.

## 2. 결정

### 2.1 AI 통합 기술

Spring AI 2.0.1을 AI 모델과 VectorStore의 공통 추상화로 사용한다.

- ChatModel: Google GenAI `gemini-2.5-flash`
- EmbeddingModel: Google GenAI `gemini-embedding-001`
- Embedding 차원: 1536
- VectorStore: Spring AI PgVectorStore
- 저장소: 기존 PostgreSQL의 `public.vector_store`
- 거리 계산: cosine distance
- 인덱스: HNSW

API Key는 코드에 저장하지 않고 `GEMINI_API_KEY` 환경변수로 주입한다.

### 2.2 VectorStore 저장 구조

MVP에서는 별도의 `AiEmbeddingEntity`, `AiEmbeddingRepository`, 자체 Embedding HTTP Client를 구현하지 않는다. Spring AI `Document`, `EmbeddingModel`, `VectorStore`가 제공하는 저장·검색·삭제 기능을 사용한다.

별도 영속 계층은 임베딩 버전, 실패 이력, 재시도 상태, 다중 청크 관계 또는 outbox 작업을 DB에서 관리해야 할 때 다시 검토한다.

### 2.3 Document 계약

Document ID는 다음 합성 문자열을 사용한다.

```text
{TARGET_TYPE}:{TARGET_ID}
```

예시:

```text
TALENT:{UUID}
REQUEST:{UUID}
PORTFOLIO:{UUID}
```

따라서 PgVectorStore ID 컬럼은 `TEXT` 타입을 사용한다. 실제 데이터 적재 전에 `spring.ai.vectorstore.pgvector.id-type: TEXT` 설정과 DB 스키마를 일치시킨다.

공통 metadata는 다음과 같다.

| 대상 | Metadata |
| --- | --- |
| 공통 | `targetType`, `targetId`, `userId` |
| Talent | 공통 + `categoryId`, `status` |
| Request | 공통 + `categoryId`, `status` |
| Portfolio | 공통 항목만 사용 |

UUID와 enum은 문자열로 저장하며 null metadata는 저장하지 않는다. 가격, 예산, 기간, 마감일은 MVP metadata에서 제외하고 원본 DB에서 검증한다.

### 2.4 검색 대상

B Matching의 검색 대상은 `TALENT`, `REQUEST`로 제한한다.

Portfolio는 A가 다른 AI 기능을 위해 임베딩할 수 있지만 B Matching의 Vector Search, Filtering, Ranking에는 직접 사용하지 않는다. 향후 Portfolio를 추천 근거로 사용할 필요가 생기면 Talent 후보 선정 후 원본 DB에서 별도로 조회한다.

### 2.5 Hybrid Matching

Matching은 Vector Search만으로 결정하지 않고 다음 순서를 사용한다.

```text
사용자 Query와 구조화 조건
→ targetType metadata filter
→ Vector Similarity Search
→ targetId 목록 추출
→ Talent 또는 Request 원본 Entity 일괄 조회
→ 상태·카테고리·금액·기간·마감일 검증
→ 서버 MatchScore 계산
→ Reputation 반영
→ TOP 3~5 선정
→ LLM 추천 이유 생성
```

VectorStore metadata는 후보 검색용 복제 정보이며 source of truth가 아니다. 최종 후보 결정은 항상 원본 Entity 상태를 기준으로 한다.

### 2.6 Ranking과 LLM 책임

Ranking은 Java 서버 코드에서 계산한다. 초기 점수는 semantic similarity를 중심으로 시작하고, 명시적인 금액 선호가 있을 때 amount fit을 추가한다. Reputation은 C의 계약이 완료된 후 반영한다.

상태, 명시적 category, 가격 상한, 예산 범위, 필수 기간, 마감 여부는 점수가 아니라 탈락 조건으로 처리한다.

LLM은 다음 역할만 담당한다.

- 후속 단계에서 자연어 Query를 검증 가능한 구조화 조건으로 변환
- 서버가 확정한 TOP 후보에 대한 추천 이유 생성

LLM은 임의 SQL, 내부 UUID, VectorStore filter 또는 최종 순위를 결정하지 않는다.

### 2.7 Embedding lifecycle과 장애 처리

A는 원본 CRUD 완료 후 Document를 upsert 또는 삭제한다.

- Talent `ACTIVE`, Request `OPEN`: 생성·수정 시 upsert
- Talent `INACTIVE`: 삭제
- Request `CLOSED`, `CANCELLED`: 삭제
- 원본 삭제: Document 삭제

AI API 또는 VectorStore 장애가 원본 Talent, Request, Portfolio CRUD를 롤백시키지 않는 방향을 유지한다. 실패 대상은 재처리할 수 있어야 하며 B는 stale Document를 원본 DB 검증 단계에서 제거한다.

## 3. 서비스 경계

### A

- 공통 `ai/embedding` 계약 소유
- Entity별 Spring AI Document 생성
- VectorStore add/upsert/delete
- 임베딩 실패 처리와 재임베딩 경로

### B

- `VectorStoreRetriever`와 `SearchRequest`를 이용한 읽기 전용 검색
- `targetType` metadata filter
- SQL 원본 기준 status, category, 금액, 기간 검증
- `Document.getScore()` 기반 semantic score 추출
- `findAllById` 기반 원본 Entity 일괄 조회
- 필수 조건 Filtering, MatchScore, TOP 후보 선정
- C Reputation 및 LLM 추천 이유 연동

### C

- Review CRUD와 작성 가능 조건 검증
- 리뷰 분석과 사용자 Reputation 계산
- B가 Review Repository에 직접 의존하지 않도록 Service/DTO 계약 제공

## 4. 고려한 대안

### 별도 AI Embedding Entity와 Repository

선택하지 않았다. MVP에서는 Spring AI VectorStore와 중복되며 관리할 데이터와 코드가 늘어난다. 실패·재시도·버전 관리 요구가 구체화되면 별도 작업 테이블을 검토한다.

### 별도 Vector Database 도입

선택하지 않았다. 현재 데이터 규모와 1주 MVP 범위에서는 기존 PostgreSQL과 pgvector로 충분하며 운영 구성요소를 추가할 이유가 작다.

### 키워드 SQL 검색만 사용

선택하지 않았다. 제목과 본문의 표현이 다르더라도 의미가 유사한 게시글을 찾는 요구를 충족하기 어렵다. 기존 SQL 검색은 보조 기능으로 유지할 수 있다.

### Vector Search 결과를 즉시 추천

선택하지 않았다. metadata 지연, 삭제된 원본, 상태·가격·기간 조건을 검증할 수 없으므로 잘못된 추천이 발생할 수 있다.

### LLM이 전체 Ranking 결정

선택하지 않았다. 결과 재현성, 테스트 가능성, 비용, 응답 지연과 설명 가능성이 나빠진다. 서버 Ranking 후 LLM은 설명만 생성한다.

### 범용 RAG Advisor에 전체 흐름 위임

선택하지 않았다. Vector 검색 직후 원본 DB 검증과 정형 조건 Filtering이 필요하므로 B의 서비스가 검색·검증·Ranking 순서를 명시적으로 조정한다.

## 5. 결과

### 긍정적 결과

- 기존 PostgreSQL 인프라를 재사용한다.
- A/B/C의 책임과 변경 범위가 명확해진다.
- 검색과 원본 검증을 분리해 stale metadata 위험을 줄인다.
- Ranking 결과를 재현하고 단위 테스트할 수 있다.
- Spring AI 표준 API를 사용해 provider 종속 코드를 줄인다.

### 부정적 결과와 비용

- 원본 DB와 VectorStore 사이에 일시적인 불일치가 존재할 수 있다.
- 모델 또는 text 규칙 변경 시 기존 데이터를 재임베딩해야 한다.
- Vector 후보 조회 후 원본 DB 조회가 추가되어 요청당 DB 접근이 늘어난다.
- Gemini와 외부 네트워크 장애에 영향을 받는다.
- 초기에는 similarity threshold와 점수 가중치를 데이터로 조정해야 한다.

## 6. 구현 제약과 후속 작업

- [ ] 합성 ID 사용 전에 PgVectorStore `id-type: TEXT`와 실제 테이블 스키마를 일치시킨다.
- [ ] A/B metadata key와 값 타입을 통합 테스트로 고정한다.
- [ ] A가 Talent Document lifecycle을 먼저 완성한다.
- [ ] B가 TALENT 검색과 `Document.getScore()` 추출을 검증한다.
- [ ] Request로 확장하기 전에 TALENT vertical slice를 완성한다.
- [ ] C가 Reputation 응답과 점수 범위를 확정한다.
- [ ] 임베딩 실패 대상 재처리 방법을 마련한다.
- [ ] 실제 검색 데이터로 topK, threshold, 점수 가중치를 조정한다.
- [ ] 자연어 조건 분석과 추천 이유는 구조화 Matching API가 안정된 후 추가한다.

## 7. 변경 기준

다음 변경은 이 ADR의 후속 ADR 또는 명시적인 개정이 필요하다.

- B Matching 검색 대상에 Portfolio 또는 새로운 타입 추가
- pgvector 외 Vector Database 도입
- EmbeddingModel 또는 embedding 차원 변경
- LLM이 Ranking을 직접 결정하도록 책임 변경
- 별도 Embedding Entity/Repository 또는 outbox 도입
- Metadata key, 값 타입, Document ID 규칙 변경
