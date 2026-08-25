import { button, formatMoney } from "../../shared/ui/index.js";
import { talents } from "../../shared/data/mock.js";

export function TalentDetailPage(id) {
  const talent = talents.find((item) => item.id === id) || talents[0];

  return `
    <section class="detail-layout">
      <article class="detail-main">
        <div class="detail-hero visual ${talent.category.toLowerCase()}">
          <span>${talent.category}</span>
        </div>
        <div class="detail-copy">
          <span class="kicker">${talent.category} · ${talent.location}</span>
          <h1>${talent.title}</h1>
          <p>${talent.summary}</p>
          <div class="seller-box">
            <div class="avatar">A</div>
            <div>
              <strong>${talent.expert}</strong>
              <span>Top Rated Seller · ★ ${talent.rating}</span>
            </div>
          </div>
          <h2>About This Service</h2>
          <p>I specialize in simple, usable interfaces and practical delivery documents that help teams move from idea to launch.</p>
          <ul class="check-list">
            <li>Responsive screen design and interaction states</li>
            <li>Frontend handoff with clear component structure</li>
            <li>Portfolio materials available during chat</li>
          </ul>
        </div>
      </article>
      <aside class="checkout-card">
        <span>Standard Package</span>
        <strong>${formatMoney(talent.price)}</strong>
        <p>Includes core screens, revisions, and delivery notes.</p>
        ${button("Request Transaction", "#/checkout", "primary")}
        ${button("Chat with Seller", "#/chat", "quiet")}
      </aside>
    </section>
  `;
}
