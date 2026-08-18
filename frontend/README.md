# Frontend Draft

AI 기반 양방향 재능 거래 플랫폼의 프론트엔드 초안입니다.

## 실행

정적 파일 기반이므로 `frontend/index.html`을 브라우저에서 열면 됩니다.

로컬 서버로 확인하려면 저장소 루트에서 아래 명령을 실행합니다.

```bash
python3 -m http.server 5173 -d frontend
```

이후 `http://localhost:5173`으로 접속합니다.

## 반영 범위

- 회원가입, 로그인 진입
- 재능 목록, 재능 등록
- 구매 요청 목록, 구매 요청 등록
- 키워드 검색, 카테고리 검색, AI 자연어 검색
- AI 검색 실패 시 키워드 검색 Fallback 표시
- 포트폴리오 관리
- 채팅, 거래 진행, 결제, 리뷰, 북마크 초안

## API 기준

`docs/api.md`의 주요 API 경로를 `src/app.js`의 `apiRoutes`에 반영했습니다.
