export function RequestDetailPage(id) {
  return `
    <section class="detail-layout request-detail" data-request-detail="${id}">
      <article class="detail-main">
        <div class="detail-hero visual">
          <span data-request-category>LOAD</span>
        </div>
        <div class="detail-copy">
          <a class="back-link" href="#/requests">‹ Back to requests</a>
          <span class="kicker" data-request-meta-line>의뢰글을 불러오는 중입니다.</span>
          <h1 data-request-title>잠시만 기다려 주세요.</h1>
          <div class="markdown-preview portfolio-detail-markdown" data-request-content>
            <p>내용을 확인하고 있습니다.</p>
          </div>
          <div data-request-files></div>
          <div class="seller-box" data-request-author-box>
            <div class="avatar" data-request-avatar>?</div>
            <div>
              <strong data-request-author>작성자 확인 중</strong>
              <span data-request-author-meta>-</span>
            </div>
          </div>
        </div>
      </article>
      <aside class="checkout-card" data-request-actions>
        <span>Estimated Budget</span>
        <strong data-request-budget>-</strong>
        <p data-request-meta>의뢰 정보를 확인 중입니다.</p>
        <button class="button primary" type="button" data-request-chat disabled>요청자와 채팅</button>
      </aside>
    </section>
  `;
}
