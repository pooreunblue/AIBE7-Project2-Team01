import { formField, pageTitle } from "../../shared/ui/index.js";

export function PortfolioPage() {
  return `
    <section class="section">
      ${pageTitle("Portfolio", "포트폴리오 관리", "등록한 작업 이력과 소개 자료를 관리합니다.")}
      <div class="portfolio-manage-layout">
        <form class="editor-card portfolio-form" data-portfolio-form>
          <h2>새 포트폴리오</h2>
          ${formField("제목", `<input name="title" type="text" maxlength="100" required placeholder="예: 브랜드 랜딩페이지" />`)}
          ${formField("설명", `<textarea name="description" rows="5" required placeholder="작업 내용과 역할을 입력하세요."></textarea>`)}
          <label class="file-drop-zone">
            <span>포트폴리오 파일</span>
            <small data-portfolio-file-name>선택사항 · 이미지, PDF 등 작업 자료</small>
            <input name="portfolioFile" type="file" />
          </label>
          <button class="button primary" type="submit">등록</button>
          <p class="form-message" data-portfolio-message aria-live="polite"></p>
        </form>
        <div class="portfolio-grid" data-portfolio-list>
          <article class="summary-card empty-state">
            <h2>포트폴리오를 불러오는 중입니다.</h2>
            <p>잠시만 기다려 주세요.</p>
          </article>
        </div>
      </div>
    </section>
  `;
}
