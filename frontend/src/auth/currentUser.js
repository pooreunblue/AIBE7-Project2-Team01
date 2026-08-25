import { getAccessToken } from "./tokenStorage.js";

// accessToken의 payload(sub = userId, email)를 디코드해서 재사용.
// 백엔드 JwtProvider.createAccessToken()과 클레임 구조를 맞춰야 함.
export function getCurrentUserId() {
  const claims = decodeAccessToken();
  return claims ? Number(claims.sub) : null;
}

export function getCurrentUserEmail() {
  const claims = decodeAccessToken();
  return claims ? claims.email : null;
}

function decodeAccessToken() {
  const token = getAccessToken();
  if (!token) return null;

  try {
    const payload = token.split(".")[1];
    const json = atob(payload.replace(/-/g, "+").replace(/_/g, "/"));
    return JSON.parse(decodeURIComponent(escape(json)));
  } catch {
    return null;
  }
}
