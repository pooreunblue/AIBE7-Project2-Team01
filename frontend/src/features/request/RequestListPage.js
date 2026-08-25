import { listToolbar, requestCard } from "../../shared/ui/index.js";
import { requests } from "../../shared/data/mock.js";

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
      <div class="request-grid" data-list="requests">
        ${requests.map(requestCard).join("")}
      </div>
      <div class="center-action">
        <button class="button secondary" type="button">Load More</button>
      </div>
    </section>
  `;
}
