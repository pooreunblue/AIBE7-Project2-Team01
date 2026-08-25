import { formatMoney } from "../../shared/ui/index.js";
import { requests, talents, user } from "../../shared/data/mock.js";

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
        <div class="profile-summary">
          <div class="avatar large">A</div>
          <div>
            <h1>${user.name}</h1>
            <p>${user.role}</p>
            <span>${user.location} · ★ 4.9 · 124 reviews</span>
          </div>
          <strong>${formatMoney(user.balance)}</strong>
        </div>
        <div class="stats-grid">
          ${user.stats.map(([label, value]) => `<article><span>${label}</span><strong>${value}</strong></article>`).join("")}
        </div>
        <div class="dashboard-panels">
          <article>
            <h2>최근 거래 내역</h2>
            <p>로고 디자인 제작 <strong>+150,000원</strong></p>
            <p>랜딩페이지 퍼블리싱 <strong>+300,000원</strong></p>
          </article>
          <article>
            <h2>판매/구매 글 관리</h2>
            <p>${talents[0].title}</p>
            <p>${requests[0].title}</p>
          </article>
        </div>
      </div>
    </section>
  `;
}
