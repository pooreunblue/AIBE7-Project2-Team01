import { apiRequest } from "../../api/api.js";

export async function searchAiMatches(payload) {
  const response = await apiRequest("/ai/matches", {
    method: "POST",
    body: JSON.stringify(payload),
  });

  return response?.data ?? response;
}
