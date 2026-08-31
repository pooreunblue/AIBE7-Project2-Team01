import { fetchCategories } from "../category/categoryApi.js";
import { escapeHtml, safeImageUrl, setSafeHtml } from "../../shared/security/xss.js";
import { formatMoney, pageTitle } from "../../shared/ui/index.js";
import { searchAiMatches } from "./matchingApi.js";

const DEFAULT_TARGET_TYPE = "TALENT";

export function AiSearchPage() {
  return `
    <section class="section ai-search-page" data-ai-search-page>
      ${pageTitle("AI Matching", "어떤 도움이 필요한가요?", "자연어 검색과 정확한 조건을 함께 사용해 적합한 글을 추천합니다.")}

      <form class="ai-match-form" data-ai-match-form>
        <div class="ai-target-control" role="radiogroup" aria-label="검색 대상">
          <label>
            <input type="radio" name="targetType" value="TALENT" checked />
            <span>전문가 찾기</span>
          </label>
          <label>
            <input type="radio" name="targetType" value="REQUEST" />
            <span>요청글 찾기</span>
          </label>
        </div>

        <div class="search-box wide ai-query-box">
          <span class="spark" aria-hidden="true">✦</span>
          <input name="query" type="search" placeholder="예: 50만원 이하 Spring 백엔드 개발" aria-label="AI 검색" required />
          <button type="submit">검색</button>
        </div>

        <div class="ai-condition-bar">
          <label class="field ai-category-field">
            <span>카테고리</span>
            <select name="categoryId" data-ai-category>
              <option value="">전체 카테고리</option>
            </select>
          </label>

          <div class="ai-target-fields" data-ai-target-fields="TALENT">
            <label class="field">
              <span>최대 가격</span>
              <input type="number" name="maxPrice" min="0" step="1000" placeholder="제한 없음" />
            </label>
            <label class="field">
              <span>최대 작업 기간</span>
              <input type="number" name="maxEstimatedDuration" min="1" placeholder="제한 없음" />
            </label>
            <label class="field compact-field">
              <span>기간 단위</span>
              <select name="durationUnit">
                <option value="DAY">일</option>
                <option value="WEEK">주</option>
                <option value="MONTH">개월</option>
              </select>
            </label>
          </div>

          <div class="ai-target-fields" data-ai-target-fields="REQUEST" hidden>
            <label class="field">
              <span>최소 예산</span>
              <input type="number" name="minBudget" min="0" step="1000" placeholder="제한 없음" disabled />
            </label>
            <label class="field">
              <span>최대 예산</span>
              <input type="number" name="maxBudget" min="0" step="1000" placeholder="제한 없음" disabled />
            </label>
            <label class="field">
              <span>마감일 시작</span>
              <input type="date" name="dueDateFrom" disabled />
            </label>
            <label class="field">
              <span>마감일 종료</span>
              <input type="date" name="dueDateTo" disabled />
            </label>
          </div>
        </div>
      </form>

      <p class="ai-search-status" data-ai-search-status aria-live="polite">
        검색 대상과 조건을 선택한 뒤 필요한 내용을 자연스럽게 입력해 주세요.
      </p>
      <div class="ai-match-results" data-ai-match-results></div>
    </section>
  `;
}

export function initAiSearchPage() {
  const page = document.querySelector("[data-ai-search-page]");
  if (!page) return;

  const form = page.querySelector("[data-ai-match-form]");
  const status = page.querySelector("[data-ai-search-status]");
  const results = page.querySelector("[data-ai-match-results]");
  const submitButton = form.querySelector("button[type='submit']");

  applyInitialSearch(form);
  bindTargetType(form);
  loadCategories(form);

  form.addEventListener("submit", async (event) => {
    event.preventDefault();
    const payload = buildMatchPayload(form);

    if (!payload.query) {
      status.textContent = "검색어를 입력해 주세요.";
      form.elements.query.focus();
      return;
    }

    submitButton.disabled = true;
    status.textContent = "AI가 의미와 조건을 함께 비교하고 있습니다.";
    results.replaceChildren();

    try {
      const response = await searchAiMatches(payload);
      renderMatchResults(results, response);
      const count = response?.candidates?.length || 0;
      status.textContent = count
        ? `${count}개의 추천 결과를 찾았습니다.`
        : "조건에 맞는 추천 결과가 없습니다. 검색어나 조건을 조정해 보세요.";
    } catch (error) {
      status.textContent = error.message || "AI 매칭 검색에 실패했습니다.";
      setSafeHtml(results, renderSearchError(error));
    } finally {
      submitButton.disabled = false;
    }
  });

  if (form.elements.query.value.trim()) {
    form.requestSubmit();
  }
}

function applyInitialSearch(form) {
  const params = hashSearchParams();
  form.elements.query.value = params.get("query") || "";

  const targetType = params.get("targetType");
  if (targetType === "REQUEST") {
    form.elements.targetType.value = "REQUEST";
  }
}

function bindTargetType(form) {
  const update = () => {
    const selectedType = form.elements.targetType.value || DEFAULT_TARGET_TYPE;
    form.querySelectorAll("[data-ai-target-fields]").forEach((group) => {
      const isActive = group.dataset.aiTargetFields === selectedType;
      group.hidden = !isActive;
      group.querySelectorAll("input, select").forEach((control) => {
        control.disabled = !isActive;
      });
    });
  };

  form.querySelectorAll('input[name="targetType"]').forEach((radio) => {
    radio.addEventListener("change", update);
  });
  update();
}

