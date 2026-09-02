export const API_BASE_URL = window.__API_BASE_URL__ || "/api";
export const WS_BASE_URL = window.__WS_BASE_URL__ || API_BASE_URL;

export function buildApiUrl(path) {
  if (/^https?:\/\//.test(path)) {
    return path;
  }

  return `${API_BASE_URL}${path.startsWith("/") ? path : `/${path}`}`;
}
