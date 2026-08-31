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

export async function generateRequestPost(payload, image) {
  const formData = new FormData();
  formData.append("data", new Blob([JSON.stringify(payload)], { type: "application/json" }));
  if (image) formData.append("image", image);
  const response = await apiRequest("/ai/generation/requests", { method: "POST", body: formData });
  return unwrapApiResponse(response);
}

export async function updateRequest(requestPostId, payload) {
  const response = await apiRequest(`/requests/${requestPostId}`, {
    method: "PUT",
    body: JSON.stringify(payload),
  });
  return unwrapApiResponse(response);
}

export async function uploadRequestFile(requestPostId, file) {
  const formData = new FormData();
  formData.append("file", file);

  const response = await apiRequest(`/requests/${requestPostId}/files`, {
    method: "POST",
    body: formData,
  });
  return unwrapApiResponse(response);
}

export async function getRequestFiles(requestPostId) {
  const response = await apiRequest(`/requests/${requestPostId}/files`);
  return unwrapApiResponse(response);
}

export async function updateRequestFile(requestPostId, fileId, file) {
  const formData = new FormData();
  formData.append("file", file);

  const response = await apiRequest(`/requests/${requestPostId}/files/${fileId}`, {
    method: "PATCH",
    body: formData,
  });
  return unwrapApiResponse(response);
}

export async function setRequestThumbnail(requestPostId, fileId) {
  const response = await apiRequest(`/requests/${requestPostId}/files/${fileId}/thumbnail`, {
    method: "PATCH",
  });
  return unwrapApiResponse(response);
}

export async function deleteRequestFile(requestPostId, fileId) {
  await apiRequest(`/requests/${requestPostId}/files/${fileId}`, { method: "DELETE" });
}

function unwrapApiResponse(response) {
  return response?.data ?? response;
}
