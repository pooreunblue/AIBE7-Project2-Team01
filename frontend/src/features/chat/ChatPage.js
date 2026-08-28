import { getCurrentUserId } from "../../auth/currentUser.js";
import { fetchMyChatRooms, fetchChatMessages, leaveChatRoom, uploadChatImage } from "./chatApi.js";
import { connectChatRoom, sendChatMessage } from "./chatSocket.js";
import { fetchRequest } from "../request/requestApi.js";
import { createTrade, fetchMyTrades, payTrade } from "../trade/tradeApi.js";
import { fetchWallet } from "../wallet/walletApi.js";

let activeClient = null;

// 채팅 목록 페이지(#/chat) 겸 채팅방 페이지(#/chat/{id}).
// 실제 데이터는 initChatPage()가 렌더링 후 비동기로 채워 넣음 (app.js의 bindPageEvents에서 호출).
export function ChatPage(activeRoomId) {
  const roomId = activeRoomId || "";
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
  const roomId = roomIdAttr || null;
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
          ${renderChatAvatar(room)}
          <div>
            <strong>${escapeHtml(room.otherUserNickname ?? "상대방")}</strong>
            <span>${postLabel(room)}</span>
          </div>
        </a>
      `;
    })
    .join("");
}

async function openRoom(panelEl, room) {
  teardownChatPage();

  const currentUserId = await getCurrentUserId({ optional: true });
  const requestPost = await loadRequestPost(room);
  const activeTrade = await loadActiveTrade(room.chatRoomId);
  const canRequestTrade = Boolean(
    requestPost &&
    !activeTrade &&
    currentUserId != null &&
    String(requestPost.userId) === String(currentUserId)
  );

  panelEl.innerHTML = panelTemplate(room, { canRequestTrade });
  const streamEl = panelEl.querySelector("[data-message-stream]");
  const formEl = panelEl.querySelector("[data-compose-form]");

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

  const imageInputEl = formEl.querySelector("[data-image-input]");
  imageInputEl?.addEventListener("change", async () => {
    const file = imageInputEl.files?.[0];
    if (!file) return;

    formEl.classList.add("uploading");
    try {
      // 업로드만 하면 서버가 STOMP 로 메시지를 브로드캐스트 → onMessage 에서 화면에 그려짐.
      await uploadChatImage(room.chatRoomId, file);
    } catch (error) {
      alert(`이미지 전송 실패: ${error.message}`);
    } finally {
      formEl.classList.remove("uploading");
      imageInputEl.value = "";
    }
  });

  const leaveButtonEl = panelEl.querySelector("[data-leave-room]");
  leaveButtonEl?.addEventListener("click", () => handleLeaveRoom(room.chatRoomId));

  panelEl.querySelector("[data-trade-request-open]")?.addEventListener("click", () => {
    openTradeRequestModal(panelEl, { room, requestPost });
  });

  panelEl.addEventListener("click", (event) => {
    const buttonEl = event.target.closest("[data-trade-pay-open]");
    if (!buttonEl) return;

    const trade = {
      tradeId: buttonEl.dataset.tradePayOpen,
      amount: buttonEl.dataset.tradeAmount,
      status: buttonEl.dataset.tradeStatus,
      payerId: buttonEl.dataset.tradePayerId,
    };
    openTradePayModal(panelEl, trade);
  });
}

async function loadRequestPost(room) {
  if (!room.requestPostId) return null;

  try {
    return await fetchRequest(room.requestPostId);
  } catch {
    return null;
  }
}

async function loadActiveTrade(chatRoomId) {
  try {
    const trades = await fetchMyTrades({ size: 50 });
    return (trades.content || []).find(
      (trade) => String(trade.chatRoomId) === String(chatRoomId) &&
        (trade.status === "PENDING" || trade.status === "PAID")
    );
  } catch {
    return null;
  }
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

function panelTemplate(room, { canRequestTrade = false } = {}) {
  return `
    <header>
      <div class="seller-box">
        ${renderChatAvatar(room)}
        <div><strong>${escapeHtml(room.otherUserNickname ?? "상대방")}</strong><span>${postLabel(room)}</span></div>
      </div>
      <div class="chat-panel-actions">
        ${canRequestTrade ? `<button type="button" class="button primary" data-trade-request-open>거래 요청</button>` : ""}
        <button type="button" class="button quiet" data-leave-room>채팅방 나가기</button>
      </div>
    </header>
    <div class="message-stream" data-message-stream>
      <p>메시지를 불러오는 중...</p>
    </div>
    <form class="message-compose" data-compose-form>
      <label class="compose-attach" title="사진 보내기">
        <input type="file" name="image" accept="image/png,image/jpeg,image/gif,image/webp" data-image-input hidden />
        <span aria-hidden="true">📷</span>
      </label>
      <input type="text" name="content" placeholder="메시지를 입력하세요..." aria-label="Message" autocomplete="off" />
      <button type="submit">Send</button>
    </form>
  `;
}

function renderBubble(message, currentUserId) {
  const mine = currentUserId != null && String(message.senderId) === String(currentUserId);
  const mineClass = mine ? "me" : "";

  if (message.messageType === "TRADE_REQUEST" && message.trade) {
    return renderTradeRequestBubble(message, currentUserId, mineClass);
  }

  if (message.messageType === "IMAGE") {
    const src = escapeHtml(message.content);
    return `<a class="bubble bubble-image ${mineClass}" href="${src}" target="_blank" rel="noopener">
      <img src="${src}" alt="첨부 이미지" loading="lazy" />
    </a>`;
  }

  return `<p class="bubble ${mineClass}">${escapeHtml(message.content)}</p>`;
}

function renderTradeRequestBubble(message, currentUserId, mineClass) {
  const trade = message.trade;
  const isPayer = currentUserId != null && String(trade.payerId) === String(currentUserId);
  const isPending = trade.status === "PENDING";

  return `
    <article class="bubble trade-bubble ${mineClass}">
      <span>거래 요청</span>
      <strong>${formatTradeAmount(trade.amount)}</strong>
      <p>${trade.postType === "REQUEST" ? "요청글 거래가 생성되었습니다." : "재능글 거래가 생성되었습니다."}</p>
      ${isPending && isPayer ? `
        <button
          type="button"
          class="button primary"
          data-trade-pay-open="${escapeHtml(trade.tradeId)}"
          data-trade-amount="${escapeHtml(trade.amount)}"
          data-trade-status="${escapeHtml(trade.status)}"
          data-trade-payer-id="${escapeHtml(trade.payerId)}"
        >거래 진행</button>
      ` : `<small>${tradeStatusLabel(trade.status, isPayer)}</small>`}
    </article>
  `;
}

function openTradeRequestModal(panelEl, { room, requestPost }) {
  if (!requestPost) {
    alert("요청글 정보를 불러오지 못했습니다.");
    return;
  }

  const defaultAmount = Number(requestPost.budgetMax || requestPost.budgetMin || 0);
  showChatModal(panelEl, `
    <div class="modal-head">
      <div>
        <span class="kicker">Trade Request</span>
        <h2>거래 금액 설정</h2>
      </div>
      <button class="modal-close" type="button" data-chat-modal-close aria-label="팝업 닫기">x</button>
    </div>
    <form class="trade-modal-form" data-trade-request-form>
      <label class="field">
        <span>요청글</span>
        <input type="text" value="${escapeHtml(requestPost.title || room.postTitle || "요청글")}" readonly />
      </label>
      <label class="field">
        <span>거래 금액</span>
        <input type="number" name="amount" min="1" step="1" value="${defaultAmount || ""}" required />
      </label>
      <p class="form-message" data-trade-modal-message aria-live="polite"></p>
      <div class="form-actions">
        <button class="button quiet" type="button" data-chat-modal-close>취소</button>
        <button class="button primary" type="submit">거래 요청 보내기</button>
      </div>
    </form>
  `);

  const modalEl = panelEl.querySelector("[data-chat-modal]");
  const formEl = modalEl.querySelector("[data-trade-request-form]");
  const messageEl = modalEl.querySelector("[data-trade-modal-message]");
  const submitButton = formEl.querySelector("button[type='submit']");

  formEl.addEventListener("submit", async (event) => {
    event.preventDefault();
    const amount = Number(new FormData(formEl).get("amount"));
    if (!Number.isFinite(amount) || amount <= 0) {
      messageEl.textContent = "거래 금액을 확인해 주세요.";
      return;
    }

    submitButton.disabled = true;
    try {
      await createTrade(room.chatRoomId, { amount, requestPostId: requestPost.requestPostId });
      panelEl.querySelector("[data-trade-request-open]")?.remove();
      closeChatModal(panelEl);
    } catch (error) {
      messageEl.textContent = error.message;
      submitButton.disabled = false;
    }
  });
}

async function openTradePayModal(panelEl, trade) {
  showChatModal(panelEl, `
    <div class="modal-head">
      <div>
        <span class="kicker">Payment</span>
        <h2>거래 결제</h2>
      </div>
      <button class="modal-close" type="button" data-chat-modal-close aria-label="팝업 닫기">x</button>
    </div>
    <div class="trade-payment-detail">
      <dl>
        <div><dt>거래 금액</dt><dd>${formatTradeAmount(trade.amount)}</dd></div>
        <div><dt>내 잔액</dt><dd data-trade-wallet>불러오는 중...</dd></div>
      </dl>
      <p class="form-message" data-trade-modal-message aria-live="polite"></p>
      <div class="form-actions">
        <button class="button quiet" type="button" data-chat-modal-close>취소</button>
        <button class="button primary" type="button" data-trade-pay-submit disabled>지불하기</button>
      </div>
    </div>
  `);

  const modalEl = panelEl.querySelector("[data-chat-modal]");
  const walletEl = modalEl.querySelector("[data-trade-wallet]");
  const messageEl = modalEl.querySelector("[data-trade-modal-message]");
  const payButton = modalEl.querySelector("[data-trade-pay-submit]");

  try {
    const wallet = await fetchWallet();
    const balance = Number(wallet.balance || 0);
    const amount = Number(trade.amount || 0);
    walletEl.textContent = formatTradeAmount(balance);
    payButton.disabled = balance < amount;
    if (balance < amount) {
      messageEl.textContent = "잔액이 부족합니다.";
    }
  } catch (error) {
    walletEl.textContent = "-";
    messageEl.textContent = error.message;
  }

  payButton.addEventListener("click", async () => {
    payButton.disabled = true;
    try {
      await payTrade(trade.tradeId);
      closeChatModal(panelEl);
      alert("결제가 완료되었습니다.");
    } catch (error) {
      messageEl.textContent = error.message;
      payButton.disabled = false;
    }
  });
}

function showChatModal(panelEl, content) {
  closeChatModal(panelEl);
  panelEl.insertAdjacentHTML("beforeend", `
    <div class="modal-backdrop chat-modal-backdrop" data-chat-modal>
      <div class="charge-modal chat-trade-modal" role="dialog" aria-modal="true">
        ${content}
      </div>
    </div>
  `);

  const modalEl = panelEl.querySelector("[data-chat-modal]");
  modalEl.addEventListener("click", (event) => {
    if (event.target === modalEl || event.target.closest("[data-chat-modal-close]")) {
      closeChatModal(panelEl);
    }
  });
}

function closeChatModal(panelEl) {
  panelEl.querySelector("[data-chat-modal]")?.remove();
}

function formatTradeAmount(value) {
  const amount = Number(value || 0);
  return `${amount.toLocaleString("ko-KR")}원`;
}

function tradeStatusLabel(status, isPayer) {
  if (status === "PAID") return "결제 완료";
  if (status === "COMPLETED") return "거래 완료";
  if (status === "CANCELLED") return "취소된 거래";
  return isPayer ? "결제 대기 중" : "상대방 결제 대기 중";
}

function postLabel(room) {
  if (room.postTitle) return escapeHtml(room.postTitle);
  if (room.talentPostId) return "판매글";
  if (room.requestPostId) return "요청글";
  return "";
}

function renderChatAvatar(room) {
  const initial = (room.otherUserNickname || "?").charAt(0).toUpperCase();
  return `
    <div class="avatar chat-avatar">
      ${room.otherUserProfileImageUrl ? `<img src="${escapeHtml(room.otherUserProfileImageUrl)}" alt="" />` : escapeHtml(initial)}
    </div>
  `;
}

function escapeHtml(value) {
  return String(value ?? "").replace(/[&<>"']/g, (char) => (
    { "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[char]
  ));
}
