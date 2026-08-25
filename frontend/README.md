# TalentPulse Frontend

바닐라 HTML, CSS, JavaScript로 만든 프론트엔드 초안입니다.

## 실행

ES module을 사용하므로 로컬 서버로 실행합니다.

```bash
python3 -m http.server 5173 -d frontend
```

접속 주소는 `http://localhost:5173`입니다.

API 서버는 기본적으로 Express 개발 서버 기준인 `http://localhost:3000`을 바라봅니다.
다른 주소를 사용해야 하면 `index.html`에서 `window.__API_BASE_URL__` 값을 먼저 선언하면 됩니다.

## 구조

```text
frontend/
  index.html
  login.html
  styles.css
  src/
    api/
      api.js
    auth/
      tokenStorage.js
    app.js
    router.js
    features/
      auth/
        authApi.js
        LoginPage.js
      chat/
        ChatPage.js
      home/
        HomePage.js
      payment/
        CheckoutPage.js
      request/
        RequestCreatePage.js
        RequestDetailPage.js
        RequestListPage.js
      search/
        AiSearchPage.js
      talent/
        TalentCreatePage.js
        TalentDetailPage.js
        TalentListPage.js
      user/
        MyPage.js
    shared/
      data/
        mock.js
      ui/
        index.js
```

## 화면

- 로그인
- 메인 홈
- 재능 목록
- 재능 등록
- 재능 상세
- 구매 요청 목록
- 구매 요청 등록
- 구매 요청 상세
- AI 검색 및 결과
- 채팅
- 마이페이지
- 거래 및 결제
