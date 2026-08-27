export function PortfolioCreatePage() {
  return `
    <section class="portfolio-write-section">
      <form class="portfolio-write-form" data-portfolio-form>
        <div class="portfolio-editor-pane">
          <input class="portfolio-title-input" name="title" type="text" maxlength="100" required placeholder="제목을 입력하세요" aria-label="포트폴리오 제목" data-portfolio-title-input />
          <div class="portfolio-title-rule" aria-hidden="true"></div>
          <div class="portfolio-write-toolbar">
            <button class="button quiet" type="button" data-markdown-image-trigger>이미지 삽입</button>
            <input name="markdownImage" type="file" accept="image/png,image/jpeg,image/jpg,image/gif,image/webp" data-markdown-image-input />
          </div>
          <textarea class="portfolio-content-input" name="description" required placeholder="작업 배경, 맡은 역할, 해결한 문제, 결과물을 자유롭게 작성하세요." aria-label="포트폴리오 설명" data-portfolio-markdown-input></textarea>
          <label class="file-drop-zone portfolio-write-upload">
            <span>첨부파일 추가</span>
            <small data-portfolio-file-name>선택사항 · 여러 이미지와 작업 자료를 함께 올릴 수 있습니다.</small>
            <input name="portfolioFiles" type="file" multiple />
          </label>
          <div class="selected-file-list" data-selected-portfolio-files hidden></div>
          <p class="form-message" data-portfolio-message aria-live="polite"></p>
          <div class="portfolio-write-actions">
            <a class="button quiet" href="#/portfolios">나가기</a>
            <button class="button primary" type="submit" data-portfolio-submit>출간하기</button>
          </div>
        </div>
        <div class="portfolio-preview-pane">
          <article class="markdown-preview" data-markdown-preview>
            <p>작성한 내용이 여기에 미리보기로 표시됩니다.</p>
          </article>
        </div>
      </form>
    </section>
  `;
}
