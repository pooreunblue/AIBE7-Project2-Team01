import { apiRequest } from "../../api/api.js";

export async function fetchTalents(keyword = "") {
  const query = keyword.trim();
  const response = await apiRequest(query ? `/talents/search?keyword=${encodeURIComponent(query)}` : "/talents");
  const data = unwrapApiResponse(response);
  return Array.isArray(data) ? data : data.content || [];
}

export async function fetchTalent(talentPostId) {
  const response = await apiRequest(`/talents/${talentPostId}`);
  return unwrapApiResponse(response);
}

export async function createTalent(payload) {
  const response = await apiRequest("/talents", {
    method: "POST",
    body: JSON.stringify(payload),
  });
  return unwrapApiResponse(response);
}

export async function updateTalent(talentPostId, payload) {
  const response = await apiRequest(`/talents/${talentPostId}`, {
    method: "PUT",
    body: JSON.stringify(payload),
  });
  return unwrapApiResponse(response);
}

export async function deleteTalent(talentPostId) {
  await apiRequest(`/talents/${talentPostId}`, { method: "DELETE" });
}

export async function inactiveTalent(talentPostId) {
  const response = await apiRequest(`/talents/${talentPostId}/inactive`, { method: "PATCH" });
  return unwrapApiResponse(response);
}

export async function uploadTalentFile(talentPostId, file) {
  const formData = new FormData();
  formData.append("file", file);

  const response = await apiRequest(`/talents/${talentPostId}/files`, {
    method: "POST",
    body: formData,
  });
  return unwrapApiResponse(response);
}

export async function getTalentFiles(talentPostId) {
  const response = await apiRequest(`/talents/${talentPostId}/files`);
  return unwrapApiResponse(response);
}

export async function updateTalentFile(talentPostId, fileId, file) {
  const formData = new FormData();
  formData.append("file", file);

  const response = await apiRequest(`/talents/${talentPostId}/files/${fileId}`, {
    method: "PATCH",
    body: formData,
  });
  return unwrapApiResponse(response);
}

export async function setTalentThumbnail(talentPostId, fileId) {
  const response = await apiRequest(`/talents/${talentPostId}/files/${fileId}/thumbnail`, {
    method: "PATCH",
  });
  return unwrapApiResponse(response);
}

export async function deleteTalentFile(talentPostId, fileId) {
  await apiRequest(`/talents/${talentPostId}/files/${fileId}`, { method: "DELETE" });
}

function unwrapApiResponse(response) {
  return response?.data ?? response;
}
