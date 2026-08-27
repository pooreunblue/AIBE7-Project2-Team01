import { pageTitle } from "../../shared/ui/index.js";

export function PortfolioCreatePage() {
  return `
    <section class="section portfolio-write-section">
      ${pageTitle("Portfolio", "포트폴리오 글 작성", "작업 과정과 결과물을 읽기 좋은 글 형태로 정리합니다.")}
      <form class="portfolio-write-form" data-portfolio-form>
        <input class="portfolio-title-input" name="title" type="text" maxlength="100" required placeholder="제목을 입력하세요" aria-label="포트폴리오 제목" />
        <div class="portfolio-title-rule" aria-hidden="true"></div>
        <textarea class="portfolio-content-input" name="description" required placeholder="작업 배경, 맡은 역할, 해결한 문제, 결과물을 자유롭게 작성하세요." aria-label="포트폴리오 설명"></textarea>
        <label class="file-drop-zone portfolio-write-upload">
          <span>첨부파일 추가</span>
          <small data-portfolio-file-name>선택사항 · 여러 이미지와 작업 자료를 함께 올릴 수 있습니다.</small>
          <input name="portfolioFiles" type="file" multiple />
        </label>
        <p class="form-message" data-portfolio-message aria-live="polite"></p>
        <div class="portfolio-write-actions">
          <a class="button quiet" href="#/portfolios">나가기</a>
          <button class="button primary" type="submit">출간하기</button>
        </div>
      </form>
    </section>
  `;
}
