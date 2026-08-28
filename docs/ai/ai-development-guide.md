# AI 기능 개발 가이드

## 1. 목표

Talent, Request, Portfolio를 임베딩하여 자연어로 검색하고, 원본 데이터와 사용자 Reputation을 함께 검증해 적합한 후보와 추천 이유를 제공한다.

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
| B | Matching / RAG | 자연어 검색, Vector Search, 조건 검증, MatchScore, Reputation 반영, 최종 후보와 추천 이유 생성 |
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
B: AI Chat → 검색 계획 → Vector Search → 원본 DB → Ranking → 추천
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
→ LLM이 targetType과 검색 조건을 구조화
→ 서버가 검색 계획 검증
→ targetType/status/category metadata filter
→ Vector Search로 후보 추출
→ targetId로 Talent/Request/Portfolio 원본 일괄 조회
→ 가격/예산/카테고리/상태/기간 최종 검증
→ 서버 MatchScore 계산
→ C의 Reputation 정보 반영
→ TOP 3~5 선정
→ LLM이 추천 이유 생성
```

LLM은 내부 UUID나 임의 SQL/filter를 만들지 않는다. Ranking은 서버가 계산하며 LLM이 순위를 임의로 변경하지 않는다.

## 6. 개발 순서

- [ ] 공통 Document ID, metadata key, text 규격 확정
- [ ] VectorStore ID 타입을 합성 ID 규칙과 일치시키기
- [ ] Talent 저장·수정·삭제 및 검색 검증
- [ ] Request와 Portfolio 임베딩 확장
- [ ] B 자연어 검색 계획 및 Vector Search 구현
- [ ] 원본 DB 조건 Filtering과 MatchScore 구현
- [ ] C Review CRUD와 작성 가능 조건 구현
- [ ] C 리뷰 분석 및 Reputation 응답 계약 구현
- [ ] B Ranking에 Reputation 연동
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
