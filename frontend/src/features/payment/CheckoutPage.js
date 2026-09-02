import { formatMoney, pageTitle } from "../../shared/ui/index.js";
import { getCurrentUserId } from "../../auth/currentUser.js";
import { fetchMyChatRooms } from "../chat/chatApi.js";
import { fetchRequest } from "../request/requestApi.js";
import { fetchTalent } from "../talent/talentApi.js";
import { fetchWallet } from "../wallet/walletApi.js";
import { fetchMyTrades, payTrade } from "../trade/tradeApi.js";
import { escapeHtml, setSafeHtml } from "../../shared/security/xss.js";

// 채팅방에서 확정된 거래를 조회하고 결제하는 화면. 이 페이지에서는 금액을 만들거나 수정하지 않는다.
export function CheckoutPage() {
  const params = new URLSearchParams(window.location.hash.split("?")[1] || "");
  const rawChatRoomId = params.get("chatRoomId");
  const chatRoomId = rawChatRoomId || null;

  if (!chatRoomId) {
    return `<section class="checkout-layout"><p>잘못된 접근입니다. 채팅방의 거래 요청을 통해 들어와 주세요.</p></section>`;
  }

  return `
    <section class="checkout-layout" data-checkout-page="${chatRoomId}">
      ${pageTitle("Checkout", "채팅으로 협의한 거래를 결제합니다.")}
      <div class="checkout-grid">
        <div>
          <article class="summary-card" data-checkout-summary>
            <p>불러오는 중...</p>
          </article>
        </div>
        <aside class="checkout-card" data-checkout-panel>
          <p>불러오는 중...</p>
        </aside>
      </div>
    </section>
  `;
}

export async function initCheckoutPage() {
  const root = document.querySelector("[data-checkout-page]");
  if (!root) return;

  const chatRoomId = root.dataset.checkoutPage;
  const summaryEl = root.querySelector("[data-checkout-summary]");
  const panelEl = root.querySelector("[data-checkout-panel]");
  const currentUserId = await getCurrentUserId({ optional: true });

  let room;
  try {
    const rooms = await fetchMyChatRooms();
    room = rooms.find((item) => item.chatRoomId === chatRoomId);
  } catch (error) {
    setSafeHtml(summaryEl, `<p>채팅방 정보를 불러오지 못했습니다: ${escapeHtml(error.message)}</p>`);
    return;
  }

  if (!room) {
    setSafeHtml(summaryEl, `<p>채팅방을 찾을 수 없거나 접근 권한이 없습니다.</p>`);
    panelEl.replaceChildren();
    return;
  }

  let post;
  let existingTrade;
  try {
    [post, existingTrade] = await Promise.all([
      room.requestPostId ? fetchRequest(room.requestPostId) : fetchTalent(room.talentPostId),
      findActiveTrade(chatRoomId),
    ]);
  } catch (error) {
    setSafeHtml(summaryEl, `<p>거래 정보를 불러오지 못했습니다: ${escapeHtml(error.message)}</p>`);
    return;
  }

  setSafeHtml(summaryEl, renderRoomSummary(room, post));

  const isPayer = currentUserId != null && existingTrade != null &&
    String(existingTrade.payerId) === String(currentUserId);
  let wallet = null;
  if (existingTrade?.status === "PENDING" && isPayer) {
    try {
      wallet = await fetchWallet();
    } catch (error) {
      setSafeHtml(panelEl, `<p>잔액을 불러오지 못했습니다: ${escapeHtml(error.message)}</p>`);
      return;
    }
  }
  renderPanel(panelEl, { room, wallet, existingTrade, isPayer, currentUserId, post });
}

async function findActiveTrade(chatRoomId) {
  const { content } = await fetchMyTrades({ size: 50 });
  return (content || []).find(
    (trade) => String(trade.chatRoomId) === String(chatRoomId) &&
      (trade.status === "PENDING" || trade.status === "PAID")
  );
}

