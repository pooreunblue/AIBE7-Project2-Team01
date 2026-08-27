export function PortfolioPage() {
  return `
    <section class="section portfolio-page">
      <div class="portfolio-card-grid portfolio-page-grid" data-portfolio-list>
        <article class="portfolio-card">
          <h3>포트폴리오를 불러오는 중입니다.</h3>
          <p>잠시만 기다려 주세요.</p>
        </article>
      </div>
      <a class="floating-create-button" href="#/portfolio-new" aria-label="새 포트폴리오 작성">
        <span aria-hidden="true">+</span>
      </a>
    </section>
  `;
}
