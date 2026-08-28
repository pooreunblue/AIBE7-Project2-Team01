import { apiRequest } from "../../api/api.js";

// 내 채팅방 목록 (백엔드: GET /chatrooms)
export function fetchMyChatRooms() {
  return apiRequest("/chatrooms").then(unwrapApiResponse);
}

// 특정 채팅방의 메시지 이력 (백엔드: GET /chatrooms/{id}?page=&size=)
// 서버는 최신순(desc)으로 내려주므로 화면에 그릴 땐 뒤집어서(오래된 순) 써야 함.
export function fetchChatMessages(chatRoomId, { page = 0, size = 50 } = {}) {
  return apiRequest(`/chatrooms/${chatRoomId}?page=${page}&size=${size}`).then(unwrapApiResponse);
}

/**
 * 채팅방 신청/입장. 이미 같은 게시글+상대방으로 만든 방이 있으면 서버가 그 방을 그대로 돌려줌(중복 생성 안 됨).
 * 게시글 상세 페이지의 "채팅 신청" 버튼에서 이 함수만 호출하면 됨.
 *
 * requestPostId, talentPostId 둘 중 하나만 넘겨야 함 (판매글이면 talentPostId, 요청글이면 requestPostId).
 */
export function createOrGetChatRoom({ requestPostId, talentPostId, otherUserId }) {
  return apiRequest("/chatrooms", {
    method: "POST",
    body: JSON.stringify({ requestPostId, talentPostId, otherUserId }),
  }).then(unwrapApiResponse);
}

/**
 * 채팅방에 이미지 전송 (백엔드: POST /chatrooms/{id}/images, multipart).
 * 서버가 업로드 후 STOMP로 메시지를 브로드캐스트하므로, 반환값을 화면에 직접 그릴 필요는 없다
 * (구독 중인 onMessage 로 도착함).
 */
export function uploadChatImage(chatRoomId, file) {
  const formData = new FormData();
  formData.append("file", file);
  return apiRequest(`/chatrooms/${chatRoomId}/images`, {
    method: "POST",
    body: formData,
  }).then(unwrapApiResponse);
}

export function requestTradeAmount(chatRoomId) {
  return apiRequest(`/chatrooms/${chatRoomId}/trade-amount-request`, {
    method: "POST",
  }).then(unwrapApiResponse);
}

function unwrapApiResponse(response) {
  return response?.data ?? response;
}

// 채팅방 나가기. 내 참가 기록만 지워지고 상대방 쪽 대화는 그대로 남음(서버에서 처리).
export function leaveChatRoom(chatRoomId) {
  return apiRequest(`/chatrooms/${chatRoomId}`, { method: "DELETE" });
}
