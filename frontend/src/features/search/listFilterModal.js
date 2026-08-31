import { fetchCategories } from "../category/categoryApi.js";
import { appendSafeHtml, escapeHtml, setSafeHtml } from "../../shared/security/xss.js";

// AI 검색(#/ai-search)의 조건 필드와 동일한 항목을 모달로 제공한다.
// targetType: "TALENT" | "REQUEST"
// current: 이미 적용 중인 조건 객체
// onApply(conditions): "적용" 시 정규화된 조건 객체 전달
export function openListFilterModal({ targetType = "TALENT", current = {}, onApply } = {}) {
  closeListFilterModal();

  const isTalent = targetType === "TALENT";
  appendSafeHtml(document.body, "beforeend", `
    <div class="modal-backdrop list-filter-backdrop" data-list-filter-modal>
      <div class="charge-modal list-filter-modal" role="dialog" aria-modal="true" aria-label="검색 필터">
        <div class="modal-head">
          <div>
            <span class="kicker">Filters</span>
            <h2>검색 조건</h2>
          </div>
          <button class="modal-close" type="button" data-list-filter-close aria-label="닫기">x</button>
        </div>
        <form class="list-filter-form" data-list-filter-form>
          <label class="field">
            <span>카테고리</span>
            <select name="categoryId" data-list-filter-category>
              <option value="">전체 카테고리</option>
            </select>
          </label>
          ${isTalent ? talentFields(current) : requestFields(current)}
          <div class="form-actions">
            <button class="button quiet" type="button" data-list-filter-reset>초기화</button>
            <button class="button primary" type="submit">적용</button>
          </div>
        </form>
      </div>
    </div>
  `);

  const backdrop = document.querySelector("[data-list-filter-modal]");
  const form = backdrop.querySelector("[data-list-filter-form]");
  const categorySelect = backdrop.querySelector("[data-list-filter-category]");

  loadCategoryOptions(categorySelect, current.categoryId);

  backdrop.addEventListener("click", (event) => {
    if (event.target === backdrop || event.target.closest("[data-list-filter-close]")) {
      closeListFilterModal();
    }
  });

  form.querySelector("[data-list-filter-reset]").addEventListener("click", () => {
    form.reset();
    categorySelect.value = "";
  });

  form.addEventListener("submit", (event) => {
    event.preventDefault();
    onApply?.(readConditions(form, targetType));
    closeListFilterModal();
  });
}

export function closeListFilterModal() {
  document.querySelector("[data-list-filter-modal]")?.remove();
}

function talentFields(current) {
  return `
    <label class="field">
      <span>최대 가격</span>
      <input type="number" name="maxPrice" min="0" step="1000" placeholder="제한 없음" value="${numAttr(current.maxPrice)}" />
    </label>
    <label class="field">
      <span>최대 작업 기간</span>
      <input type="number" name="maxEstimatedDuration" min="1" placeholder="제한 없음" value="${numAttr(current.maxEstimatedDuration)}" />
    </label>
    <label class="field">
      <span>기간 단위</span>
      <select name="durationUnit">
        <option value="DAY"${current.durationUnit === "DAY" ? " selected" : ""}>일</option>
        <option value="WEEK"${current.durationUnit === "WEEK" ? " selected" : ""}>주</option>
        <option value="MONTH"${current.durationUnit === "MONTH" ? " selected" : ""}>개월</option>
      </select>
    </label>
  `;
}

function requestFields(current) {
  return `
    <label class="field">
      <span>최소 예산</span>
      <input type="number" name="minBudget" min="0" step="1000" placeholder="제한 없음" value="${numAttr(current.minBudget)}" />
    </label>
    <label class="field">
      <span>최대 예산</span>
      <input type="number" name="maxBudget" min="0" step="1000" placeholder="제한 없음" value="${numAttr(current.maxBudget)}" />
    </label>
    <label class="field">
      <span>마감일 시작</span>
      <input type="date" name="dueDateFrom" value="${escapeHtml(current.dueDateFrom || "")}" />
    </label>
    <label class="field">
      <span>마감일 종료</span>
      <input type="date" name="dueDateTo" value="${escapeHtml(current.dueDateTo || "")}" />
    </label>
  `;
}

function readConditions(form, targetType) {
  const data = new FormData(form);
  const conditions = { categoryId: text(data.get("categoryId")) };

  if (targetType === "TALENT") {
    conditions.maxPrice = number(data.get("maxPrice"));
    conditions.maxEstimatedDuration = number(data.get("maxEstimatedDuration"));
    conditions.durationUnit = conditions.maxEstimatedDuration ? (text(data.get("durationUnit")) || "DAY") : null;
  } else {
    conditions.minBudget = number(data.get("minBudget"));
    conditions.maxBudget = number(data.get("maxBudget"));
    conditions.dueDateFrom = text(data.get("dueDateFrom"));
    conditions.dueDateTo = text(data.get("dueDateTo"));
  }
  return conditions;
}

async function loadCategoryOptions(select, selectedId) {
  try {
    const categories = await fetchCategories();
    setSafeHtml(select, `
      <option value="">전체 카테고리</option>
      ${categories.map((category) => `
        <option value="${escapeHtml(category.categoryId)}"${String(category.categoryId) === String(selectedId || "") ? " selected" : ""}>${escapeHtml(category.name)}</option>
      `).join("")}
    `);
  } catch {
    setSafeHtml(select, '<option value="">카테고리를 불러오지 못했습니다</option>');
  }
}

function text(value) {
  const trimmed = String(value || "").trim();
  return trimmed || null;
}

function number(value) {
  if (value == null || value === "") return null;
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : null;
}

function numAttr(value) {
  return value == null ? "" : escapeHtml(String(value));
}
