import {
  getAccessToken,
  getRefreshToken,
  removeAccessToken,
  removeRefreshToken,
  setAccessToken,
  setRefreshToken,
} from "../auth/tokenStorage.js";

const API_BASE_URL = window.__API_BASE_URL__ || "/api";

export async function apiRequest(path, options = {}) {
  return request(path, options, false);
}

async function request(path, options, hasRetried) {
  const response = await fetch(buildUrl(path), buildOptions(options));

  if (response.status !== 401 || hasRetried || options.skipAuthRefresh) {
    return parseResponse(response);
  }

  const refreshed = await refreshAccessToken();
  if (!refreshed) {
    clearTokensAndRedirect();
    throw new Error("Authentication expired.");
  }

  const retryResponse = await fetch(buildUrl(path), buildOptions(options));
  return parseResponse(retryResponse);
}

function buildUrl(path) {
  if (/^https?:\/\//.test(path)) {
    return path;
  }

  return `${API_BASE_URL}${path.startsWith("/") ? path : `/${path}`}`;
}

function buildOptions(options) {
  const headers = new Headers(options.headers || {});
  const accessToken = getAccessToken();

  if (accessToken) {
    headers.set("Authorization", `Bearer ${accessToken}`);
  }

  if (options.body && !headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }

  return {
    ...options,
    headers,
  };
}

async function refreshAccessToken() {
  const refreshToken = getRefreshToken();
  if (!refreshToken) {
    return false;
  }

  const response = await fetch(buildUrl("/auth/refresh"), {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ refreshToken }),
  });

  if (!response.ok) {
    return false;
  }

  const responseData = await response.json();
  const data = responseData.data || responseData;
  if (!data.accessToken) {
    return false;
  }

  setAccessToken(data.accessToken);
  setRefreshToken(data.refreshToken || refreshToken);
  return true;
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

function clearTokensAndRedirect() {
  removeAccessToken();
  removeRefreshToken();
  window.location.href = "login.html";
}
