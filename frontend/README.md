# TalentPulse Frontend

바닐라 HTML, CSS, JavaScript로 만든 프론트엔드 초안입니다.

## 실행

Express가 정적 프론트 파일을 서빙하고 `/api` 요청을 Spring Boot 백엔드로 프록시합니다.

```bash
cd frontend
npm install
npm start
```

접속 주소는 `http://localhost:3000`입니다.

Spring Boot API 서버는 기본적으로 `http://localhost:8080`을 바라봅니다.
다른 주소를 사용해야 하면 Express 실행 시 `API_TARGET` 값을 지정합니다.

```bash
API_TARGET=http://localhost:8081 npm start
```

## 구조

```text
frontend/
  index.html
  login.html
  package.json
  server.js
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