async function loadCategories(form) {
  const select = form.querySelector("[data-ai-category]");
  try {
    const categories = await fetchCategories();
    setSafeHtml(select, `
      <option value="">전체 카테고리</option>
      ${categories.map((category) => `
        <option value="${escapeHtml(category.categoryId)}">${escapeHtml(category.name)}</option>
      `).join("")}
    `);
  } catch {
    setSafeHtml(select, '<option value="">카테고리를 불러오지 못했습니다</option>');
  }
}

function buildMatchPayload(form) {
  const data = new FormData(form);
  const targetType = data.get("targetType") || DEFAULT_TARGET_TYPE;
  const condition = {
    categoryId: optionalText(data.get("categoryId")),
  };

  if (targetType === "TALENT") {
    condition.maxPrice = optionalNumber(data.get("maxPrice"));
    condition.maxEstimatedDuration = optionalNumber(data.get("maxEstimatedDuration"));
    condition.durationUnit = condition.maxEstimatedDuration
      ? optionalText(data.get("durationUnit")) || "DAY"
      : null;
  } else {
    condition.minBudget = optionalNumber(data.get("minBudget"));
    condition.maxBudget = optionalNumber(data.get("maxBudget"));
    condition.dueDateFrom = optionalText(data.get("dueDateFrom"));
    condition.dueDateTo = optionalText(data.get("dueDateTo"));
  }

  return {
    query: String(data.get("query") || "").trim(),
    targetType,
    condition,
    limit: 5,
  };
}

function renderMatchResults(container, response) {
  const candidates = response?.candidates || [];
  if (!candidates.length) {
    container.replaceChildren();
    return;
  }

  const targetLabel = response.targetType === "REQUEST" ? "요청글" : "재능글";
  setSafeHtml(container, `
    <div class="ai-result-heading">
      <div>
        <span class="kicker">${escapeHtml(targetLabel)} 추천</span>
        <h2>조건에 가까운 결과</h2>
      </div>
      <span>${candidates.length}개</span>
    </div>
    <div class="ai-result-list">
      ${candidates.map(renderMatchCandidate).join("")}
    </div>
  `);
}

function renderMatchCandidate(candidate) {
  const isRequest = candidate.targetType === "REQUEST";
  const href = isRequest
    ? `#/request/${candidate.targetId}`
    : `#/talent/${candidate.targetId}`;
  const imageUrl = safeImageUrl(candidate.thumbnailUrl);
  const reason = String(candidate.recommendationReason || "").trim();
  const score = Math.round(Number(candidate.matchScore || 0) * 100);

  return `
    <article class="ai-result-card ${imageUrl ? "has-media" : "text-only"}">
      ${imageUrl ? `
        <a class="ai-result-thumbnail" href="${escapeHtml(href)}" aria-label="${escapeHtml(candidate.title)} 상세 보기">
          <img src="${escapeHtml(imageUrl)}" alt="" loading="lazy" />
        </a>
      ` : ""}
      <div class="ai-result-body">
        <div class="ai-result-meta">
          <span>${escapeHtml(candidate.categoryName || (isRequest ? "요청글" : "재능글"))}</span>
          <strong>적합도 ${score}%</strong>
        </div>
        <h3><a href="${escapeHtml(href)}">${escapeHtml(candidate.title || "제목 없음")}</a></h3>
        <p>${escapeHtml(markdownExcerpt(candidate.content))}</p>
        ${reason ? `<div class="ai-recommendation-reason"><strong>추천 이유</strong><span>${escapeHtml(reason)}</span></div>` : ""}
        <div class="ai-result-footer">
          <div>
            <span>${escapeHtml(candidate.authorNickname || "작성자")}</span>
            <strong>${isRequest ? formatBudget(candidate) : formatOptionalMoney(candidate.price)}</strong>
          </div>
          <a class="button quiet" href="${escapeHtml(href)}">상세보기</a>
        </div>
      </div>
    </article>
  `;
}

function renderSearchError(error) {
  const message = error?.status === 400
    ? "검색 대상과 입력 조건이 맞는지 확인해 주세요."
    : "잠시 후 다시 검색해 주세요.";
  return `
    <div class="ai-result-empty">
      <strong>검색 결과를 가져오지 못했습니다.</strong>
      <span>${escapeHtml(message)}</span>
    </div>
  `;
}

function hashSearchParams() {
  const query = window.location.hash.split("?")[1] || "";
  return new URLSearchParams(query);
}

function optionalText(value) {
  const text = String(value || "").trim();
  return text || null;
}

function optionalNumber(value) {
  if (value == null || value === "") return null;
  const number = Number(value);
  return Number.isFinite(number) ? number : null;
}

function markdownExcerpt(markdown) {
  return String(markdown || "")
    .replace(/!\[[^\]]*]\([^)]*\)/g, "")
    .replace(/\[([^\]]+)]\([^)]*\)/g, "$1")
    .replace(/```[\s\S]*?```/g, "")
    .replace(/`([^`]+)`/g, "$1")
    .replace(/^#{1,6}\s*/gm, "")
    .replace(/^>\s*/gm, "")
    .replace(/[*_~]/g, "")
    .replace(/^\s*[-+]\s+/gm, "")
    .replace(/^\s*\d+\.\s+/gm, "")
    .replace(/\s+/g, " ")
    .trim()
    .slice(0, 180);
}

function formatOptionalMoney(value) {
  return value == null ? "가격 협의" : formatMoney(Number(value));
}

function formatBudget(candidate) {
  if (candidate.budgetMin == null || candidate.budgetMax == null) return "예산 협의";
  return `${formatMoney(Number(candidate.budgetMin))} - ${formatMoney(Number(candidate.budgetMax))}`;
}
