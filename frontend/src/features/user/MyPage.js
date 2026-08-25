import { formatMoney } from "../../shared/ui/index.js";
import { user } from "../../shared/data/mock.js";

export function MyPage() {
  return `
    <section class="dashboard-layout">
      <aside class="dashboard-menu">
        <a class="active" href="#/mypage">대시보드</a>
        <a href="#/talent-new">프로필 수정</a>
        <a href="#/requests">판매/구매 관리</a>
        <a href="#/chat">메시지 내역</a>
        <a href="#/talents">포트폴리오 관리</a>
        <a href="#/mypage">리뷰 목록</a>
      </aside>
      <div class="dashboard-main">
        <div class="profile-summary" data-my-page-summary>
          <div class="avatar large">A</div>
          <div>
            <h1 data-my-page-nickname>${user.name}</h1>
            <p data-my-page-email>계정 정보를 불러오는 중입니다.</p>
            <span data-my-page-created-at>${user.location}</span>
          </div>
          <div class="wallet-summary">
            <strong data-my-page-wallet>${formatMoney(user.balance)}</strong>
            <button class="button secondary" type="button" data-wallet-charge-open>충전하기</button>
          </div>
        </div>
        <p class="form-message" data-my-page-message aria-live="polite"></p>
        <div class="modal-backdrop" data-wallet-charge-modal hidden>
          <form class="charge-modal" data-wallet-charge-form>
            <div class="modal-head">
              <div>
                <span class="kicker">Wallet</span>
                <h2>지갑 충전</h2>
              </div>
              <button class="modal-close" type="button" data-wallet-charge-close aria-label="충전 팝업 닫기">x</button>
            </div>
            <label class="field">
              <span>충전 금액</span>
              <input name="amount" type="number" min="1000" step="1000" placeholder="1000" required />
            </label>
            <p class="form-message" data-wallet-charge-message aria-live="polite"></p>
            <div class="form-actions">
              <button class="button quiet" type="button" data-wallet-charge-close>취소</button>
              <button class="button primary" type="submit">충전하기</button>
            </div>
          </form>
        </div>
        <div class="dashboard-section">
          <div class="section-head">
            <h2>포트폴리오</h2>
            <a href="#/portfolios">전체보기</a>
          </div>
          <div class="portfolio-card-grid" data-portfolio-preview>
            <article class="portfolio-card">
              <span>LOAD</span>
              <h3>포트폴리오를 불러오는 중입니다.</h3>
              <p>잠시만 기다려 주세요.</p>
            </article>
          </div>
        </div>
      </div>
    </section>
  `;
}
