import { button, pageTitle } from "../../shared/ui/index.js";
import { escapeHtml } from "../../shared/security/xss.js";

// 상태 코드별 안내 문구. 정의되지 않은 코드는 4xx/5xx 그룹 기본값으로 처리한다.
const PRESETS = {
  400: {
    kicker: "400 Bad Request",
    title: "잘못된 요청이에요",
    copy: "입력한 내용이나 주소를 다시 확인해 주세요.",
  },
  401: {
    kicker: "401 Unauthorized",
    title: "로그인이 필요해요",
    copy: "이 페이지를 보려면 먼저 로그인해 주세요.",
  },
  403: {
    kicker: "403 Forbidden",
    title: "접근 권한이 없어요",
    copy: "이 페이지에 접근할 수 있는 권한이 없습니다.",
  },
  404: {
    kicker: "404 Not Found",
    title: "페이지를 찾을 수 없어요",
    copy: "주소가 바뀌었거나 삭제된 페이지일 수 있습니다.",
  },
  409: {
    kicker: "409 Conflict",
    title: "요청이 충돌했어요",
    copy: "이미 처리된 요청이거나 다른 작업과 겹쳤습니다. 잠시 후 다시 시도해 주세요.",
  },
  500: {
    kicker: "500 Server Error",
    title: "서버에 문제가 발생했어요",
    copy: "일시적인 오류일 수 있습니다. 잠시 후 다시 시도해 주세요.",
  },
  502: {
    kicker: "502 Bad Gateway",
    title: "서버에 연결하지 못했어요",
    copy: "서버가 응답하지 않습니다. 잠시 후 다시 시도해 주세요.",
  },
  503: {
    kicker: "503 Service Unavailable",
    title: "지금은 서비스를 이용할 수 없어요",
    copy: "서버 점검 중이거나 일시적으로 접속이 몰렸습니다. 잠시 후 다시 시도해 주세요.",
  },
};

function resolvePreset(status) {
  if (PRESETS[status]) {
    return PRESETS[status];
  }
  if (status >= 500) {
    return { ...PRESETS[500], kicker: `${status} Server Error` };
  }
  if (status >= 400) {
    return { ...PRESETS[400], kicker: `${status} Bad Request` };
  }
  return PRESETS[500];
}

/**
 * 400/500번대 예외 화면. 라우트가 없을 때(404)와 API 요청 실패 시 공통으로 사용한다.
 *
 * @param {number} [status=500] HTTP 상태 코드
 * @param {string} [detail=""]  서버가 내려준 message 등 추가 설명 (선택)
 */
export function ErrorPage(status = 500, detail = "") {
  const code = Number(status) || 500;
  const preset = resolvePreset(code);
  const showLoginAction = code === 401 || code === 403;

  return `
    <section class="hero error-page" data-error-status="${escapeHtml(String(code))}">
      <p class="error-code" aria-hidden="true">${escapeHtml(String(code))}</p>
      ${pageTitle(preset.kicker, preset.title, preset.copy)}
      ${detail ? `<p class="error-detail">${escapeHtml(detail)}</p>` : ""}
      <div class="error-actions">
        ${button("홈으로", "#/home", "primary")}
        ${showLoginAction ? button("로그인", "#/login", "quiet") : ""}
        <button class="button quiet" type="button" data-error-back>이전 페이지</button>
      </div>
    </section>
  `;
}

/** 라우트가 존재하지 않을 때 렌더링하는 404 화면. */
export function NotFoundPage() {
  return ErrorPage(404);
}

/**
 * 페이지 내부에서 API 호출이 실패했을 때 컨테이너에 넣을 예외 마크업.
 * `error.status`가 있으면 해당 코드로, 없으면 500으로 처리한다.
 *
 * @example
 * try { ... } catch (error) { setSafeHtml(container, errorState(error)); }
 */
export function errorState(error) {
  const status = Number(error?.status) || 500;
  const detail = typeof error?.message === "string" ? error.message : "";
  return ErrorPage(status, detail);
}
