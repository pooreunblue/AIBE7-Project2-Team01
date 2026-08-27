import { button } from "../../shared/ui/index.js";
import { getCurrentUserId } from "../../auth/currentUser.js";
import { fetchMyChatRooms, fetchChatMessages, leaveChatRoom } from "./chatApi.js";
import { connectChatRoom, sendChatMessage } from "./chatSocket.js";

let activeClient = null;

// 채팅 목록 페이지(#/chat) 겸 채팅방 페이지(#/chat/{id}).
// 실제 데이터는 initChatPage()가 렌더링 후 비동기로 채워 넣음 (app.js의 bindPageEvents에서 호출).
export function ChatPage(activeRoomId) {
  const roomId = activeRoomId ? Number(activeRoomId) : "";
  return `
    <section class="chat-layout" data-chat-page data-room-id="${roomId}">
      <aside class="conversation-list" data-conversation-list>
        <h1>Messages</h1>
        <input type="search" placeholder="Search conversations..." aria-label="Search conversations" />
        <div data-room-list>
          <p>채팅방을 불러오는 중...</p>
        </div>
     </aside>
      <article class="chat-panel" data-chat-panel>
        <p>왼쪽에서 대화를 선택하세요.</p>
      </article>
    </section>
  `;
}

// 페이지가 렌더링된 뒤 호출: 채팅방 목록/이력 로드, WebSocket 연결, 메시지 전송 바인딩.
export function initChatPage() {
  const root = document.querySelector("[data-chat-page]");
  if (!root) return;

  const roomIdAttr = root.dataset.roomId;
  const roomId = roomIdAttr ? Number(roomIdAttr) : null;
  const listEl = root.querySelector("[data-room-list]");
  const panelEl = root.querySelector("[data-chat-panel]");

  loadRoomList(listEl, roomId, panelEl);
}

// 다른 페이지로 이동할 때 반드시 호출해서 WebSocket 연결을 정리해야 함 (app.js render()에서 매번 호출).
export function teardownChatPage() {
  if (activeClient) {
    activeClient.deactivate();
    activeClient = null;
  }
}

async function loadRoomList(listEl, roomId, panelEl) {
  let rooms = [];
  try {
    rooms = await fetchMyChatRooms();
  } catch (error) {
    listEl.innerHTML = `<p>채팅방 목록을 불러오지 못했습니다: ${escapeHtml(error.message)}</p>`;
    return;
  }

  listEl.innerHTML = renderRoomList(rooms, roomId);

  if (!roomId) return;

  const room = rooms.find((item) => item.chatRoomId === roomId);
  if (!room) {
    panelEl.innerHTML = `<p>채팅방을 찾을 수 없거나 접근 권한이 없습니다.</p>`;
    return;
  }

  openRoom(panelEl, room);
}

function renderRoomList(rooms, activeRoomId) {
  if (!rooms.length) {
    return `<p>아직 채팅방이 없습니다. 게시글에서 "채팅 신청"을 눌러보세요.</p>`;
  }

  return rooms
    .map((room) => {
      const isActive = room.chatRoomId === activeRoomId;
      return `
        <a class="conversation ${isActive ? "active" : ""}" href="#/chat/${room.chatRoomId}">
          <strong>${escapeHtml(room.otherUserNickname ?? "상대방")}</strong>
          <span>${postLabel(room)}</span>
        </a>
      `;
    })
    .join("");
}

function openRoom(panelEl, room) {
  teardownChatPage();

  panelEl.innerHTML = panelTemplate(room);
  const streamEl = panelEl.querySelector("[data-message-stream]");
  const formEl = panelEl.querySelector("[data-compose-form]");
  const currentUserId = getCurrentUserId();

  loadHistory(streamEl, room.chatRoomId, currentUserId);

  activeClient = connectChatRoom(room.chatRoomId, {
    onMessage: (message) => {
      streamEl.insertAdjacentHTML("beforeend", renderBubble(message, currentUserId));
      streamEl.scrollTop = streamEl.scrollHeight;
    },
    onError: () => {
      streamEl.insertAdjacentHTML("beforeend", `<p>채팅 연결에 문제가 발생했습니다.</p>`);
    },
  });

  formEl.addEventListener("submit", (event) => {
    event.preventDefault();
    const input = formEl.elements.content;
    const value = input.value.trim();
    if (!value) return;

    try {
      sendChatMessage(activeClient, room.chatRoomId, value);
      input.value = "";
    } catch (error) {
      alert(error.message);
    }
  });

  const leaveButtonEl = panelEl.querySelector("[data-leave-room]");
  leaveButtonEl?.addEventListener("click", () => handleLeaveRoom(room.chatRoomId));
}

async function handleLeaveRoom(chatRoomId) {
  if (!confirm("채팅방을 나가시겠습니까? 대화 내용을 다시 볼 수 없습니다.")) return;

  try {
    await leaveChatRoom(chatRoomId);
  } catch (error) {
    alert(`채팅방 나가기 실패: ${error.message}`);
    return;
  }

  // hashchange 리스너(app.js의 render())가 teardown + 목록 갱신을 알아서 처리함.
  window.location.hash = "/chat";
}

async function loadHistory(streamEl, chatRoomId, currentUserId) {
  try {
    const history = await fetchChatMessages(chatRoomId);
    const ascending = [...history].reverse();
    streamEl.innerHTML = ascending.length
      ? ascending.map((message) => renderBubble(message, currentUserId)).join("")
      : `<p>아직 메시지가 없습니다. 첫 메시지를 보내보세요.</p>`;
    streamEl.scrollTop = streamEl.scrollHeight;
  } catch (error) {
    streamEl.innerHTML = `<p>메시지 이력을 불러오지 못했습니다: ${escapeHtml(error.message)}</p>`;
  }
}

function panelTemplate(room) {
  const payHref = `#/checkout?chatRoomId=${room.chatRoomId}`;
  const initial = (room.otherUserNickname || "?").charAt(0).toUpperCase();

  return `
    <header>
      <div class="seller-box">
        <div class="avatar">${escapeHtml(initial)}</div>
        <div><strong>${escapeHtml(room.otherUserNickname ?? "상대방")}</strong><span>${postLabel(room)}</span></div>
      </div>
      <div class="chat-panel-actions">
        ${button("결제하기", payHref, "primary")}
        <button type="button" class="button quiet" data-leave-room>채팅방 나가기</button>
      </div>
    </header>
    <div class="message-stream" data-message-stream>
      <p>메시지를 불러오는 중...</p>
    </div>
    <form class="message-compose" data-compose-form>
      <input type="text" name="content" placeholder="메시지를 입력하세요..." aria-label="Message" autocomplete="off" />
      <button type="submit">Send</button>
    </form>
  `;
}

function renderBubble(message, currentUserId) {
  const mine = currentUserId != null && Number(message.senderId) === Number(currentUserId);
  return `<p class="bubble ${mine ? "me" : ""}">${escapeHtml(message.content)}</p>`;
}

function postLabel(room) {
  if (room.talentPostId) return `판매글 #${room.talentPostId}`;
  if (room.requestPostId) return `요청글 #${room.requestPostId}`;
  return "";
}

function escapeHtml(value) {
  return String(value ?? "").replace(/[&<>"']/g, (char) => (
    { "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[char]
  ));
}
