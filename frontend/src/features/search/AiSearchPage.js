import { fetchCategories } from "../category/categoryApi.js";
import { escapeHtml, safeImageUrl, safeUrl, setSafeHtml } from "../../shared/security/xss.js";
import { formatMoney } from "../../shared/ui/index.js";
import { analyzeAiMatchQuery, searchAiMatches } from "./matchingApi.js";

const DEFAULT_TARGET_TYPE = "TALENT";

export function AiSearchPage() {
  return `
    <section class="section ai-search-page" data-ai-search-page>
      <form class="ai-match-form" data-ai-match-form>
        <div class="search-box wide ai-query-box">
          <span class="spark ai-search-logo" aria-hidden="true">✦</span>
          <input name="query" type="search" placeholder="예: 50만원 이하 Spring 백엔드 개발자 찾아줘" aria-label="검색어" required />
          <button type="submit">Search</button>
        </div>

        <button class="ai-filter-toggle" type="button" data-ai-filter-toggle aria-expanded="false" aria-controls="ai-match-options">
          <span>상세 조건</span>
          <strong aria-hidden="true">⌄</strong>
        </button>

        <div class="ai-match-options" id="ai-match-options" data-ai-match-options hidden>
          <div class="ai-target-control" role="radiogroup" aria-label="검색 대상">
            <label>
              <input type="radio" name="targetType" value="TALENT" checked />
              <span>재능글 찾기</span>
            </label>
            <label>
              <input type="radio" name="targetType" value="REQUEST" />
              <span>요청글 찾기</span>
            </label>
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
                  <option value="">전체</option>
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
        </div>
      </form>

      <div class="ai-match-results" data-ai-match-results></div>
    </section>
  `;
}

export function initAiSearchPage() {
  const page = document.querySelector("[data-ai-search-page]");
  if (!page) return;

  const form = page.querySelector("[data-ai-match-form]");
  if (!form) return;

  const params = hashSearchParams();
  const categoriesReady = loadCategories(form);

  applyInitialSearch(form, params);
  bindTargetType(form);
  bindFilterToggle(form);
  bindSearchActions(form);

  const initialQuery = form.elements.query.value.trim();
  const results = getResults();
  if (initialQuery) {
    categoriesReady.finally(() => {
      runAiSearch(form);
    });
    return;
  }

  renderInitialState(results);
}

function bindSearchActions(form) {
  form.addEventListener("submit", (event) => {
    event.preventDefault();
    runAiSearch(form);
  });
}

function bindFilterToggle(form) {
  const toggle = form.querySelector("[data-ai-filter-toggle]");
  const options = form.querySelector("[data-ai-match-options]");
  if (!toggle || !options) {
    return;
  }

  toggle.addEventListener("click", () => {
    const isExpanded = toggle.getAttribute("aria-expanded") === "true";
    setFilterExpanded(toggle, options, !isExpanded);
  });
}

function setFilterExpanded(toggle, options, expanded) {
  toggle.setAttribute("aria-expanded", String(expanded));
  options.hidden = !expanded;

  const icon = toggle.querySelector("strong");
  if (icon) {
    icon.textContent = expanded ? "⌃" : "⌄";
  }
}

function applyInitialSearch(form, params) {
  form.elements.query.value = params.get("query") || "";

  const targetType = params.get("targetType");
  if (targetType === "REQUEST") {
    form.elements.targetType.value = "REQUEST";
  }
}

