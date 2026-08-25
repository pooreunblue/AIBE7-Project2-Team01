import { button, formField, pageTitle } from "../../shared/ui/index.js";

const categoryOptions = ["Design", "Development", "Writing", "Consulting"]
  .map((item) => `<option>${item}</option>`)
  .join("");

export function RequestCreatePage() {
  return `
    <section class="form-page">
      ${pageTitle("Create Request", "전문가/프리랜서를 구하는 요청글 작성", "필요한 작업 범위와 예산, 희망 일정을 정리해 주세요.")}
      <form class="editor-card">
        ${formField("제목", `<input type="text" placeholder="예: 쇼핑몰 상세페이지 디자인이 필요해요" />`)}
        ${formField("설명", `<textarea rows="6" placeholder="필요한 작업, 참고 자료, 원하는 결과물을 입력하세요."></textarea>`)}
        ${formField("카테고리", `<select>${categoryOptions}</select>`)}
        ${formField("예산", `<input type="text" placeholder="예: 500,000원" />`)}
        ${formField("희망 일정", `<input type="text" placeholder="예: 2026년 9월 첫째 주까지" />`)}
        <div class="form-actions">
          ${button("Cancel", "#/home", "quiet")}
          <button class="button primary" type="submit">Post Request</button>
        </div>
      </form>
    </section>
  `;
}
