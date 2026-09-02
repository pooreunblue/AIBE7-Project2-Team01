# Spring AI 개발 환경 설정 가이드

## 문서 목적

이 문서는 Google Gemini Embedding과 PostgreSQL pgvector를 이용한 벡터 검색 및 RAG 기능을 개발하기 전에 필요한 환경설정을 안내한다.

현재 프로젝트에는 AI 게시글 생성, Talent/Request 임베딩 lifecycle, Hybrid Matching과 LLM 추천 이유가 구현되어 있다. 이 문서는 해당 기능의 비즈니스 로직보다 실행에 필요한 모델·VectorStore 환경설정을 중심으로 설명한다.

## 현재 구성

| 항목 | 설정 |
| --- | --- |
| Spring Boot | 4.0.7 |
| Java | 17 |
| Spring AI | 2.0.1 |
| Embedding 모델 | `gemini-embedding-001` |
| Embedding 차원 | 1536 |
| Vector Store | PostgreSQL pgvector |
| 거리 계산 | `COSINE_DISTANCE` |
| 인덱스 | `HNSW` |
| 스키마 | `public` |
| 테이블 | `vector_store` |

Spring AI 설정은 `backend/src/main/resources/application-ai.yaml`에서 관리한다.

## 1. Gemini API Key 준비

Gemini API Key는 코드나 YAML에 직접 작성하지 않는다.

`backend/.env.sample`을 참고하여 로컬 전용 `backend/.env` 파일에 값을 입력한다.

```properties
GEMINI_API_KEY=
AI_PGVECTOR_INITIALIZE_SCHEMA=true
```

실제 사용 시 `GEMINI_API_KEY` 우측에 개인 또는 개발환경용 키를 입력한다.

```properties
GEMINI_API_KEY=개발환경용_API_KEY
AI_PGVECTOR_INITIALIZE_SCHEMA=true
```

주의 사항:

- 실제 API Key를 Git에 커밋하지 않는다.
- 채팅, 이슈, PR 설명에 API Key를 남기지 않는다.
- 로컬, 개발 서버, 운영 서버는 서로 다른 키를 사용하는 것을 권장한다.
- 배포 환경에서는 `.env` 파일 대신 GitHub Actions Secret이나 서버 환경변수를 사용한다.

환경별로 API Key가 달라도 애플리케이션 코드는 변경하지 않는다. 모든 환경에서 동일한 `GEMINI_API_KEY` 환경변수 이름을 사용하고 값만 다르게 주입한다.

## 2. pgvector 확장 설치

로컬 DB, 공용 개발 DB, 운영 DB가 서로 다르면 각 PostgreSQL DB에서 확장을 한 번씩 설치해야 한다.

대상 DB에 접속한 후 다음 SQL을 실행한다.

```sql
CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS pgcrypto;
```

설치 여부는 다음 쿼리로 확인한다.

```sql
SELECT extname
FROM pg_extension
WHERE extname IN ('vector', 'pgcrypto');
```

결과에 `vector`, `pgcrypto`가 표시되면 준비가 완료된 것이다.

확장 생성 권한이 없다면 DB 관리자에게 설치를 요청해야 한다. 기존 테이블이나 스키마를 삭제할 필요는 없다.

## 3. Vector Store 스키마 초기화

`application-ai.yaml`은 다음 환경변수로 자동 초기화 여부를 결정한다.

```properties
AI_PGVECTOR_INITIALIZE_SCHEMA=true
```

`true`로 실행하면 Spring AI가 필요한 `vector_store` 테이블과 인덱스 생성을 시도한다.

초기화가 완료된 후에는 필요에 따라 다음과 같이 끌 수 있다.

```properties
AI_PGVECTOR_INITIALIZE_SCHEMA=false
```

기존 벡터 테이블 삭제 옵션은 항상 `false`로 설정되어 있다.

```yaml
remove-existing-vector-store-table: false
```

## 4. 애플리케이션 실행

