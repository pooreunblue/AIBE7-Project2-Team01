import { createOrGetChatRoom } from "./chatApi.js";

/**
 * 게시글 상세 페이지의 "채팅 신청" / "채팅하기" 버튼에서 그대로 호출하면 됨.
 * 이미 같은 게시글+상대방으로 만든 채팅방이 있으면 새로 만들지 않고 그 방을 그대로 열어줌.
 *
 * 판매글(talent)이면 talentPostId, 요청글(request)이면 requestPostId를 넘기고 (둘 중 하나만),
 * otherUserId는 그 게시글 작성자의 user_id.
 *
 * 사용 예:
 *   import { startChat } from "../../features/chat/startChat.js";
 *
 *   button.addEventListener("click", () => {
 *     startChat({ talentPostId: post.talentPostId, otherUserId: post.userId });
 *   });
 */
export async function startChat({ requestPostId, talentPostId, otherUserId }) {
  const room = await createOrGetChatRoom({ requestPostId, talentPostId, otherUserId });
  window.location.hash = `/chat/${room.chatRoomId}`;
  return room;
}
