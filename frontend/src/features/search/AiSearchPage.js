import { pageTitle } from "../../shared/ui/index.js";

export function AiSearchPage() {
  return `
    <section class="section ai-match-page" data-ai-match-page>
      ${pageTitle("AI Search", "필요한 재능과 요청을 자연어로 찾아보세요.", "검색 문장을 분석해 가능한 상세 조건을 자동으로 채웁니다.")}

      <form class="ai-match-panel" data-ai-match-form>
        <div class="search-box wide">
          <span class="spark">✦</span>
          <input name="query" type="search" placeholder="예: 50만원 이하 Spring 백엔드 개발자 찾아줘" aria-label="AI search" required />
          <button type="submit">Search</button>
        </div>

        <div class="ai-match-target-tabs" role="tablist" aria-label="검색 대상">
          <button type="button" class="active" data-ai-target="TALENT">재능글 찾기</button>
          <button type="button" data-ai-target="REQUEST">요청글 찾기</button>
        </div>

        <div class="ai-match-filters">
          <label>
            <span>카테고리</span>
            <select name="categoryId" data-ai-category-select>
              <option value="">전체</option>
            </select>
          </label>

          <label data-ai-talent-filter>
            <span>최대 가격</span>
            <input name="maxPrice" type="number" min="0" step="1000" placeholder="예: 500000" />
          </label>

          <label data-ai-talent-filter>
            <span>최대 기간</span>
            <input name="maxEstimatedDuration" type="number" min="1" step="1" placeholder="예: 7" />
          </label>

          <label data-ai-talent-filter>
            <span>기간 단위</span>
            <select name="durationUnit">
              <option value="">전체</option>
              <option value="DAY">일</option>
              <option value="WEEK">주</option>
              <option value="MONTH">개월</option>
            </select>
          </label>

          <label data-ai-request-filter hidden>
            <span>최소 예산</span>
            <input name="minBudget" type="number" min="0" step="1000" placeholder="예: 300000" />
          </label>

          <label data-ai-request-filter hidden>
            <span>최대 예산</span>
            <input name="maxBudget" type="number" min="0" step="1000" placeholder="예: 700000" />
          </label>

          <label data-ai-request-filter hidden>
            <span>마감 시작</span>
            <input name="dueDateFrom" type="date" />
          </label>

          <label data-ai-request-filter hidden>
            <span>마감 종료</span>
            <input name="dueDateTo" type="date" />
          </label>
        </div>
      </form>

      <div class="ai-insight" data-ai-match-insight>
        <strong>AI Insight</strong>
        <span>검색어를 입력하면 분석된 조건과 추천 결과를 보여드립니다.</span>
      </div>

      <div class="ai-match-results" data-ai-match-results>
        <article class="empty-state">
          <span>READY</span>
          <h3>검색을 시작해보세요.</h3>
          <p>재능글과 요청글 중 상황에 맞는 대상을 선택하거나, 검색 문장으로 자동 전환할 수 있습니다.</p>
        </article>
      </div>
    </section>
  `;
}
