import DOMPurify from "../../../node_modules/dompurify/dist/purify.es.mjs";

const HTML_SANITIZE_OPTIONS = Object.freeze({
  USE_PROFILES: { html: true },
  FORBID_TAGS: ["script", "style", "iframe", "object", "embed", "template"],
  FORBID_ATTR: ["style", "srcdoc"],
  ALLOW_ARIA_ATTR: true,
  ALLOW_DATA_ATTR: true,
});

/** HTML 템플릿 안의 텍스트와 속성값에 사용할 이스케이프 함수다. */
export function escapeHtml(value) {
  return String(value ?? "").replace(/[&<>"']/g, (character) => (
    { "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[character]
  ));
}

/** innerHTML에 넣기 전 실행 가능한 태그와 속성을 제거한다. */
export function sanitizeHtml(value) {
  return DOMPurify.sanitize(String(value ?? ""), HTML_SANITIZE_OPTIONS);
}

export function setSafeHtml(element, value) {
  if (!element) return;
  element.innerHTML = sanitizeHtml(value);
}

export function appendSafeHtml(element, position, value) {
  if (!element) return;
  element.insertAdjacentHTML(position, sanitizeHtml(value));
}

/** 링크에는 같은 페이지 hash, 상대 경로, http/https 주소만 허용한다. */
export function safeUrl(value) {
  return normalizeUrl(value, { allowBlob: false });
}

/** 이미지에는 http/https와 로컬 미리보기에 필요한 blob 주소만 허용한다. */
export function safeImageUrl(value) {
  return normalizeUrl(value, { allowBlob: true });
}

function normalizeUrl(value, { allowBlob }) {
  const candidate = String(value ?? "").trim();
  if (!candidate || containsControlCharacter(candidate)) {
    return "";
  }

  if (candidate.startsWith("#/")) {
    return candidate;
  }

  try {
    const parsed = new URL(candidate, window.location.origin);
    if (parsed.protocol === "http:" || parsed.protocol === "https:") {
      return parsed.href;
    }
    if (allowBlob && parsed.protocol === "blob:") {
      return parsed.href;
    }
  } catch {
    return "";
  }
  return "";
}

function containsControlCharacter(value) {
  return /[\u0000-\u001F\u007F]/.test(value);
}
