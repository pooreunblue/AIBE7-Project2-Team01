const API_BASE_URL = window.__API_BASE_URL__ || "/api";
const CSRF_COOKIE_NAME = "XSRF-TOKEN";
const CSRF_HEADER_NAME = "X-XSRF-TOKEN";
const SAFE_METHODS = new Set(["GET", "HEAD", "OPTIONS", "TRACE"]);

let csrfRequest = null;

export async function apiRequest(path, options = {}) {
  return request(path, options, false);
}

async function request(path, options, hasRetried) {
  const requestOptions = await buildOptions(options);
  const response = await fetch(buildUrl(path), requestOptions);

  if (response.status !== 401 || hasRetried || options.skipAuthRefresh) {
    return parseResponse(response);
  }

  const refreshed = await refreshAccessToken();
  if (!refreshed) {
    if (!options.authOptional) {
      redirectToLogin();
    }
    throw new Error("Authentication expired.");
  }

  const retryOptions = await buildOptions(options);
  const retryResponse = await fetch(buildUrl(path), retryOptions);
  return parseResponse(retryResponse);
}

function buildUrl(path) {
  if (/^https?:\/\//.test(path)) {
    return path;
  }

  return `${API_BASE_URL}${path.startsWith("/") ? path : `/${path}`}`;
}

async function buildOptions(options) {
  const headers = new Headers(options.headers || {});
  const method = (options.method || "GET").toUpperCase();

  if (options.body && !(options.body instanceof FormData) && !headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }

  if (!SAFE_METHODS.has(method) && !headers.has(CSRF_HEADER_NAME)) {
    const csrfToken = await getCsrfToken();
    headers.set(CSRF_HEADER_NAME, csrfToken);
  }

  return {
    ...options,
    credentials: options.credentials || "include",
    headers,
  };
}

async function refreshAccessToken() {
  const csrfToken = await getCsrfToken();
  const response = await fetch(buildUrl("/auth/refresh"), {
    method: "POST",
    credentials: "include",
    headers: {
      [CSRF_HEADER_NAME]: csrfToken,
    },
  });

  if (!response.ok) {
    return false;
  }

  return true;
}

async function getCsrfToken() {
  const currentToken = readCookie(CSRF_COOKIE_NAME);
  if (currentToken) {
    return currentToken;
  }

  if (!csrfRequest) {
    csrfRequest = requestCsrfCookie().finally(() => {
      csrfRequest = null;
    });
  }

  return csrfRequest;
}

async function requestCsrfCookie() {
  const response = await fetch(buildUrl("/auth/csrf"), {
    method: "GET",
    credentials: "include",
  });

  if (!response.ok) {
    throw new Error("CSRF token request failed.");
  }

  const token = readCookie(CSRF_COOKIE_NAME);
  if (!token) {
    throw new Error("CSRF token cookie was not created.");
  }

  return token;
}

function readCookie(name) {
  const prefix = `${encodeURIComponent(name)}=`;
  const cookies = document.cookie.split(";");

  for (const cookie of cookies) {
    const value = cookie.trim();
    if (value.startsWith(prefix)) {
      return decodeURIComponent(value.slice(prefix.length));
    }
  }

  return null;
}

async function parseResponse(response) {
  if (response.status === 204) {
    return null;
  }

  const contentType = response.headers.get("Content-Type") || "";
  const data = contentType.includes("application/json")
    ? await response.json()
    : await response.text();

  if (!response.ok) {
    const message = data?.message || response.statusText || "API request failed.";
    const error = new Error(message);
    error.status = response.status;
    error.data = data;
    throw error;
  }

  return data;
}

function redirectToLogin() {
  window.location.href = "login.html";
}
