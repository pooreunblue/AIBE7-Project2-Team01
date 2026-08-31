import { apiRequest } from "../../api/api.js";

export async function fetchTalents(keyword = "") {
  const page = await fetchTalentPage({ keyword });
  return page.content;
}

export async function fetchTalentPage({ keyword = "", page = 0, size = 20, conditions = {} } = {}) {
  const query = keyword.trim();
  const params = new URLSearchParams({
    page: String(page),
    size: String(size),
    sort: "createdAt,desc",
  });
  if (query) params.set("keyword", query);
  setIfPresent(params, "categoryId", conditions.categoryId);
  setIfPresent(params, "maxPrice", conditions.maxPrice);
  setIfPresent(params, "maxEstimatedDuration", conditions.maxEstimatedDuration);
  setIfPresent(params, "durationUnit", conditions.durationUnit);

  const path = query ? "/talents/search" : "/talents";
  const response = await apiRequest(`${path}?${params}`);
  const data = unwrapApiResponse(response);
  return normalizePage(data);
}

function setIfPresent(params, name, value) {
  if (value !== null && value !== undefined && value !== "") params.set(name, String(value));
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

export async function generateTalentPost(payload, image) {
  const formData = new FormData();
  formData.append("data", new Blob([JSON.stringify(payload)], { type: "application/json" }));
  if (image) formData.append("image", image);
  const response = await apiRequest("/ai/generation/talents", { method: "POST", body: formData });
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

function normalizePage(data) {
  if (Array.isArray(data)) {
    return { content: data, page: 0, totalPages: 1, first: true, last: true };
  }

  return {
    ...data,
    content: data?.content || [],
    page: data?.number ?? data?.page ?? 0,
    totalPages: data?.totalPages ?? 0,
    first: data?.first ?? true,
    last: data?.last ?? true,
  };
}
