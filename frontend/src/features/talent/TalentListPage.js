import { listToolbar } from "../../shared/ui/index.js";

export function TalentListPage() {
  return `
    <section class="section talent-page">
      <div class="list-heading">
        <div>
          <span class="kicker">Experts</span>
          <h1>전문가/프리랜서를 찾아보세요.</h1>
        </div>
      </div>
      ${listToolbar("전문가", "All", "#/talents", { withFilters: true })}
      <div class="talent-grid" data-list="talents">
        <article class="talent-card">
          <div class="card-body">
            <span class="kicker">LOAD</span>
            <h3>재능글을 불러오는 중입니다.</h3>
            <p>잠시만 기다려 주세요.</p>
          </div>
        </article>
      </div>
      <div class="center-action">
        <button class="button secondary" type="button" data-talent-load-more hidden>더 보기</button>
      </div>
      <a class="floating-create-button" href="#/talent-new" aria-label="새 재능글 작성">
        <span aria-hidden="true">+</span>
      </a>
    </section>
  `;
}
