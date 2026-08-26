import { apiRequest } from "../../api/api.js";

export async function fetchRequests(keyword = "") {
  const query = keyword.trim();
  const response = await apiRequest(query ? `/requests/search?keyword=${encodeURIComponent(query)}` : "/requests");
  const data = unwrapApiResponse(response);
  return Array.isArray(data) ? data : data.content || [];
}

export async function fetchRequest(requestPostId) {
  const response = await apiRequest(`/requests/${requestPostId}`);
  return unwrapApiResponse(response);
}

export async function createRequest(payload) {
  const response = await apiRequest("/requests", {
    method: "POST",
    body: JSON.stringify(payload),
  });
  return unwrapApiResponse(response);
}

function unwrapApiResponse(response) {
  return response?.data ?? response;
}
