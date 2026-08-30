# AI 기능 개발 가이드

상세 연동 규칙은 [B Matching 연동 계약](./matching-integration-contract.md)에서 확인한다.

## 1. 목표

Talent와 Request를 자연어로 검색하고, 원본 데이터와 사용자 Reputation을 함께 검증해 적합한 후보와 추천 이유를 제공한다. Portfolio 임베딩은 다른 AI 기능에서 사용할 수 있지만 B Matching 검색 대상에서는 제외한다.

```text
게시글/포트폴리오
→ Embedding → pgvector 저장
→ 자연어 검색 → Vector Search
→ 원본 데이터 조건 검증 → Ranking
→ Reputation 반영 → 최종 추천 → 추천 이유 생성
```

현재 Spring AI, Gemini Embedding/ChatModel, PostgreSQL pgvector 초기 설정만 완료되어 있다. AI 비즈니스 기능과 Review/Reputation은 아직 구현 전이다.

## 2. 역할 분담

| 담당 | 영역 | 주요 작업 |
| --- | --- | --- |
| A | Generation / Embedding | AI 게시글 생성, Talent/Request/Portfolio 임베딩, 생성·수정·삭제에 따른 VectorStore 데이터 관리 |
| B | Matching / RAG | Talent/Request 자연어 검색, Vector Search, 원본 조건 검증, MatchScore, Reputation 반영, 최종 후보와 추천 이유 생성 |
| C | Review / Reputation | Review CRUD, 리뷰 분석, 사용자 Reputation 계산, B가 사용할 Reputation 정보 제공 |

Review는 현재 placeholder만 존재한다. C가 Review 구조와 Reputation 응답 계약을 먼저 정하고, B는 Review Repository를 직접 사용하지 않고 C가 제공하는 Service/DTO를 통해 Reputation을 조회한다.

Review 작성 가능 조건을 거래 완료(`TradeStatus.COMPLETED`)와 연결할지, 중복 리뷰를 어떻게 제한할지는 C 구현 전에 거래 담당자와 합의한다. C 연동 전 B는 Reputation을 제외하거나 중립값으로 처리한다.

## 3. 전체 구조

```text
ai
├── embedding       # A/B 공통 계약과 VectorStore 쓰기
├── generation      # A
├── matching        # B
└── reputation      # C, Review 분석 결과 제공

A: Domain CRUD → Document 생성 → EmbeddingService → VectorStore
B: AI Chat → TALENT/REQUEST 검색 계획 → Vector Search → 원본 DB → Ranking → 추천
C: Review CRUD/분석 → ReputationService → B Ranking
```

## 4. 공통 Embedding 규칙

MVP에서는 Spring AI `VectorStore`를 사용하며 별도 `AiEmbeddingEntity`, `AiEmbeddingRepository`, 자체 HTTP Client를 만들지 않는다.

### Document ID

```text
{TARGET_TYPE}:{TARGET_ID}

TALENT:{UUID}
REQUEST:{UUID}
PORTFOLIO:{UUID}
```

`EmbeddingTargetType`은 `TALENT`, `REQUEST`, `PORTFOLIO`만 사용한다. 동일 ID로 다시 저장해 수정 시 upsert하고, 삭제 시 같은 ID를 사용한다.

주의: 현재 pgvector 기본 ID 컬럼은 UUID 타입이다. 위 합성 ID를 사용하기 전에 팀 합의 후 VectorStore ID 타입을 `TEXT`로 맞춰야 한다.

### Metadata

| 대상 | 필수 metadata |
| --- | --- |
| 공통 | `targetType`, `targetId`, `userId` |
| Talent | 공통 + `categoryId`, `status` |
| Request | 공통 + `categoryId`, `status` |
| Portfolio | 공통 항목만 사용 |

UUID와 enum은 문자열로 저장하고 nullable 값은 key 자체를 생략한다. 가격, 예산, 기간, 마감일은 우선 원본 DB에서 최종 검증한다. Metadata는 후보를 좁히기 위한 복제 정보이며 source of truth가 아니다.

### Document Text

```text
Talent:    [TYPE] 재능 제공 / [TITLE] / [CATEGORY] / [DESCRIPTION]
Request:   [TYPE] 재능 요청 / [TITLE] / [CATEGORY] / [DESCRIPTION]
Portfolio: [TYPE] 포트폴리오 / [TITLE] / [DESCRIPTION]
```

