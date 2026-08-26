import { listToolbar } from "../../shared/ui/index.js";

export function RequestListPage() {
  return `
    <section class="section">
      <div class="list-heading">
        <div>
          <span class="kicker">Requests</span>
          <h1>등록된 의뢰와 일을 찾아보세요.</h1>
        </div>
      </div>
      ${listToolbar("의뢰", "All", "#/requests")}
      <div class="request-grid" data-request-list>
        <article class="request-card">
          <span class="kicker">LOAD</span>
          <h3>의뢰글을 불러오는 중입니다.</h3>
          <p>잠시만 기다려 주세요.</p>
        </article>
      </div>
      <div class="center-action">
        <button class="button secondary" type="button" disabled>Load More</button>
      </div>
    </section>
  `;
}
