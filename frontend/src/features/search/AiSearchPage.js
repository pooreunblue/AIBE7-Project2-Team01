import { pageTitle, requestCard, talentCard } from "../../shared/ui/index.js";
import { requests, talents } from "../../shared/data/mock.js";

export function AiSearchPage() {
  return `
    <section class="section">
      ${pageTitle("AI Search", "What are you looking for?", "Natural language search with keyword fallback when AI is unavailable.")}
      <form class="search-box wide" data-search-form>
        <span class="spark">✦</span>
        <input name="query" type="search" value="I need a freelance UX designer for a fintech app..." aria-label="AI search" />
        <button type="submit">Search</button>
      </form>
      <div class="ai-insight">
        <strong>AI Insight</strong>
        <span>Looking for UX Design expertise specifically tailored for Financial Technology.</span>
      </div>
      <div class="search-results-layout">
        <div>
          <h2>Top Talent Matches</h2>
          <div class="talent-grid two-col">
            ${talents.map((talent) => talentCard(talent, true)).join("")}
          </div>
        </div>
        <aside>
          <h2>Similar Requests</h2>
          ${requests.map(requestCard).join("")}
        </aside>
      </div>
    </section>
  `;
}