프로젝트 루트에서 백엔드 디렉터리로 이동한다.

```bash
cd backend
```

테스트를 실행한다.

```bash
./gradlew test
```

애플리케이션을 실행한다.

```bash
./gradlew bootRun
```

정상 실행을 위해 다음 조건이 필요하다.

- `backend/.env`에 `GEMINI_API_KEY`가 설정되어 있어야 한다.
- PostgreSQL, Redis 등 기존 프로젝트 의존 서비스에 접속할 수 있어야 한다.
- 현재 접속 DB에 pgvector 관련 확장이 설치되어 있어야 한다.
- 자동 초기화를 사용한다면 DB 계정에 테이블과 인덱스 생성 권한이 있어야 한다.

## 5. 환경별 설정 방법

### 로컬 개발환경

개발자 개인의 `backend/.env`에 개인 개발용 API Key를 입력한다.

### 공용 개발 서버

서버 환경변수 또는 배포 Secret에 공용 개발용 API Key를 등록한다.

```text
GEMINI_API_KEY=공용_개발용_키
```

### 운영환경

운영 전용 API Key를 Secret Manager 또는 서버 환경변수로 주입한다. 개발용 키와 운영용 키를 공유하지 않는다.

## 6. 모델과 차원 변경 시 주의 사항

현재 Gemini Embedding 모델과 pgvector 차원은 다음과 같이 맞춰져 있다.

```yaml
spring:
  ai:
    google:
      genai:
        embedding:
          text:
            model: gemini-embedding-001
            dimensions: 1536
    vectorstore:
      pgvector:
        dimensions: 1536
```

Embedding 모델 또는 차원을 변경하면 기존에 저장된 벡터와 호환되지 않을 수 있다. 모델 변경은 팀과 협의하고, 변경 후에는 해당 환경의 벡터 데이터를 다시 생성해야 한다.

API Key만 변경하는 경우 기존 벡터 데이터에는 영향을 주지 않는다.

## 7. 다음 개발 단계에서 사용할 Bean

직접 Google GenAI HTTP Client나 별도 Embedding Client를 만들지 않는다. Spring AI가 자동 구성한 Bean을 주입해서 사용한다.

```java
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;

private final EmbeddingModel embeddingModel;
private final VectorStore vectorStore;
```

자동 구성되는 주요 구현체는 다음과 같다.

- `EmbeddingModel`: `GoogleGenAiTextEmbeddingModel`
- `VectorStore`: `PgVectorStore`

## 8. 문제 해결

### `GEMINI_API_KEY`를 찾을 수 없는 경우

- `backend/.env` 파일 위치를 확인한다.
- 환경변수 이름의 대소문자를 확인한다.
- 서버를 다시 실행한다.

### `extension "vector" is not available` 오류

현재 PostgreSQL 서버에 pgvector가 설치되지 않은 상태다. PostgreSQL 서버에 pgvector 패키지를 설치한 후 `CREATE EXTENSION` 쿼리를 다시 실행한다.

### `permission denied to create extension` 오류

현재 DB 사용자의 권한이 부족하다. 관리자 권한 계정으로 확장을 설치하거나 DB 관리자에게 요청한다.

### Vector dimension 오류

Gemini Embedding 차원과 pgvector 테이블의 차원이 다른 상태다. 현재 프로젝트에서는 두 설정 모두 1536이어야 한다.

### 기존 `vector_store`와 차원이 다른 경우

운영 데이터를 임의로 삭제하지 않는다. 기존 데이터 백업과 재임베딩 계획을 세운 후 팀 협의를 거쳐 마이그레이션한다.

## 참고 문서

- [Spring AI Getting Started](https://docs.spring.io/spring-ai/reference/getting-started.html)
- [Google GenAI Text Embeddings](https://docs.spring.io/spring-ai/reference/api/embeddings/google-genai-embeddings-text.html)
- [Spring AI PGvector](https://docs.spring.io/spring-ai/reference/api/vectordbs/pgvector.html)