Markdown 이미지 문법과 URL은 제거하되 의미가 있는 본문과 기술 키워드는 유지한다. 가격, UUID, 닉네임, 상태, 날짜, 파일명은 text에서 제외한다.

## 5. Matching 흐름

```text
사용자 Query
→ targetType 확인(TALENT 또는 REQUEST)
→ 초기에는 API가 구조화된 검색 조건을 직접 받음
→ 서버가 검색 계획 검증
→ targetType metadata filter
→ Vector Search로 후보 추출
→ targetId로 Talent 또는 Request 원본 일괄 조회
→ 가격/예산/카테고리/상태/기간 최종 검증
→ 서버 MatchScore 계산
→ C의 Reputation 정보 반영
→ TOP 3~5 선정
→ LLM이 추천 이유 생성
```

B는 Portfolio Document를 검색하거나 Ranking에 직접 사용하지 않는다. 초기 API는 `query`, `targetType`, 선택적 `categoryId`와 금액·기간 조건을 받으며 자연어 조건 추출은 검색 흐름이 안정된 뒤 추가한다.

`VectorSearchService`는 Spring AI `VectorStoreRetriever`로 검색하고, `AiMatchingService`는 원본 조회와 전체 흐름을 조정하며, `MatchRankingService`는 외부 의존성 없이 점수만 계산한다. Ranking은 서버가 결정하고 LLM은 내부 UUID나 임의 SQL/filter 또는 순위를 만들지 않는다.

상태와 명시적 category, 가격 상한, 예산 범위, 기간, 마감일은 VectorStore metadata가 아닌 SQL 원본을 기준으로 필수 Filtering 처리한다. 초기 MatchScore는 semantic score 중심으로 시작하고 금액 선호가 있을 때만 amount fit을 추가한다. Reputation은 C 계약이 완료된 뒤 반영한다.

## 6. 개발 순서

- [ ] 공통 Document ID, metadata key, text 규격 확정
- [ ] VectorStore ID 타입을 합성 ID 규칙과 일치시키기
- [ ] A가 Talent 저장·수정·삭제와 검색용 metadata 준비
- [ ] B가 TALENT Vector Search와 similarity score 추출 검증
- [ ] B가 Talent 원본 Entity 일괄 조회와 필수 조건 Filtering 구현
- [ ] B가 구조화된 TALENT Matching API와 단순 Ranking 완성
- [ ] A가 Request/Portfolio 임베딩 확장
- [ ] B가 REQUEST Matching으로 확장(Portfolio 검색 제외)
- [ ] C Review CRUD와 작성 가능 조건 구현
- [ ] C 리뷰 분석 및 Reputation 응답 계약 구현
- [ ] B Ranking에 Reputation 연동
- [ ] LLM 자연어 Query 조건 분석
- [ ] LLM 추천 이유 생성
- [ ] 기존 데이터 재임베딩과 장애 복구 방법 준비
- [ ] 프론트 AI 채팅 연동

## 7. 협업 시 주의사항

- 공통 `ai/embedding` 코드는 담당자와 협의 없이 임의 수정하지 않는다.
- Document ID와 metadata key 이름 및 타입을 임의 변경하지 않는다.
- `build.gradle`, `application-ai.yaml`, 모델명, embedding 차원 변경은 반드시 팀에 공유한다.
- Metadata와 Reputation 결과는 원본 도메인 데이터의 source of truth로 사용하지 않는다.
- B는 Review Repository에 직접 의존하지 않고 C가 제공하는 Reputation 계약을 사용한다.
- C는 Reputation 계산 규칙과 점수 범위를 변경할 때 B에게 먼저 공유한다.
- 각 담당 영역 밖의 Domain Entity, Service, DTO를 수정해야 하면 해당 담당자에게 먼저 공유한다.
- AI API 장애 때문에 원본 Talent/Request/Portfolio/Review CRUD가 롤백되지 않도록 한다.
- 임베딩 실패는 기록하고 대상별 또는 전체 재임베딩으로 복구할 수 있게 한다.
- 실제 API Key와 사용자 원문·리뷰 내용을 로그나 PR에 남기지 않는다.
