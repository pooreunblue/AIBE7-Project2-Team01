## 회원

| Method | URL | 설명 |
| --- | --- | --- |
| POST | /signup | 회원가입 |
| POST | /login | 로그인 |
| GET | /member | 내 정보 조회 |
| PATCH | /member | 회원 정보 수정 |

---

## 재능

| Method | URL | 설명 |
| --- | --- | --- |
| GET | /talents | 재능 목록 조회 |
| GET | /talents/{id} | 재능 상세 조회 |
| POST | /talents | 재능 등록 |
| PATCH | /talents/{id} | 재능 수정 |
| DELETE | /talents/{id} | 재능 삭제 |

---

## 구매 요청

| Method | URL | 설명 |
| --- | --- | --- |
| GET | /requests | 구매 요청 목록 조회 |
| GET | /requests/{id} | 구매 요청 상세 조회 |
| POST | /requests | 구매 요청 등록 |
| PATCH | /requests/{id} | 구매 요청 수정 |
| DELETE | /requests/{id} | 구매 요청 삭제 |

---

## 포트폴리오

| Method | URL | 설명 |
| --- | --- | --- |
| GET | /portfolios/{id} | 포트폴리오 조회 |
| POST | /portfolios | 포트폴리오 등록 |
| PATCH | /portfolios/{id} | 포트폴리오 수정 |
| DELETE | /portfolios/{id} | 포트폴리오 삭제 |

---

## 카테고리

| Method | URL | 설명 |
| --- | --- | --- |
| GET | /categories | 카테고리 조회 |
| GET | /categories/{id} | 카테고리별 조회 |

---

## 검색

| Method | URL | 설명 |
| --- | --- | --- |
| GET | /search | 키워드 검색 |
| POST | /search/ai | AI 검색 |

---

## 채팅

| Method | URL | 설명 |
| --- | --- | --- |
| GET | /chatrooms | 채팅방 목록 |
| POST | /chatrooms | 채팅방 생성 |
| GET | /chatrooms/{id} | 채팅 내역 조회 |

### WebSocket

- `/ws`
- `/chat/{roomId}`

---

## 거래

| Method | URL | 설명 |
| --- | --- | --- |
| POST | /trades | 거래 요청 |
| PATCH | /trades/{id} | 거래 상태 변경 |
| GET | /trades/{id} | 거래 조회 |

---

## 결제

| Method | URL | 설명 |
| --- | --- | --- |
| POST | /payments | 결제 요청 |
| POST | /payments/confirm | 결제 승인 |
| GET | /payments/{id} | 결제 조회 |

---

## 리뷰

| Method | URL | 설명 |
| --- | --- | --- |
| POST | /reviews | 리뷰 작성 |
| PATCH | /reviews/{id} | 리뷰 수정 |
| DELETE | /reviews/{id} | 리뷰 삭제 |
| GET | /reviews/{id} | 리뷰 조회 |