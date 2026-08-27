export function TalentDetailPage(id) {
  return `
    <section class="detail-layout" data-talent-detail="${id}">
      <article class="detail-main">
        <div class="detail-hero visual">
          <span data-talent-category>LOAD</span>
        </div>
        <div class="detail-copy">
          <span class="kicker" data-talent-meta>재능글을 불러오는 중입니다.</span>
          <h1 data-talent-title>잠시만 기다려 주세요.</h1>
          <div class="markdown-preview portfolio-detail-markdown" data-talent-content>
            <p>내용을 확인하고 있습니다.</p>
          </div>
          <div data-talent-files></div>
          <div data-talent-linked-portfolio></div>
          <div class="seller-box">
            <div class="avatar" data-talent-avatar>?</div>
            <div>
              <strong data-talent-author>작성자 확인 중</strong>
              <span data-talent-status>-</span>
            </div>
          </div>
        </div>
      </article>
      <aside class="checkout-card" data-talent-actions>
        <span>Service Details</span>
        <strong data-talent-price>-</strong>
        <p data-talent-duration>예상 작업기간을 확인 중입니다.</p>
        <button class="button primary" type="button" data-talent-chat disabled>Chat with Seller</button>
      </aside>
    </section>
  `;
}
