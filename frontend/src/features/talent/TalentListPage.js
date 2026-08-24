import { listToolbar, talentCard } from "../../shared/ui/index.js";
import { talents } from "../../shared/data/mock.js";

export function TalentListPage() {
  return `
    <section class="section">
      <div class="list-heading">
        <div>
          <span class="kicker">Experts</span>
          <h1>전문가/프리랜서를 찾아보세요.</h1>
        </div>
        <div class="filter-actions">
          <select aria-label="Sort">
            <option>Popular first</option>
            <option>Newest first</option>
          </select>
          <button type="button">Filters</button>
        </div>
      </div>
      ${listToolbar("전문가", "All", "#/talents")}
      <div class="talent-grid" data-list="talents">
        ${talents.map((talent) => talentCard(talent)).join("")}
      </div>
      <div class="center-action">
        <button class="button secondary" type="button">Load More</button>
      </div>
    </section>
  `;
}
