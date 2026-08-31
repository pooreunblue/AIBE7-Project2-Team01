# TalentPulse 문서 안내

## 핵심 명세

| 문서 | 내용 |
| --- | --- |
| [프로젝트 기획서](./기획서.md) | 서비스 배경, 핵심 기능과 MVP 범위 |
| [요구사항 명세서](./requirements.md) | 기능별 완료 상태와 MVP 제외·후속 범위 |
| [API 명세서](./api.md) | REST, WebSocket, 인증·CSRF와 AI 요청 계약 |
| [ERD](./erd.md) | 도메인 관계, 상태, 제약과 VectorStore |
| [초기 스키마](./schema.sql) | PostgreSQL 신규 DB 생성 기준 |

## AI

| 문서 | 내용 |
| --- | --- |
| [AI 기능 개발 가이드](./ai/AI-기능-개발-가이드.md) | 역할, 전체 흐름과 구현 현황 |
| [Spring AI 환경설정 가이드](./ai/Spring-AI-환경설정-가이드.md) | Gemini, pgvector와 환경변수 설정 |
| [A-B 임베딩 연동 계약](./ai/A-B-임베딩-연동-계약.md) | Document ID, metadata, text와 lifecycle |
| [B 매칭 연동 계약](./ai/B-매칭-연동-계약.md) | Vector Search와 SQL 원본 검증 계약 |
| [AI 에이전트 설계 원칙](./ai/AI-에이전트-설계.md) | 코드 작업 시 공통 개발 원칙 |

## ADR

| ADR | 상태 | 결정 |
| --- | --- | --- |
| [ADR-000](./adr/ADR-000-Spring-AI-초기-아키텍처.md) | 대체됨 | Spring AI 초기 아키텍처 |
| [ADR-001](./adr/ADR-001-HttpOnly-쿠키-JWT-인증.md) | 승인 | JWT를 HttpOnly Cookie로 관리 |
| [ADR-002](./adr/ADR-002-하이브리드-AI-매칭-아키텍처.md) | 승인 | Vector Search와 SQL 검증을 결합한 매칭 |
| [ADR-003](./adr/ADR-003-리뷰-평판-MVP-제외.md) | 승인 | Review/Reputation을 MVP에서 제외 |

## 트러블슈팅

- [거래 정합성 및 임베딩 AFTER_COMMIT](./troubleshooting/거래-정합성-및-임베딩-AFTER-COMMIT.md)
- [MVP 리뷰 기능 범위 조정](./troubleshooting/MVP-리뷰-기능-범위-조정.md)

## DB 변경

- 신규 DB는 [schema.sql](./schema.sql)을 기준으로 구성한다.
- 기존 DB의 거래 정합성 제약은 [20260831_trade_integrity_constraints.sql](./migrations/20260831_trade_integrity_constraints.sql)을 적용한다.
- pgvector 사용 전 `CREATE EXTENSION IF NOT EXISTS vector;` 실행 권한을 확인한다.
