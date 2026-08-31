export function UserProfilePage(userId) {
  return `
    <section class="section public-profile-page" data-user-profile="${userId}">
      <div class="profile-summary">
        <div class="avatar large" data-user-profile-avatar>?</div>
        <div>
          <span class="kicker">Profile</span>
          <h1 data-user-profile-nickname>사용자 정보를 불러오는 중입니다.</h1>
          <p data-user-profile-created-at>-</p>
        </div>
      </div>

      <section class="dashboard-section mypage-portfolio-section">
        <div class="section-head">
          <h2>포트폴리오</h2>
        </div>
        <div class="portfolio-card-grid mypage-portfolio-grid" data-user-profile-portfolios>
          <article class="portfolio-card text-only">
            <h3>포트폴리오를 불러오는 중입니다.</h3>
            <p>잠시만 기다려 주세요.</p>
          </article>
        </div>
      </section>

      <section class="dashboard-section public-profile-posts">
        <div class="section-head">
          <h2>작성한 게시글</h2>
          <button class="button quiet" type="button" data-user-profile-posts-all hidden>전체보기</button>
        </div>
        <div class="public-profile-post-list" data-user-profile-posts>
          <article class="trade-list-card">
            <span>LOAD</span>
            <h3>작성한 게시글을 불러오는 중입니다.</h3>
            <p>잠시만 기다려 주세요.</p>
          </article>
        </div>
      </section>
    </section>
  `;
}
