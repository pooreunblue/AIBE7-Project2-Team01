import { apiRequest } from "../../api/api.js";
import {
  removeAccessToken,
  removeRefreshToken,
  setAccessToken,
  setRefreshToken,
} from "../../auth/tokenStorage.js";

export async function login(credentials) {
  const response = await apiRequest("/auth/login", {
    method: "POST",
    body: JSON.stringify(credentials),
    skipAuthRefresh: true,
  });
  const data = response.data || response;

  setAccessToken(data.accessToken);
  setRefreshToken(data.refreshToken);
  return data;
}

export async function signup(payload) {
  const response = await apiRequest("/users/signup", {
    method: "POST",
    body: JSON.stringify(payload),
    skipAuthRefresh: true,
  });
  return response.data || response;
}

export async function logout() {
  try {
    await apiRequest("/auth/logout", {
      method: "POST",
      skipAuthRefresh: true,
    });
  } finally {
    removeAccessToken();
    removeRefreshToken();
  }
}
