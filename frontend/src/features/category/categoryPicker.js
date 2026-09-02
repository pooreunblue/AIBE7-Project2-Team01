import { fetchCategories } from "./categoryApi.js";
import { appendSafeHtml, escapeHtml, setSafeHtml } from "../../shared/security/xss.js";

const DEFAULT_LABEL = "카테고리 선택";

export function bindCategoryPicker({
  select,
  trigger,
  title = "카테고리 선택",
  includeAll = false,
  onChange,
} = {}) {
  if (!select || !trigger || trigger.dataset.categoryPickerBound === "true") {
    return;
  }

  trigger.dataset.categoryPickerBound = "true";
  syncCategoryPickerButton(select, trigger, includeAll);
  select.addEventListener("change", () => syncCategoryPickerButton(select, trigger, includeAll));

  trigger.addEventListener("click", () => {
    openCategoryPicker({
      selectedId: select.value,
      title,
      includeAll,
      onSelect: (category) => {
        setCategorySelection(select, category.categoryId, category.name);
        syncCategoryPickerButton(select, trigger, includeAll);
        select.dispatchEvent(new Event("change", { bubbles: true }));
        onChange?.(category);
      },
    });
  });
}

export function setCategorySelection(select, categoryId, name = "") {
  if (!select) {
    return;
  }

  const id = String(categoryId || "");
  const label = name || DEFAULT_LABEL;
  if (id && ![...select.options].some((option) => option.value === id)) {
    select.append(new Option(label, id));
  }

  select.value = id;
  select.dataset.categoryName = id ? label : "";
}

export function syncCategoryPickerButton(select, trigger, includeAll = false) {
  if (!select || !trigger) {
    return;
  }

  const label = resolveCategoryLabel(select, includeAll);
  const labelEl = trigger.querySelector("[data-category-picker-label]");
  if (labelEl) {
    labelEl.textContent = label;
  } else {
    trigger.textContent = label;
  }
}

export function resolveCategoryLabel(select, includeAll = false) {
  if (!select?.value) {
    return includeAll ? "전체 카테고리" : DEFAULT_LABEL;
  }

  const selected = select.options[select.selectedIndex];
  return selected?.textContent || select.dataset.categoryName || DEFAULT_LABEL;
}

function openCategoryPicker({ selectedId = "", title, includeAll, onSelect }) {
  const layer = document.querySelectorAll(".modal-backdrop:not([hidden])").length;
  appendSafeHtml(document.body, "beforeend", `
    <div class="modal-backdrop category-picker-backdrop" data-category-picker-modal style="z-index: ${60 + layer * 10}">
      <div class="charge-modal category-picker-modal" role="dialog" aria-modal="true" aria-label="${escapeHtml(title)}">
        <div class="modal-head">
          <div>
            <span class="kicker">Category</span>
            <h2>${escapeHtml(title)}</h2>
          </div>
          <button class="modal-close" type="button" data-category-picker-close aria-label="카테고리 선택 팝업 닫기">x</button>
        </div>
        <div class="category-picker-list" data-category-picker-list>
          <p>카테고리를 불러오는 중입니다.</p>
        </div>
      </div>
    </div>
  `);

  const backdrop = [...document.querySelectorAll("[data-category-picker-modal]")].at(-1);
  const list = backdrop.querySelector("[data-category-picker-list]");
  document.body.classList.add("modal-open");

  const close = () => {
    backdrop.remove();
    syncBodyScrollLock();
    document.removeEventListener("keydown", onKeydown);
  };

  const onKeydown = (event) => {
    if (event.key === "Escape" && isTopCategoryModal(backdrop)) {
      close();
    }
  };

  backdrop.addEventListener("click", (event) => {
    if (event.target === backdrop || event.target.closest("[data-category-picker-close]")) {
      close();
    }
  });

  list.addEventListener("click", (event) => {
    const button = event.target.closest("[data-category-picker-id]");
    if (!button) {
      return;
    }

    onSelect?.({
      categoryId: button.dataset.categoryPickerId,
      name: button.dataset.categoryPickerName || "",
    });
    close();
  });

  document.addEventListener("keydown", onKeydown);
  loadCategoryItems(list, selectedId, includeAll);
}

async function loadCategoryItems(list, selectedId, includeAll) {
  try {
    const categories = await fetchCategories();
    const items = includeAll ? [{ categoryId: "", name: "전체 카테고리" }, ...categories] : categories;
    if (!items.length) {
      setSafeHtml(list, `<p>등록된 카테고리가 없습니다.</p>`);
      return;
    }

    setSafeHtml(list, items.map((category) => {
      const id = String(category.categoryId || "");
      const isSelected = String(selectedId || "") === id;
      return `
        <button
          type="button"
          class="category-picker-item ${isSelected ? "is-selected" : ""}"
          data-category-picker-id="${escapeHtml(id)}"
          data-category-picker-name="${escapeHtml(category.name)}"
          aria-pressed="${isSelected ? "true" : "false"}"
        >
          <span>${escapeHtml(category.name)}</span>
          ${isSelected ? `<strong>선택됨</strong>` : ""}
        </button>
      `;
    }).join(""));
  } catch {
    setSafeHtml(list, `<p>카테고리를 불러오지 못했습니다.</p>`);
  }
}

function isTopCategoryModal(backdrop) {
  const modals = [...document.querySelectorAll("[data-category-picker-modal]")];
  return modals.at(-1) === backdrop;
}

function syncBodyScrollLock() {
  const hasOpenModal = document.querySelector(".modal-backdrop:not([hidden])");
  document.body.classList.toggle("modal-open", Boolean(hasOpenModal));
}
