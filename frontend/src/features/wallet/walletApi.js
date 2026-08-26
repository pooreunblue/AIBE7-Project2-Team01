import { apiRequest } from "../../api/api.js";

export async function fetchWallet() {
  const response = await apiRequest("/wallet");
  return response.data || response;
}

export async function chargeWallet(amount) {
  const response = await apiRequest("/wallet/charge", {
    method: "POST",
    body: JSON.stringify({ amount }),
  });

  return response.data || response;
}
