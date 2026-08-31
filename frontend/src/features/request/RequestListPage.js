import { listToolbar } from "../../shared/ui/index.js";

export function RequestListPage() {
  return `
    <section class="section talent-page">
      <div class="list-heading">
        <div>
          <span class="kicker">Requests</span>
          <h1>등록된 의뢰와 일을 찾아보세요.</h1>
        </div>
        <div class="filter-actions">
          <select aria-label="Sort">
            <option>Popular first</option>
            <option>Newest first</option>
          </select>
          <button type="button">Filters</button>
        </div>
      </div>
      ${listToolbar("의뢰", "All", "#/requests")}
      <div class="request-grid" data-request-list>
        <article class="request-card">
          <div class="card-body">
            <span class="kicker">LOAD</span>
            <h3>의뢰글을 불러오는 중입니다.</h3>
            <p>잠시만 기다려 주세요.</p>
          </div>
        </article>
      </div>
      <div class="center-action">
        <button class="button secondary" type="button" data-request-load-more hidden>더 보기</button>
      </div>
      <a class="floating-create-button" href="#/request-new" aria-label="새 요청글 작성">
        <span aria-hidden="true">+</span>
      </a>
    </section>
  `;
}
