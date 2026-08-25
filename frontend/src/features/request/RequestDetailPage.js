import { button } from "../../shared/ui/index.js";
import { requests } from "../../shared/data/mock.js";

export function RequestDetailPage(id) {
  const request = requests.find((item) => item.id === id) || requests[0];

  return `
    <section class="detail-layout request-detail">
      <article class="detail-main padded">
        <a class="back-link" href="#/requests">‹ Back to requests</a>
        <span class="kicker">${request.category} · Featured Request</span>
        <h1>${request.title}</h1>
        <p>${request.summary}</p>
        <h2>Description</h2>
        <p>We need a refined interface that improves clarity, reduces decision fatigue, and keeps the transaction flow easy to follow.</p>
        <h2>Key Requirements</h2>
        <ul class="check-list">
          <li>Experience with responsive product pages</li>
          <li>Clean component naming and reusable screens</li>
          <li>Portfolio examples attached before transaction</li>
        </ul>
      </article>
      <aside class="checkout-card">
        <span>Estimated Budget</span>
        <strong>${request.budget}</strong>
        <p>Fixed price project · Delivery ${request.due}</p>
        ${button("Send Proposal", "#/chat", "primary")}
        ${button("Message Requester", "#/chat", "quiet")}
      </aside>
    </section>
  `;
}
