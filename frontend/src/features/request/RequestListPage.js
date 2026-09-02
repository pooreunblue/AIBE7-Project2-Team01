import { listToolbar } from "../../shared/ui/index.js";

export function RequestListPage() {
  return `
    <section class="section talent-page">
      <div class="list-heading">
        <div>
          <span class="kicker">Requests</span>
          <h1>등록된 의뢰와 일을 찾아보세요.</h1>
        </div>
      </div>
      ${listToolbar("의뢰", "All", "#/requests", { withFilters: true })}
      <div class="list-page-frame">
        <div class="request-grid" data-request-list>
          <article class="request-card">
            <div class="card-body">
              <span class="kicker">LOAD</span>
              <h3>의뢰글을 불러오는 중입니다.</h3>
              <p>잠시만 기다려 주세요.</p>
            </div>
          </article>
        </div>
        <div class="list-pagination" aria-label="요청글 페이지 이동">
          <button class="list-page-arrow" type="button" data-request-page-prev aria-label="이전 요청글 페이지" hidden>‹</button>
          <span data-request-page-label>1 / 1</span>
          <button class="list-page-arrow" type="button" data-request-page-next aria-label="다음 요청글 페이지" hidden>›</button>
        </div>
      </div>
      <a class="floating-create-button" href="#/request-new" aria-label="새 요청글 작성">
        <span aria-hidden="true">+</span>
      </a>
    </section>
  `;
}
