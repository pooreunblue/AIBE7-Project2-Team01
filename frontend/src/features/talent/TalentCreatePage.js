export function TalentCreatePage() {
  return `
    <section class="portfolio-write-section talent-write-section">
      <form class="portfolio-write-form" data-talent-form>
        <div class="portfolio-editor-pane">
          <input class="portfolio-title-input" name="postTitle" type="text" maxlength="100" required placeholder="재능글 제목을 입력하세요" aria-label="재능글 제목" data-portfolio-title-input />
          <div class="portfolio-title-rule" aria-hidden="true"></div>
          <div class="portfolio-write-toolbar talent-write-toolbar">
            <button class="button quiet" type="button" data-talent-settings-open>상세정보 수정</button>
            <button class="button quiet" type="button" data-markdown-image-trigger>이미지 삽입</button>
            <input name="markdownImage" type="file" accept="image/png,image/jpeg,image/jpg,image/gif,image/webp" data-markdown-image-input />
            <button class="button quiet" type="button" data-talent-thumbnail-trigger>대표 이미지 추가</button>
            <input name="thumbnailImage" type="file" accept="image/png,image/jpeg,image/jpg,image/gif,image/webp" data-talent-thumbnail-input />
            <button class="button quiet" type="button" data-talent-ai-generate>AI로 글 다듬기</button>
            <button class="button quiet" type="button" data-talent-portfolio-open>포트폴리오 추가</button>
            <input name="portfolioId" type="hidden" data-talent-portfolio-value />
          </div>
          <div class="talent-thumbnail-preview" data-talent-thumbnail-preview hidden></div>
          <div class="talent-detail-summary" data-talent-detail-summary>
            <span>상세 설정을 입력해 주세요.</span>
          </div>
          <textarea class="portfolio-content-input" name="content" required placeholder="제공 범위, 진행 방식, 산출물, 수정 가능 범위를 마크다운으로 작성하세요." aria-label="재능글 설명" data-portfolio-markdown-input></textarea>
          <p class="form-message" data-talent-message aria-live="polite"></p>
          <div class="portfolio-write-actions">
            <a class="button quiet" href="#/talents">나가기</a>
            <button class="button primary" type="submit" data-talent-submit>출간하기</button>
          </div>
        </div>
        <div class="portfolio-preview-pane">
          <article class="markdown-preview" data-markdown-preview>
            <p>작성한 내용이 여기에 미리보기로 표시됩니다.</p>
          </article>
        </div>
        <div class="modal-backdrop" data-talent-settings-modal hidden>
          <div class="charge-modal talent-settings-modal" role="dialog" aria-modal="true" aria-label="재능글 상세 설정">
            <div class="modal-head">
              <div>
                <span class="kicker">Service Settings</span>
                <h2>상세정보</h2>
              </div>
              <button class="modal-close" type="button" data-talent-settings-close aria-label="상세정보 팝업 닫기">x</button>
            </div>
            <label class="field">
              <span>카테고리</span>
              <select name="categoryId" data-talent-category-select>
                <option value="">카테고리를 불러오는 중...</option>
              </select>
            </label>
            <label class="field">
              <span>가격</span>
              <input name="price" type="number" min="0" step="1000" placeholder="예: 800000" />
            </label>
            <label class="field">
              <span>예상 작업기간</span>
              <input name="estimatedDuration" type="number" min="1" step="1" placeholder="예: 7" />
            </label>
            <label class="field">
              <span>기간 단위</span>
              <select name="durationUnit">
                <option value="DAY">일</option>
                <option value="WEEK">주</option>
                <option value="MONTH">개월</option>
              </select>
            </label>
            <div class="form-actions">
              <button class="button quiet" type="button" data-talent-settings-close>취소</button>
              <button class="button primary" type="button" data-talent-settings-save>저장</button>
            </div>
          </div>
        </div>
        <div class="modal-backdrop" data-talent-portfolio-modal hidden>
          <div class="charge-modal talent-portfolio-modal" role="dialog" aria-modal="true" aria-label="연결 포트폴리오 선택">
            <div class="modal-head">
              <div>
                <span class="kicker">Portfolio</span>
                <h2>포트폴리오 선택</h2>
              </div>
              <button class="modal-close" type="button" data-talent-portfolio-close aria-label="포트폴리오 선택 팝업 닫기">x</button>
            </div>
            <div class="talent-portfolio-list" data-talent-portfolio-list>
              <p>포트폴리오를 불러오는 중입니다.</p>
            </div>
            <div class="form-actions">
              <button class="button quiet" type="button" data-talent-portfolio-clear>연결 해제</button>
              <button class="button primary" type="button" data-talent-portfolio-close>닫기</button>
            </div>
          </div>
        </div>
      </form> 
    </section>
  `;
}
