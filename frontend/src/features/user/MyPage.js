import { formatMoney } from "../../shared/ui/index.js";

export function MyPage() {
  return `
    <section class="section mypage-page">
      <div class="profile-summary" data-my-page-summary>
        <div class="avatar large" data-my-page-avatar>A</div>
        <div>
          <h1 data-my-page-nickname>계정 정보를 불러오는 중입니다.</h1>
          <p data-my-page-email>계정 정보를 불러오는 중입니다.</p>
          <span data-my-page-created-at>-</span>
        </div>
        <div class="profile-actions">
          <button class="button quiet" type="button" data-profile-edit-open>회원정보 수정</button>
          <button class="button secondary" type="button" data-my-page-logout>로그아웃</button>
        </div>
      </div>

      <p class="form-message" data-my-page-message aria-live="polite"></p>

      <div class="mypage-grid">
        <div class="dashboard-section mypage-portfolio-section">
          <div class="section-head">
            <h2>포트폴리오 목록</h2>
            <a href="#/portfolios">전체보기</a>
          </div>
          <div class="portfolio-card-grid mypage-portfolio-grid" data-portfolio-preview>
            <article class="portfolio-card">
              <span>LOAD</span>
              <h3>포트폴리오를 불러오는 중입니다.</h3>
              <p>잠시만 기다려 주세요.</p>
            </article>
          </div>
        </div>

        <div class="dashboard-section mypage-wallet-section">
          <div class="section-head">
            <h2>지갑</h2>
          </div>
          <div class="wallet-summary">
            <span>사용 가능 금액</span>
            <strong data-my-page-wallet>${formatMoney(0)}</strong>
            <div class="wallet-actions">
              <button class="button primary" type="button" data-wallet-charge-open>충전하기</button>
              <button class="button quiet" type="button" data-wallet-history-open>거래내역 확인하기</button>
            </div>
          </div>
        </div>

        <div class="dashboard-section mypage-trade-section">
          <div class="section-head">
            <h2>거래 목록</h2>
            <a href="#/my-trades">전체보기</a>
          </div>
          <div class="mypage-trade-preview" data-my-trade-preview>
            <article class="trade-list-card">
              <span>LOAD</span>
              <h3>거래 목록을 불러오는 중입니다.</h3>
              <p>잠시만 기다려 주세요.</p>
            </article>
          </div>
        </div>
      </div>

      <section class="dashboard-section mypage-authored-section">
        <div class="section-head">
          <h2>작성한 게시글</h2>
          <button class="button quiet" type="button" data-my-authored-posts-all hidden>전체보기</button>
        </div>
        <div class="public-profile-post-list" data-my-authored-posts>
          <article class="trade-list-card">
            <span>LOAD</span>
            <h3>작성한 게시글을 불러오는 중입니다.</h3>
            <p>잠시만 기다려 주세요.</p>
          </article>
        </div>
      </section>

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

      <div class="modal-backdrop" data-wallet-history-modal hidden>
        <div class="charge-modal wallet-history-modal" role="dialog" aria-modal="true">
          <div class="modal-head">
            <div>
              <span class="kicker">Wallet</span>
              <h2>거래내역</h2>
            </div>
            <button class="modal-close" type="button" data-wallet-history-close aria-label="거래내역 팝업 닫기">x</button>
          </div>
          <div class="wallet-history-list" data-wallet-history-list>
            <article class="wallet-history-item">
              <h3>거래내역을 불러오는 중입니다.</h3>
              <p>잠시만 기다려 주세요.</p>
            </article>
          </div>
          <div class="form-actions">
            <button class="button quiet" type="button" data-wallet-history-close>닫기</button>
          </div>
        </div>
      </div>

      <div class="modal-backdrop" data-profile-edit-modal hidden>
        <form class="charge-modal" data-profile-edit-form>
          <div class="modal-head">
            <div>
              <span class="kicker">Profile</span>
              <h2>회원정보 수정</h2>
            </div>
            <button class="modal-close" type="button" data-profile-edit-close aria-label="회원정보 수정 팝업 닫기">x</button>
          </div>
          <label class="field">
            <span>닉네임</span>
            <input name="nickname" type="text" minlength="2" maxlength="20" required />
          </label>
          <div class="profile-upload-field">
            <label class="profile-image-picker" aria-label="프로필 사진 변경">
              <span class="signup-avatar" data-profile-edit-preview aria-hidden="true">○</span>
              <span class="profile-edit-button" aria-hidden="true">+</span>
              <input name="profileImage" type="file" accept="image/png,image/jpeg,image/jpg" />
            </label>
            <small data-profile-edit-file-name>선택사항 · JPG, PNG 파일</small>
          </div>
          <p class="form-message" data-profile-edit-message aria-live="polite"></p>
          <div class="form-actions">
            <button class="button quiet" type="button" data-profile-edit-close>취소</button>
            <button class="button primary" type="submit">저장하기</button>
          </div>
        </form>
      </div>
    </section>
  `;
}
