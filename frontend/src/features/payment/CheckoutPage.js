import { formatMoney, pageTitle } from "../../shared/ui/index.js";
import { getCurrentUserId } from "../../auth/currentUser.js";
import { fetchMyChatRooms } from "../chat/chatApi.js";
import { fetchRequest } from "../request/requestApi.js";
import { fetchWallet } from "../wallet/walletApi.js";
import { createTrade, fetchMyTrades, payTrade } from "../trade/tradeApi.js";

// 채팅방 하위 결제 화면(#/checkout?chatRoomId={id}). 거래는 반드시 채팅을 거쳐야 하므로
// chatRoomId 없이는 진입할 수 없음 (ChatPage의 "결제하기" 버튼에서만 들어옴).
export function CheckoutPage() {
  const params = new URLSearchParams(window.location.hash.split("?")[1] || "");
  const rawChatRoomId = params.get("chatRoomId");
  const chatRoomId = rawChatRoomId && /^\d+$/.test(rawChatRoomId) ? rawChatRoomId : null;

  if (!chatRoomId) {
    return `<section class="checkout-layout"><p>잘못된 접근입니다. 채팅방의 "결제하기" 버튼을 통해 들어와 주세요.</p></section>`;
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

  const chatRoomId = Number(root.dataset.checkoutPage);
  const summaryEl = root.querySelector("[data-checkout-summary]");
  const panelEl = root.querySelector("[data-checkout-panel]");
  const currentUserId = getCurrentUserId();

  let room;
  try {
    const rooms = await fetchMyChatRooms();
    room = rooms.find((item) => item.chatRoomId === chatRoomId);
  } catch (error) {
    summaryEl.innerHTML = `<p>채팅방 정보를 불러오지 못했습니다: ${escapeHtml(error.message)}</p>`;
    return;
  }

  if (!room) {
    summaryEl.innerHTML = `<p>채팅방을 찾을 수 없거나 접근 권한이 없습니다.</p>`;
    panelEl.innerHTML = "";
    return;
  }

  if (!room.requestPostId) {
    // talent 도메인이 아직 없어서 재능글 기반 거래는 백엔드에서도 막고 있음 — 프론트도 동일하게 안내만.
    summaryEl.innerHTML = renderRoomSummary(room, null);
    panelEl.innerHTML = `<p>재능글 기반 거래는 아직 지원하지 않습니다.</p>`;
    return;
  }

  let post;
  let wallet;
  let existingTrade;
  try {
    [post, wallet, existingTrade] = await Promise.all([
      fetchRequest(room.requestPostId),
      fetchWallet(),
      findActiveTrade(chatRoomId),
    ]);
  } catch (error) {
    summaryEl.innerHTML = `<p>거래 정보를 불러오지 못했습니다: ${escapeHtml(error.message)}</p>`;
    return;
  }

  summaryEl.innerHTML = renderRoomSummary(room, post);

  const isPayer = currentUserId != null && Number(post.userId) === Number(currentUserId);
  renderPanel(panelEl, { chatRoomId, room, post, wallet, existingTrade, isPayer });
}

async function findActiveTrade(chatRoomId) {
  const { content } = await fetchMyTrades({ size: 50 });
  return (content || []).find(
    (trade) => trade.chatRoomId === chatRoomId && (trade.status === "PENDING" || trade.status === "PAID")
  );
}

function renderRoomSummary(room, post) {
  return `
    <h2>거래 정보</h2>
    <div class="seller-box">
      <div class="avatar">${escapeHtml((room.otherUserNickname || "?").charAt(0).toUpperCase())}</div>
      <div><strong>${escapeHtml(room.otherUserNickname ?? "상대방")}</strong><span>채팅방 #${room.chatRoomId}</span></div>
    </div>
    ${post
      ? `
        <h2>요청글</h2>
        <p><strong>${escapeHtml(post.title)}</strong></p>
        <p>${escapeHtml(post.content)}</p>
        <dl>
          <div><dt>예산</dt><dd>${formatMoney(Number(post.budgetMin || 0))} ~ ${formatMoney(Number(post.budgetMax || 0))}</dd></div>
        </dl>
      `
      : ""}
  `;
}

function renderPanel(panelEl, { chatRoomId, post, wallet, existingTrade, isPayer }) {
  if (existingTrade) {
    panelEl.innerHTML = renderTradePanel(existingTrade, wallet, isPayer);
    if (existingTrade.status === "PENDING" && isPayer) {
      bindPayButton(panelEl, existingTrade.tradeId);
    }
    return;
  }

  if (!isPayer) {
    panelEl.innerHTML = `<p>결제는 이 요청글을 등록한 의뢰인만 진행할 수 있습니다.</p>`;
    return;
  }

  panelEl.innerHTML = `
    <span>Payment Details</span>
    <p>내 잔액: ${formatMoney(Number(wallet.balance || 0))}</p>
    <form data-checkout-form>
      <label>
        결제 금액
        <input type="number" name="amount" min="1" step="1" value="${post.budgetMax || post.budgetMin || ""}" required />
      </label>
      <p data-checkout-message></p>
      <button type="submit" class="button primary">결제 진행</button>
    </form>
    <p class="secure-note">결제 금액은 지갑(cash) 잔액에서 차감됩니다.</p>
  `;

  const formEl = panelEl.querySelector("[data-checkout-form]");
  const messageEl = panelEl.querySelector("[data-checkout-message]");
  const submitButton = formEl.querySelector("button");

  formEl.addEventListener("submit", async (event) => {
    event.preventDefault();
    const amount = Number(new FormData(formEl).get("amount"));
    if (!Number.isFinite(amount) || amount <= 0) {
      messageEl.textContent = "결제 금액을 확인해 주세요.";
      return;
    }

    submitButton.disabled = true;
    try {
      messageEl.textContent = "";
      const trade = await createTrade(chatRoomId, { amount, requestPostId: post.requestPostId });
      await payTrade(trade.tradeId);
      panelEl.innerHTML = `<p>결제가 완료되었습니다.</p>`;
    } catch (error) {
      messageEl.textContent = error.message;
      submitButton.disabled = false;
    }
  });
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
      ${isPayer ? `<div><dt>내 잔액</dt><dd>${formatMoney(Number(wallet.balance || 0))}</dd></div>` : ""}
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
      panelEl.innerHTML = `<p>결제가 완료되었습니다.</p>`;
    } catch (error) {
      if (messageEl) messageEl.textContent = error.message;
      payButton.disabled = false;
    }
  });
}

function escapeHtml(value) {
  return String(value ?? "").replace(/[&<>"']/g, (char) => (
    { "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[char]
  ));
}
