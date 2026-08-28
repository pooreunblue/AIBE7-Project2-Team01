# API 명세

- OpenAPI: `v0`
- OAS: `3.1`
- Server: `http://localhost:8080`
- API 문서: `/v3/api-docs`

## Auth

| Method | URL | 설명 |
| --- | --- | --- |
| POST | `/auth/login` | 로그인 |
| POST | `/auth/refresh` | 액세스 토큰 재발급 |
| POST | `/auth/logout` | 로그아웃 |

## User

| Method | URL | 설명 |
| --- | --- | --- |
| POST | `/users/signup` | 회원가입 |
| GET | `/users/me` | 마이페이지 조회 |
| PATCH | `/users/me` | 내 정보 수정 |
| DELETE | `/users/me` | 회원 탈퇴 |
| PATCH | `/users/me/profile-image` | 프로필 이미지 수정 |

## Category

| Method | URL | 설명 |
| --- | --- | --- |
| GET | `/categories` | 카테고리 목록 |

## Talent Post

| Method | URL | 설명 |
| --- | --- | --- |
| GET | `/talents` | 재능글 목록 조회 |
| GET | `/talents/{talentPostId}` | 재능글 상세 조회 |
| POST | `/talents` | 재능글 등록 |
| PUT | `/talents/{talentPostId}` | 재능글 수정 |
| DELETE | `/talents/{talentPostId}` | 재능글 삭제 |
| GET | `/talents/search` | 재능글 검색 |
| PATCH | `/talents/{talentPostId}/inactive` | 재능글 비활성화 |

## Talent Post File

| Method | URL | 설명 |
| --- | --- | --- |
| GET | `/talents/{talentPostId}/files` | 재능글 파일 목록 조회 |
| POST | `/talents/{talentPostId}/files` | 재능글 파일 업로드 |
| DELETE | `/talents/{talentPostId}/files/{fileId}` | 재능글 파일 삭제 |
| PATCH | `/talents/{talentPostId}/files/{fileId}` | 재능글 파일 교체 |
| PATCH | `/talents/{talentPostId}/files/{fileId}/thumbnail` | 재능글 대표 이미지 지정 |

## Request Post

| Method | URL | 설명 |
| --- | --- | --- |
| GET | `/requests` | 의뢰글 목록 조회 |
| GET | `/requests/{requestPostId}` | 의뢰글 상세 조회 |
| POST | `/requests` | 의뢰글 등록 |
| PUT | `/requests/{requestPostId}` | 의뢰글 수정 |
| DELETE | `/requests/{requestPostId}` | 의뢰글 삭제 |
| GET | `/requests/search` | 의뢰글 검색 |
| PATCH | `/requests/{requestPostId}/close` | 의뢰글 마감 |

## Request Post File

| Method | URL | 설명 |
| --- | --- | --- |
| GET | `/requests/{requestPostId}/files` | 의뢰글 파일 목록 조회 |
| POST | `/requests/{requestPostId}/files` | 의뢰글 파일 업로드 |
| DELETE | `/requests/{requestPostId}/files/{fileId}` | 의뢰글 파일 삭제 |
| PATCH | `/requests/{requestPostId}/files/{fileId}` | 의뢰글 파일 교체 |
| PATCH | `/requests/{requestPostId}/files/{fileId}/thumbnail` | 의뢰글 대표 이미지 지정 |

## Portfolio

| Method | URL | 설명 |
| --- | --- | --- |
| POST | `/portfolios` | 포트폴리오 등록 |
| GET | `/portfolios/{portfolioId}` | 포트폴리오 상세 |
| PATCH | `/portfolios/{portfolioId}` | 포트폴리오 수정 |
| DELETE | `/portfolios/{portfolioId}` | 포트폴리오 삭제 |
| GET | `/users/{userId}/portfolios` | 사용자 포트폴리오 목록 |
| GET | `/users/me/portfolios` | 내 포트폴리오 목록 |

## Portfolio File

| Method | URL | 설명 |
| --- | --- | --- |
| GET | `/portfolios/{portfolioId}/files` | 포트폴리오 파일 목록 조회 |
| POST | `/portfolios/{portfolioId}/files` | 포트폴리오 파일 업로드 |
| DELETE | `/portfolios/{portfolioId}/files/{fileId}` | 포트폴리오 파일 삭제 |
| PATCH | `/portfolios/{portfolioId}/files/{fileId}` | 포트폴리오 파일 교체 |
| PATCH | `/portfolios/{portfolioId}/files/{fileId}/thumbnail` | 포트폴리오 대표 이미지 지정 |

## Wallet

| Method | URL | 설명 |
| --- | --- | --- |
| POST | `/wallet/charge` | 지갑 충전 |
| GET | `/wallet` | 내 지갑 조회 |
| GET | `/wallet/transactions` | 지갑 거래 내역 조회 |

## Trade

| Method | URL | 설명 |
| --- | --- | --- |
| POST | `/trades/{tradeId}/pay` | 거래 결제 |
| PATCH | `/trades/{tradeId}/complete` | 거래 완료 |
| PATCH | `/trades/{tradeId}/cancel` | 거래 취소 |
| GET | `/trades` | 거래 목록 조회 |
| GET | `/trades/{tradeId}` | 거래 상세 조회 |

## Chat Room

| Method | URL | 설명 |
| --- | --- | --- |
| GET | `/chatrooms` | 내 채팅방 목록 조회 |
| POST | `/chatrooms` | 채팅방 생성 |
| POST | `/chatrooms/{chatRoomId}/trades` | 채팅방 거래 생성 |
| POST | `/chatrooms/{chatRoomId}/images` | 채팅 이미지 전송 |
| GET | `/chatrooms/{id}` | 채팅 메시지 목록 조회 |
| DELETE | `/chatrooms/{id}` | 채팅방 삭제 |

## Upload

| Method | URL | 설명 |
| --- | --- | --- |
| POST | `/uploads/temp` | 임시 파일 업로드 |

## Health

| Method | URL | 설명 |
| --- | --- | --- |
| GET | `/health` | 서버 상태 확인 |
