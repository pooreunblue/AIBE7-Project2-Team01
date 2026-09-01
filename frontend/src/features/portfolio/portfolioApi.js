import { apiRequest } from "../../api/api.js";

export async function getMyPortfolios() {
  const response = await apiRequest("/users/me/portfolios");
  return response.data || response;
}

export async function getUserPortfolios(userId) {
  const response = await apiRequest(`/users/public/${userId}/portfolios`, { authOptional: true });
  return response.data || response;
}

export async function createPortfolio(payload) {
  const response = await apiRequest("/portfolios", {
    method: "POST",
    body: JSON.stringify(payload),
  });
  return response.data || response;
}

export async function getPortfolio(portfolioId) {
  const response = await apiRequest(`/portfolios/${portfolioId}`);
  return response.data || response;
}

export async function updatePortfolio(portfolioId, payload) {
  const response = await apiRequest(`/portfolios/${portfolioId}`, {
    method: "PATCH",
    body: JSON.stringify(payload),
  });
  return response.data || response;
}

export async function uploadPortfolioFile(portfolioId, file) {
  const formData = new FormData();
  formData.append("file", file);

  const response = await apiRequest(`/portfolios/${portfolioId}/files`, {
    method: "POST",
    body: formData,
  });
  return response.data || response;
}

export async function getPortfolioFiles(portfolioId) {
  const response = await apiRequest(`/portfolios/${portfolioId}/files`);
  return response.data || response;
}

export async function updatePortfolioFile(portfolioId, fileId, file) {
  const formData = new FormData();
  formData.append("file", file);

  const response = await apiRequest(`/portfolios/${portfolioId}/files/${fileId}`, {
    method: "PATCH",
    body: formData,
  });
  return response.data || response;
}

export async function setPortfolioThumbnail(portfolioId, fileId) {
  const response = await apiRequest(`/portfolios/${portfolioId}/files/${fileId}/thumbnail`, {
    method: "PATCH",
  });
  return response.data || response;
}

export async function deletePortfolioFile(portfolioId, fileId) {
  await apiRequest(`/portfolios/${portfolioId}/files/${fileId}`, {
    method: "DELETE",
  });
}

export async function deletePortfolio(portfolioId) {
  await apiRequest(`/portfolios/${portfolioId}`, {
    method: "DELETE",
  });
}
