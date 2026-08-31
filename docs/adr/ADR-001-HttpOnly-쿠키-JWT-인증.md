# ADR-001: JWT 인증 토큰을 HttpOnly Cookie로 관리

- 상태: 승인됨 (Accepted)
- 결정일: 2026-08-29
- 적용 범위: 웹 인증, 토큰 재발급, OAuth2 로그인, HTTP API, WebSocket 연결

## 배경

우리 서비스는 웹 브라우저 기반이며, 프론트엔드 JavaScript가 Access Token과 Refresh Token 값을 직접 읽거나 관리할 필요가 낮다.

토큰을 `localStorage` 또는 JavaScript에서 접근할 수 있는 저장소에 보관하면 XSS 취약점 발생 시 토큰 값이 외부로 유출될 가능성이 커진다. 반면 쿠키 기반 인증은 브라우저가 요청마다 쿠키를 자동 전송하므로 프론트엔드의 토큰 저장 및 첨부 로직을 단순화할 수 있지만, 별도의 방어가 없다면 CSRF 공격에 노출될 수 있다.

따라서 토큰 탈취 위험과 CSRF 위험을 함께 고려한 인증 저장 방식을 결정해야 한다.

## 결정

Access Token과 Refresh Token을 `HttpOnly Cookie`로 관리한다.

- 인증 쿠키에 `HttpOnly=true`를 적용하여 JavaScript에서 토큰 값을 직접 읽지 못하게 한다.
- 인증 쿠키에 `SameSite=Lax`를 적용하여 교차 사이트 요청에서의 쿠키 전송을 제한한다.
- 로컬 HTTP 환경에서는 `Secure=false`, 운영 HTTPS 환경에서는 `Secure=true`를 사용한다.
- 프론트엔드는 모든 API 요청에 `credentials: include`를 사용한다.
- Access Token 만료 시 Refresh Token 쿠키를 사용해 Access Token을 재발급한다.
- 로그아웃 시 발급 때와 동일한 속성 및 경로로 두 인증 쿠키를 만료시킨다.

Cookie 기반 인증의 CSRF 위험은 Spring Security의 CSRF 보호 기능으로 방어한다.

- `CookieCsrfTokenRepository`를 사용한다.
- 프론트엔드는 `GET /auth/csrf`로 CSRF 토큰 쿠키를 준비한다.
- `POST`, `PUT`, `PATCH`, `DELETE` 요청에는 쿠키의 토큰을 `X-XSRF-TOKEN` 헤더로 전송한다.
- 서버는 CSRF 쿠키와 요청 헤더의 토큰이 일치하는지 검증한다.
- CSRF 토큰은 인증 정보가 아니며 프론트엔드가 헤더로 전달해야 하므로 `XSRF-TOKEN` 쿠키에는 `HttpOnly`를 적용하지 않는다.
- SockJS 내부 전송 경로인 `/ws/**`는 HTTP CSRF 검사에서 제외하되, WebSocket Origin을 허용된 프론트엔드 주소로 제한한다.

환경별 설정은 다음 환경변수로 관리한다.

```env
AUTH_COOKIE_SECURE=false
AUTH_COOKIE_SAME_SITE=Lax
FRONTEND_ORIGIN=http://localhost:3000
```

운영 환경에서는 다음 조건을 지켜야 한다.

```env
AUTH_COOKIE_SECURE=true
AUTH_COOKIE_SAME_SITE=Lax
FRONTEND_ORIGIN=https://서비스-프론트엔드-도메인
```

## 선택 이유

웹 브라우저 기반 서비스이므로 토큰을 클라이언트 JavaScript에서 직접 관리할 필요성이 낮다고 판단했다. Access Token과 Refresh Token을 HttpOnly Cookie로 관리하면 JavaScript에서 토큰 값에 직접 접근할 수 없어 XSS 발생 시 토큰 자체가 탈취되는 위험을 줄일 수 있다.

