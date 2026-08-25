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

  setAccessToken(response.accessToken);
  setRefreshToken(response.refreshToken);
  return response;
}

export async function logout() {
  await apiRequest("/auth/logout", {
    method: "POST",
    skipAuthRefresh: true,
  });

  removeAccessToken();
  removeRefreshToken();
}
