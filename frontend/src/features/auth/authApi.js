import { apiRequest } from "../../api/api.js";
import { clearCurrentUser, getCurrentUser } from "../../auth/currentUser.js";

export async function login(credentials) {
  await apiRequest("/auth/login", {
    method: "POST",
    body: JSON.stringify(credentials),
    skipAuthRefresh: true,
  });
  return getCurrentUser({ force: true });
}

export async function signup(payload) {
  const formData = new FormData();
  const profileImage = payload.profileImage;
  formData.append(
    "request",
    new Blob([
      JSON.stringify({
        email: payload.email,
        password: payload.password,
        nickname: payload.nickname,
      }),
    ], { type: "application/json" })
  );

  if (profileImage) {
    formData.append("profileImage", profileImage);
  }

  const response = await apiRequest("/users/signup", {
    method: "POST",
    body: formData,
    skipAuthRefresh: true,
  });
  return response.data || response;
}

export async function logout() {
  try {
    await apiRequest("/auth/logout", {
      method: "POST",
      authOptional: true,
    });
  } finally {
    clearCurrentUser();
  }
}
