import { apiRequest } from "../../api/api.js";

export async function getMyPortfolios() {
  const response = await apiRequest("/users/me/portfolios");
  return response.data || response;
}

export async function createPortfolio(payload) {
  const response = await apiRequest("/portfolios", {
    method: "POST",
    body: JSON.stringify(payload),
  });
  return response.data || response;
}

export async function deletePortfolio(portfolioId) {
  await apiRequest(`/portfolios/${portfolioId}`, {
    method: "DELETE",
  });
}