또한 브라우저의 쿠키 자동 전송을 활용해 프론트엔드의 토큰 저장, 조회 및 Authorization 헤더 관리 복잡도를 낮춘다. 대신 Cookie 기반 인증에서 발생하는 CSRF 위험은 `SameSite`, `Secure`, CSRF 토큰 검증 및 Origin 제한을 함께 적용해 보완한다.

## 검토한 대안

### localStorage에 JWT 저장

- 장점: 구현이 단순하고 Authorization 헤더 사용이 명확하다.
- 단점: JavaScript에서 토큰에 접근할 수 있어 XSS 발생 시 Access Token과 Refresh Token이 직접 유출될 수 있다.
- 결론: 채택하지 않는다.

### JavaScript 메모리에 Access Token 저장

- 장점: 영구 저장소보다 토큰 탈취 범위를 줄일 수 있다.
- 단점: 새로고침 시 인증 복구 흐름이 복잡해지고 Refresh Token 저장 전략이 별도로 필요하다.
- 결론: 현재 프로젝트 규모와 웹 인증 흐름에 비해 복잡도가 높아 채택하지 않는다.

### 서버 세션 기반 인증

- 장점: 토큰 수명과 폐기를 서버에서 통제하기 쉽다.
- 단점: 현재 구축된 JWT, OAuth2, Redis Refresh Token 구조를 변경해야 한다.
- 결론: 기존 인증 구조를 유지하기 위해 채택하지 않는다.

## 결과

### 긍정적 영향

- XSS 발생 시 JavaScript를 통한 인증 토큰 값의 직접 탈취 위험이 줄어든다.
- 프론트엔드가 토큰 값을 저장하거나 Authorization 헤더를 직접 구성하지 않아도 된다.
- 일반 로그인, OAuth2 로그인 및 토큰 재발급이 같은 쿠키 정책을 사용한다.
- CSRF 토큰 검증과 Origin 제한을 통해 쿠키 자동 전송에 따른 위험을 보완한다.

### 부정적 영향 및 주의사항

- HttpOnly는 XSS 자체를 막지 않는다. XSS 코드가 사용자 브라우저에서 인증된 요청을 실행할 위험은 별도의 입력 검증, 출력 인코딩 및 CSP로 방어해야 한다.
- 프론트엔드의 모든 상태 변경 요청은 CSRF 헤더를 포함해야 한다.
- 운영 환경에서 `AUTH_COOKIE_SECURE=true`를 누락하면 HTTPS 쿠키 보호 수준이 낮아진다.
- 프론트와 백엔드가 완전히 다른 사이트로 배포되면 `SameSite=None; Secure` 및 CORS 정책을 별도로 재검토해야 한다.
- Swagger에서 상태 변경 API를 테스트할 때도 먼저 CSRF 토큰을 발급받고 `X-XSRF-TOKEN` 헤더를 설정해야 한다.

## 구현 위치

- 인증 쿠키 생성 및 삭제: `backend/src/main/java/org/example/link/auth/cookie/CookieUtil.java`
- CSRF 및 접근 제어 설정: `backend/src/main/java/org/example/link/auth/config/SecurityConfig.java`
- CSRF 토큰 발급: `backend/src/main/java/org/example/link/auth/controller/AuthController.java`
- 쿠키 환경설정: `backend/src/main/resources/application-auth.yaml`
- 프론트 CSRF 헤더 처리: `frontend/src/api/api.js`
- WebSocket Origin 제한: `backend/src/main/java/org/example/link/domain/chat/websocket/WebSocketConfig.java`

## 후속 규칙

- 인증 토큰을 응답 본문, `localStorage`, `sessionStorage` 또는 일반 JavaScript 변수에 저장하지 않는다.
- 인증 쿠키의 `HttpOnly` 설정을 해제하지 않는다.
- 상태 변경 API를 추가할 때 공통 `apiRequest`를 사용하여 CSRF 처리를 우회하지 않는다.
- 운영 배포 전 `Secure`, `SameSite`, `FRONTEND_ORIGIN` 설정을 점검한다.
- 쿠키 정책 또는 프론트·백엔드 배포 도메인이 변경되면 이 ADR을 다시 검토한다.
