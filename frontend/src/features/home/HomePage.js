import { pageTitle } from "../../shared/ui/index.js";

export function HomePage() {
  return `
    <section class="hero">
      ${pageTitle("TalentPulse", "필요한 사람과 일을 빠르게 연결하세요.", "프로젝트를 맡길 전문가를 찾거나, 내가 제공할 수 있는 서비스를 등록할 수 있습니다.")}
      <form class="search-box home-ai-search" data-search-form>
        <span class="spark ai-search-logo" aria-hidden="true">✦</span>
        <input name="query" type="search" placeholder="필요한 작업이나 전문가를 입력해보세요" aria-label="AI search" />
        <button type="submit">Search</button>
      </form>
      <div class="primary-flow" data-primary-flow>
        <button class="flow-trigger" type="button" aria-expanded="false" aria-controls="hire-options" data-flow-trigger="hire">
          사람이 필요해요
        </button>
        <button class="flow-trigger" type="button" aria-expanded="false" aria-controls="work-options" data-flow-trigger="work">
          일이 필요해요
        </button>
        <div class="flow-options" id="hire-options" data-flow-options="hire" hidden>
          <a href="#/talents">전문가 찾기</a>
          <a href="#/request-new">요청글 작성</a>
        </div>
        <div class="flow-options" id="work-options" data-flow-options="work" hidden>
          <a href="#/requests">일 찾기</a>
          <a href="#/talent-new">재능 등록</a>
        </div>
      </div>
    </section>
  `;
}
