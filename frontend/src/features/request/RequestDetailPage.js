export function RequestDetailPage(id) {
  return `
    <section class="detail-layout request-detail" data-request-detail="${id}">
      <article class="detail-main padded">
        <a class="back-link" href="#/requests">‹ Back to requests</a>
        <span class="kicker" data-request-category>LOAD</span>
        <h1 data-request-title>의뢰글을 불러오는 중입니다.</h1>
        <p data-request-content>잠시만 기다려 주세요.</p>
        <div data-request-files></div>
      </article>
      <aside class="checkout-card">
        <span>Estimated Budget</span>
        <strong data-request-budget>-</strong>
        <p data-request-meta>의뢰 정보를 확인 중입니다.</p>
        <button class="button primary" type="button" data-request-chat disabled>Message Requester</button>
      </aside>
    </section>
  `;
}
