export function MyTradesPage() {
  return `
    <section class="section trade-page" data-my-trades-page>
      <div class="section-head">
        <div>
          <span class="kicker">Trades</span>
          <h1>거래 목록</h1>
        </div>
        <a class="button quiet" href="#/mypage">마이페이지</a>
      </div>

      <div class="trade-status-layout">
        <section class="trade-status-section">
          <div class="section-head compact">
            <h2>미완료된 거래</h2>
          </div>
          <div class="trade-list" data-trade-list="active">
            <article class="trade-list-card">
              <span>LOAD</span>
              <h3>거래 목록을 불러오는 중입니다.</h3>
              <p>잠시만 기다려 주세요.</p>
            </article>
          </div>
        </section>

        <section class="trade-status-section">
          <div class="section-head compact">
            <h2>완료된 거래</h2>
          </div>
          <div class="trade-list" data-trade-list="completed">
            <article class="trade-list-card">
              <span>LOAD</span>
              <h3>거래 목록을 불러오는 중입니다.</h3>
              <p>잠시만 기다려 주세요.</p>
            </article>
          </div>
        </section>
      </div>
    </section>
  `;
}
