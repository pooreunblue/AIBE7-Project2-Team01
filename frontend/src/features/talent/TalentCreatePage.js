import { button, formField, pageTitle } from "../../shared/ui/index.js";

const categoryOptions = ["Design", "Development", "Writing", "Consulting"]
  .map((item) => `<option>${item}</option>`)
  .join("");

export function TalentCreatePage() {
  return `
    <section class="form-page">
      ${pageTitle("Create Service", "내가 제공할 수 있는 재능/서비스 등록", "가격과 예상 작업기간, 포트폴리오를 함께 연결해 주세요.")}
      <form class="editor-card">
        ${formField("제목", `<input type="text" placeholder="예: 반응형 웹 프론트엔드 개발" />`)}
        ${formField("설명", `<textarea rows="6" placeholder="제공 가능한 서비스 범위와 진행 방식을 입력하세요."></textarea>`)}
        ${formField("카테고리", `<select>${categoryOptions}</select>`)}
        ${formField("가격", `<input type="text" placeholder="예: 800,000원" />`)}
        ${formField("예상 작업기간", `<input type="text" placeholder="예: 7일" />`)}
        ${formField("포트폴리오 연결", `<input type="url" placeholder="https://portfolio.example.com" />`)}
        <div class="form-actions">
          ${button("Cancel", "#/home", "quiet")}
          <button class="button primary" type="submit">Post Talent</button>
        </div>
      </form>
    </section>
  `;
}
