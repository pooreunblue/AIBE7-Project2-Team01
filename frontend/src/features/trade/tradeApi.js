import { apiRequest } from "../../api/api.js";

// 채팅방 하위에 거래 생성 (거래는 반드시 채팅을 거쳐야 한다는 정책 반영)
export async function createTrade(chatRoomId, { amount, requestPostId, talentPostId }) {
  const response = await apiRequest(`/chatrooms/${chatRoomId}/trades`, {
    method: "POST",
    body: JSON.stringify({ amount, requestPostId, talentPostId }),
  });
  return unwrapApiResponse(response);
}

export async function fetchTrade(tradeId) {
  const response = await apiRequest(`/trades/${tradeId}`);
  return unwrapApiResponse(response);
}

export async function fetchMyTrades({ page = 0, size = 20 } = {}) {
  const response = await apiRequest(`/trades?page=${page}&size=${size}`);
  return unwrapApiResponse(response);
}

// 지갑에서 차감하고 거래를 완료 상태로 전환
export async function payTrade(tradeId) {
  const response = await apiRequest(`/trades/${tradeId}/pay`, { method: "POST" });
  return unwrapApiResponse(response);
}

export async function cancelTrade(tradeId) {
  const response = await apiRequest(`/trades/${tradeId}/cancel`, { method: "PATCH" });
  return unwrapApiResponse(response);
}

function unwrapApiResponse(response) {
  return response?.data ?? response;
}
