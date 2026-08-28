export function RequestCreatePage() {
  return `
    <section class="portfolio-write-section request-write-section">
      <form class="portfolio-write-form" data-request-create-form>
        <div class="portfolio-editor-pane">
          <input class="portfolio-title-input" name="title" type="text" maxlength="100" required placeholder="요청글 제목을 입력하세요" aria-label="요청글 제목" data-portfolio-title-input />
          <div class="portfolio-title-rule" aria-hidden="true"></div>
          <div class="portfolio-write-toolbar request-write-toolbar">
            <button class="button quiet" type="button" data-request-settings-open>상세정보 수정</button>
            <button class="button quiet" type="button" data-markdown-image-trigger>이미지 삽입</button>
            <input name="markdownImage" type="file" accept="image/png,image/jpeg,image/jpg,image/gif,image/webp" data-markdown-image-input />
            <button class="button quiet" type="button" data-request-thumbnail-trigger>대표 이미지 추가</button>
            <input name="thumbnailImage" type="file" accept="image/png,image/jpeg,image/jpg,image/gif,image/webp" data-request-thumbnail-input />
          </div>
          <div class="talent-thumbnail-preview" data-request-thumbnail-preview hidden></div>
          <div class="talent-detail-summary" data-request-detail-summary>
            <span>상세 설정을 입력해 주세요.</span>
          </div>
          <textarea class="portfolio-content-input" name="content" required placeholder="필요한 작업, 참고 자료, 원하는 결과물을 마크다운으로 작성하세요." aria-label="요청글 설명" data-portfolio-markdown-input></textarea>
          <p class="form-message" data-request-create-message aria-live="polite"></p>
          <div class="portfolio-write-actions">
            <a class="button quiet" href="#/requests">나가기</a>
            <button class="button primary" type="submit">요청글 등록</button>
          </div>
        </div>
        <div class="portfolio-preview-pane">
          <article class="markdown-preview" data-markdown-preview>
            <p>작성한 내용이 여기에 미리보기로 표시됩니다.</p>
          </article>
        </div>
        <div class="modal-backdrop" data-request-settings-modal hidden>
          <div class="charge-modal talent-settings-modal" role="dialog" aria-modal="true" aria-label="요청글 상세 설정">
            <div class="modal-head">
              <div>
                <span class="kicker">Request Settings</span>
                <h2>상세정보</h2>
              </div>
              <button class="modal-close" type="button" data-request-settings-close aria-label="상세정보 팝업 닫기">x</button>
            </div>
            <label class="field">
              <span>카테고리</span>
              <select name="categoryId" data-request-category-select>
                <option value="">카테고리를 불러오는 중...</option>
              </select>
            </label>
            <label class="field">
              <span>최소 예산</span>
              <input name="budgetMin" type="number" min="0" step="1000" placeholder="예: 300000" />
            </label>
            <label class="field">
              <span>최대 예산</span>
              <input name="budgetMax" type="number" min="0" step="1000" placeholder="예: 500000" />
            </label>
            <label class="field">
              <span>희망 마감일</span>
              <input name="dueDate" type="date" />
            </label>
            <div class="form-actions">
              <button class="button quiet" type="button" data-request-settings-close>취소</button>
              <button class="button primary" type="button" data-request-settings-save>저장</button>
            </div>
          </div>
        </div>
      </form>
    </section>
  `;
}
