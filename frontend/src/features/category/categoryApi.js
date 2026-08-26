import { apiRequest } from "../../api/api.js";

export async function fetchCategories() {
  const response = await apiRequest("/categories");
  return response?.data ?? response;
}
