import { apiRequest } from "../../api/api.js";

export async function analyzeAiMatchQuery(query) {
  const response = await apiRequest("/ai/matches/analyze", {
    method: "POST",
    body: JSON.stringify({ query }),
  });
  return unwrapApiResponse(response);
}

export async function searchAiMatches(payload) {
  const response = await apiRequest("/ai/matches", {
    method: "POST",
    body: JSON.stringify(payload),
  });
  return unwrapApiResponse(response);
}

function unwrapApiResponse(response) {
  return response?.data ?? response;
}
