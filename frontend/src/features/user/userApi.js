import { apiRequest } from "../../api/api.js";

export async function getMyPage() {
  const response = await apiRequest("/users/me");
  return response.data || response;
}

export async function getPublicUser(userId) {
  const response = await apiRequest(`/users/public/${userId}`, { authOptional: true });
  return response.data || response;
}

export async function updateMyPage(payload) {
  const response = await apiRequest("/users/me", {
    method: "PATCH",
    body: JSON.stringify(payload),
  });
  return response.data || response;
}

export async function updateProfileImage(file) {
  const formData = new FormData();
  formData.append("file", file);

  const response = await apiRequest("/users/me/profile-image", {
    method: "PATCH",
    body: formData,
  });
  return response.data || response;
}
