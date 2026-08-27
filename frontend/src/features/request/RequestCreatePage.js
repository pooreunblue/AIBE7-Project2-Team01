import { button, formField, pageTitle } from "../../shared/ui/index.js";

export function RequestCreatePage() {
  return `
    <section class="form-page">
      ${pageTitle("Create Request", "전문가/프리랜서를 구하는 요청글 작성", "필요한 작업 범위와 예산, 희망 일정을 정리해 주세요.")}
      <form class="editor-card" data-request-create-form>
        ${formField("제목", `<input name="title" type="text" placeholder="예: 쇼핑몰 상세페이지 디자인이 필요해요" required />`)}
        ${formField("설명", `<textarea name="content" rows="6" placeholder="필요한 작업, 참고 자료, 원하는 결과물을 입력하세요." required></textarea>`)}
        ${formField("카테고리", `<select name="categoryId" data-request-category-select required><option value="">카테고리를 불러오는 중...</option></select>`)}
        ${formField("최소 예산", `<input name="budgetMin" type="number" min="0" step="1000" placeholder="예: 300000" required />`)}
        ${formField("최대 예산", `<input name="budgetMax" type="number" min="0" step="1000" placeholder="예: 500000" required />`)}
        ${formField("희망 일정", `<input type="text" placeholder="예: 2026년 9월 첫째 주까지" />`)}
        <label class="file-drop-zone">
          <span>의뢰 자료 추가</span>
          <small data-request-file-name>선택사항 · 대표 이미지와 참고 자료를 함께 올릴 수 있습니다.</small>
          <input name="requestFiles" type="file" multiple />
        </label>
        <div class="selected-file-list" data-selected-request-files hidden></div>
        <p class="form-message" data-request-create-message aria-live="polite"></p>
        <div class="form-actions">
          ${button("Cancel", "#/home", "quiet")}
          <button class="button primary" type="submit">Post Request</button>
        </div>
      </form>
    </section>
  `;
}