function renderRoomSummary(room, post) {
  const isRequestPost = Boolean(room.requestPostId);
  return `
    <h2>거래 정보</h2>
    <div class="seller-box">
      <div class="avatar">${escapeHtml((room.otherUserNickname || "?").charAt(0).toUpperCase())}</div>
      <div><strong>${escapeHtml(room.otherUserNickname ?? "상대방")}</strong><span>채팅방 #${room.chatRoomId}</span></div>
    </div>
    ${post
      ? `
        <h2>${isRequestPost ? "재능 요청글" : "재능 판매글"}</h2>
        <p><strong>${escapeHtml(post.title)}</strong></p>
        <p>${escapeHtml(post.content)}</p>
        ${isRequestPost ? `<dl>
          <div><dt>예산</dt><dd>${formatMoney(Number(post.budgetMin || 0))} ~ ${formatMoney(Number(post.budgetMax || 0))}</dd></div>
        </dl>` : `<dl>
          <div><dt>등록 가격</dt><dd>${formatMoney(Number(post.price || 0))}</dd></div>
        </dl>`}
      `
      : ""}
  `;
}

function renderPanel(panelEl, { room, wallet, existingTrade, isPayer, currentUserId, post }) {
  if (existingTrade) {
    setSafeHtml(panelEl, renderTradePanel(existingTrade, wallet, isPayer));
    if (existingTrade.status === "PENDING" && isPayer) {
      bindPayButton(panelEl, existingTrade.tradeId);
    }
    return;
  }

  const isPostOwner = currentUserId != null && String(post.userId) === String(currentUserId);
  const guidance = room.requestPostId
    ? (isPostOwner
      ? "채팅에서 상대방에게 금액 설정을 요청해 주세요."
      : "채팅에서 받을 금액을 설정한 뒤 지불 요청을 보내 주세요.")
    : (isPostOwner
      ? "채팅에서 판매 금액을 설정하고 거래 요청을 보내 주세요."
      : "판매자가 거래 금액을 확정할 때까지 기다려 주세요.");

  setSafeHtml(panelEl, `
    <span>Payment Details</span>
    <p>${guidance}</p>
    <a class="button primary" href="#/chat/${room.chatRoomId}">채팅으로 이동</a>
  `);
}

function renderTradePanel(trade, wallet, isPayer) {
  if (trade.status === "PAID") {
    return `
      <span>Payment Details</span>
      <p>결제가 완료된 거래입니다.</p>
      <dl>
        <div><dt>결제 금액</dt><dd>${formatMoney(Number(trade.amount || 0))}</dd></div>
      </dl>
    `;
  }

  // PENDING
  return `
    <span>Payment Details</span>
    <p>${isPayer ? "생성된 거래가 있습니다. 결제를 진행해 주세요." : "상대방의 결제를 기다리는 중입니다."}</p>
    <dl>
      <div><dt>결제 금액</dt><dd>${formatMoney(Number(trade.amount || 0))}</dd></div>
      ${isPayer ? `<div><dt>내 잔액</dt><dd>${formatMoney(Number(wallet?.balance || 0))}</dd></div>` : ""}
    </dl>
    <p data-checkout-message></p>
    ${isPayer ? `<button type="button" class="button primary" data-checkout-pay>결제하기</button>` : ""}
  `;
}

function bindPayButton(panelEl, tradeId) {
  const payButton = panelEl.querySelector("[data-checkout-pay]");
  const messageEl = panelEl.querySelector("[data-checkout-message]");
  if (!payButton) return;

  payButton.addEventListener("click", async () => {
    payButton.disabled = true;
    try {
      await payTrade(tradeId);
      setSafeHtml(panelEl, `<p>결제가 완료되어 거래가 완료되었습니다.</p>`);
    } catch (error) {
      if (messageEl) messageEl.textContent = error.message;
      payButton.disabled = false;
    }
  });
}
