import { apiRequest } from "./api.js";

export async function uploadTempImage(file) {
  const formData = new FormData();
  formData.append("file", file);

  const response = await apiRequest("/uploads/temp", {
    method: "POST",
    body: formData,
  });
  return response.data || response;
}
