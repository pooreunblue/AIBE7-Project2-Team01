import { apiRequest } from "../../api/api.js";

export async function getMyPage() {
  const response = await apiRequest("/users/me");
  return response.data || response;
}