function bindTargetType(form) {
  const update = () => {
    const selectedType = getSelectedTargetType(form);
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

async function runAiSearch(form) {
  const query = readQuery(form);
  if (!query) {
    focusQuery(form);
    return;
  }

  const results = getResults();
  const submitButton = form.querySelector("button[type='submit']");
  setButtonsDisabled([submitButton], true);
  renderLoading(results, "AI SEARCH", "AI가 검색 문장을 분석하고 있습니다.");

  let analysis = null;
  try {
    analysis = await analyzeAiMatchQuery(query);
    applyAnalysis(form, analysis);
  } catch {
    renderLoading(results, "AI SEARCH", "분석을 건너뛰고 현재 조건으로 검색합니다.");
  }

  try {
    const payload = buildAiMatchPayload(form, query, analysis);
    const response = await searchAiMatches(payload);
    renderAiResults(results, response, analysis);
  } catch (error) {
    renderError(results, "AI 매칭 결과를 불러오지 못했습니다.", error);
  } finally {
    setButtonsDisabled([submitButton], false);
  }
}

function applyAnalysis(form, analysis) {
  if (!analysis) return;

  resetConditions(form);
  setTargetType(form, analysis.targetType);
  form.elements.query.value = analysis.semanticQuery || analysis.originalQuery || "";

  const condition = analysis.condition || {};
  setControlValue(form, "categoryId", condition.categoryId);
  setControlValue(form, "maxPrice", condition.maxPrice);
  setControlValue(form, "maxEstimatedDuration", condition.maxEstimatedDuration);
  setControlValue(form, "durationUnit", condition.durationUnit);
  setControlValue(form, "minBudget", condition.minBudget);
  setControlValue(form, "maxBudget", condition.maxBudget);
  setControlValue(form, "dueDateFrom", condition.dueDateFrom);
  setControlValue(form, "dueDateTo", condition.dueDateTo);
}

function buildAiMatchPayload(form, originalQuery, analysis) {
  const targetType = getSelectedTargetType(form);
  const semanticQuery = analysis?.semanticQuery || originalQuery;
  return {
    query: semanticQuery,
    targetType,
    condition: buildCondition(form, targetType),
    limit: 5,
  };
}

function buildCondition(form, targetType) {
  const data = new FormData(form);
  const condition = {
    categoryId: optionalText(data.get("categoryId")),
  };

  if (targetType === "TALENT") {
    condition.maxPrice = optionalNumber(data.get("maxPrice"));
    condition.maxEstimatedDuration = optionalNumber(data.get("maxEstimatedDuration"));
    condition.durationUnit = condition.maxEstimatedDuration
      ? optionalText(data.get("durationUnit")) || "DAY"
      : null;
    return condition;
  }

  condition.minBudget = optionalNumber(data.get("minBudget"));
  condition.maxBudget = optionalNumber(data.get("maxBudget"));
  condition.dueDateFrom = optionalText(data.get("dueDateFrom"));
  condition.dueDateTo = optionalText(data.get("dueDateTo"));
  return condition;
}

function renderAiResults(container, response, analysis) {
  const candidates = response?.candidates || [];
  if (!candidates.length) {
    setSafeHtml(container, renderSearchState("EMPTY", "조건에 맞는 결과가 없습니다.", "검색어를 넓히거나 가격, 예산, 기간 조건을 조정해보세요."));
    return;
  }

  const summary = renderAnalysisSummary(analysis);
  setSafeHtml(container, `
    <div class="ai-result-heading">
      <div>
        <span class="kicker">AI ${escapeHtml(targetLabel(response.targetType))} 추천</span>
        <h2>조건에 가까운 결과</h2>
        ${summary}
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
  const score = candidate.matchScore == null
    ? null
    : Math.round(Number(candidate.matchScore || 0) * 100);

  return `
    <article class="ai-result-card ${imageUrl ? "has-media" : "text-only"}">
      ${imageUrl ? `
        <a class="ai-result-thumbnail" href="${escapeHtml(safeUrl(href))}" aria-label="${escapeHtml(candidate.title)} 상세 보기">
          <img src="${escapeHtml(imageUrl)}" alt="" loading="lazy" />
        </a>
      ` : ""}
      <div class="ai-result-body">
        <div class="ai-result-meta">
          <span>${escapeHtml(candidate.categoryName || (isRequest ? "요청글" : "재능글"))}</span>
          ${score == null ? "" : `<strong>적합도 ${score}%</strong>`}
        </div>
        <h3><a href="${escapeHtml(safeUrl(href))}">${escapeHtml(candidate.title || "제목 없음")}</a></h3>
        <p>${escapeHtml(markdownExcerpt(candidate.content))}</p>
        ${reason ? `<div class="ai-recommendation-reason"><strong>추천 이유</strong><span>${escapeHtml(reason)}</span></div>` : ""}
        <div class="ai-result-footer">
          <div>
            <span>${escapeHtml(candidate.authorNickname || "작성자")}</span>
            <strong>${isRequest ? formatBudget(candidate) : formatOptionalMoney(candidate.price)}</strong>
          </div>
          <a class="button quiet" href="${escapeHtml(safeUrl(href))}">상세보기</a>
        </div>
      </div>
    </article>
  `;
}

function renderLoading(container, label, message) {
  setSafeHtml(container, `
    <article class="ai-search-state is-loading">
      <span>${escapeHtml(label)}</span>
      <h3>검색 결과를 준비하고 있습니다.</h3>
      <p>${escapeHtml(message)}</p>
      <div class="ai-search-progress" aria-hidden="true"><i></i></div>
    </article>
  `);
}

function renderInitialState(container) {
  setSafeHtml(
    container,
    renderSearchState(
      "AI SEARCH",
      "찾고 싶은 재능이나 요청을 입력해보세요.",
      "예산, 기간, 카테고리 같은 조건을 문장으로 적으면 AI가 알맞은 검색 조건과 후보를 함께 찾아줍니다."
    )
  );
}

function renderError(container, title, error) {
  const message = error?.status === 400
    ? "검색 대상과 입력 조건이 맞는지 확인해 주세요."
    : error?.message || "잠시 후 다시 검색해 주세요.";
  setSafeHtml(container, renderSearchState("ERROR", title, message));
}

function renderSearchState(label, title, message) {
  return `
    <article class="ai-search-state">
      <span>${escapeHtml(label)}</span>
      <h3>${escapeHtml(title)}</h3>
      <p>${escapeHtml(message)}</p>
    </article>
  `;
}

function renderAnalysisSummary(analysis) {
  if (!analysis) {
    return "";
  }

  const details = [
    targetLabel(analysis.targetType),
    analysis.categoryName || "전체 카테고리",
    ...conditionLabels(analysis.condition || {}),
  ].filter(Boolean);

  return `<p class="ai-result-summary">${escapeHtml(details.join(" · "))}</p>`;
}

function conditionLabels(condition) {
  const labels = [];
  if (condition.maxPrice != null) {
    labels.push(`최대 ${formatMoney(Number(condition.maxPrice))}`);
  }
  if (condition.maxEstimatedDuration != null) {
    labels.push(`최대 ${condition.maxEstimatedDuration}${durationUnitLabel(condition.durationUnit)}`);
  }
  if (condition.minBudget != null || condition.maxBudget != null) {
    labels.push(`${formatMoney(Number(condition.minBudget || 0))} - ${formatMoney(Number(condition.maxBudget || 0))}`);
  }
  if (condition.dueDateFrom || condition.dueDateTo) {
    labels.push(`${condition.dueDateFrom || "시작일 무관"} ~ ${condition.dueDateTo || "종료일 무관"}`);
  }
  return labels;
}

function resetConditions(form) {
  for (const name of ["categoryId", "maxPrice", "maxEstimatedDuration", "durationUnit", "minBudget", "maxBudget", "dueDateFrom", "dueDateTo"]) {
    setControlValue(form, name, "");
  }
}

function setTargetType(form, targetType) {
  const resolvedTargetType = targetType === "REQUEST" ? "REQUEST" : "TALENT";
  form.elements.targetType.value = resolvedTargetType;
  form.querySelectorAll('input[name="targetType"]').forEach((radio) => {
    radio.checked = radio.value === resolvedTargetType;
  });
  form.querySelector('input[name="targetType"]')?.dispatchEvent(new Event("change", { bubbles: true }));
}

function setControlValue(form, name, value) {
  const control = form.elements[name];
  if (!control || value == null) {
    return;
  }
  control.value = String(value);
}

function getSelectedTargetType(form) {
  return form.elements.targetType.value || DEFAULT_TARGET_TYPE;
}

function readQuery(form) {
  return String(form.elements.query?.value || "").trim();
}

function focusQuery(form) {
  form.elements.query?.focus();
}

function getResults() {
  return document.querySelector("[data-ai-match-results]");
}

function setButtonsDisabled(buttons, disabled) {
  buttons.forEach((button) => {
    if (button) {
      button.disabled = disabled;
    }
  });
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
  if (value == null || value === "") {
    return null;
  }
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
  if (candidate.budgetMin == null || candidate.budgetMax == null) {
    return "예산 협의";
  }
  return `${formatMoney(Number(candidate.budgetMin))} - ${formatMoney(Number(candidate.budgetMax))}`;
}

function targetLabel(targetType) {
  if (targetType === "REQUEST") {
    return "요청글";
  }
  return "재능글";
}

function durationUnitLabel(unit) {
  const labels = { DAY: "일", WEEK: "주", MONTH: "개월" };
  return labels[unit] || "일";
}
