import { pageTitle } from "../../shared/ui/index.js";

export function PortfolioPage() {
  return `
    <section class="section">
      <div class="section-head portfolio-page-head">
        ${pageTitle("Portfolio", "포트폴리오 관리", "등록한 작업 이력과 소개 자료를 관리합니다.")}
        <a class="button primary" href="#/portfolio-new">새 글 작성</a>
      </div>
      <div class="portfolio-card-grid portfolio-page-grid" data-portfolio-list>
        <article class="portfolio-card">
          <h3>포트폴리오를 불러오는 중입니다.</h3>
          <p>잠시만 기다려 주세요.</p>
        </article>
      </div>
    </section>
  `;
}
