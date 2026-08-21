import { messages, requests, talents, user } from "./data.js";
import { button, categoryTabs, formField, formatMoney, pageTitle, requestCard, talentCard } from "./components.js";

export function homePage() {
  return `
    <section class="hero">
      ${pageTitle("AI Talent Marketplace", "Find the perfect talent for your next project.", "Describe exactly what you need and move from search to transaction without losing context.")}
      <form class="search-box" data-search-form>
        <span class="spark">✦</span>
        <input name="query" type="search" value="I need a freelance UX designer for a fintech app..." aria-label="AI search" />
        <button type="submit">Search</button>
      </form>
      <div class="suggestions">
        <span>UX/UI Design</span>
        <span>Copywriting</span>
        <span>Video Editing</span>
        <span>SEO</span>
      </div>
    </section>

    <section class="section">
      <div class="section-head">
        <h2>Explore Categories</h2>
        <a href="#/talents">View all</a>
      </div>
      <div class="category-grid">
        <a href="#/talents?category=Design"><span>⌘</span>Design & Creative</a>
        <a href="#/talents?category=Development"><span>{ }</span>Development</a>
        <a href="#/talents?category=Marketing"><span>↗</span>Marketing</a>
        <a href="#/talents?category=Writing"><span>T</span>Writing & Translation</a>
      </div>
    </section>
  `;
}

export function loginPage() {
  return `
    <section class="auth-page">
      <form class="auth-card">
        <h1>TalentPulse</h1>
        <p>Welcome back. Please sign in to continue.</p>
        ${formField("Email Address", `<input type="email" value="alex@example.com" />`)}
        ${formField("Password", `<input type="password" value="password" />`)}
        <button class="button primary" type="submit">Sign In</button>
        <div class="auth-options">
          <button type="button">Google</button>
          <button type="button">Apple</button>
        </div>
        <span class="inline-note">Do not have an account? <a href="#/login">Sign up</a></span>
      </form>
    </section>
  `;
}

export function talentsPage() {
  return `
    <section class="section">
      <div class="list-heading">
        <div>
          <span class="kicker">Discover Top Talent</span>
          <h1>Specialized professionals ready to elevate your project.</h1>
        </div>
        <div class="filter-actions">
          <select aria-label="Sort">
            <option>Popular first</option>
            <option>Newest first</option>
          </select>
          <button type="button">Filters</button>
        </div>
      </div>
      ${categoryTabs()}
      <div class="talent-grid">
        ${talents.map((talent) => talentCard(talent)).join("")}
      </div>
      <div class="center-action">${button("Post a Talent", "#/talent-new", "secondary")}</div>
    </section>
  `;
}

export function talentDetailPage(id) {
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

export function talentNewPage() {
  return formPage("Post a Talent", "Share your skill with the marketplace.", "Talent Title", "Premium UX/UI Design for Web Apps", "Post Talent");
}

export function requestsPage() {
  return `
    <section class="section">
      <div class="list-heading">
        <div>
          <span class="kicker">Project Requests</span>
          <h1>Requests from buyers looking for the right expert.</h1>
        </div>
        ${button("Post Request", "#/request-new", "secondary")}
      </div>
      <div class="request-grid">
        ${requests.map(requestCard).join("")}
      </div>
    </section>
  `;
}

export function requestNewPage() {
  return formPage("Post a Request", "Describe what you need, set your budget, and connect with the right talent.", "Request Title", "Need a logo design for my new coffee shop", "Post Request");
}

export function requestDetailPage(id) {
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

export function aiSearchPage() {
  return `
    <section class="section">
      ${pageTitle("AI Search", "What are you looking for?", "Natural language search with keyword fallback when AI is unavailable.")}
      <form class="search-box wide" data-search-form>
        <span class="spark">✦</span>
        <input name="query" type="search" value="I need a freelance UX designer for a fintech app..." aria-label="AI search" />
        <button type="submit">Search</button>
      </form>
      <div class="ai-insight">
        <strong>AI Insight</strong>
        <span>Looking for UX Design expertise specifically tailored for Financial Technology.</span>
      </div>
      <div class="search-results-layout">
        <div>
          <h2>Top Talent Matches</h2>
          <div class="talent-grid two-col">
            ${talents.map((talent) => talentCard(talent, true)).join("")}
          </div>
        </div>
        <aside>
          <h2>Similar Requests</h2>
          ${requests.map(requestCard).join("")}
        </aside>
      </div>
    </section>
  `;
}

export function chatPage() {
  return `
    <section class="chat-layout">
      <aside class="conversation-list">
        <h1>Messages</h1>
        <input type="search" placeholder="Search conversations..." aria-label="Search conversations" />
        <a class="conversation active" href="#/chat"><strong>Elena Rodriguez</strong><span>That sounds great.</span></a>
        <a class="conversation" href="#/chat"><strong>Marcus Chen</strong><span>Here is the link.</span></a>
      </aside>
      <article class="chat-panel">
        <header>
          <div class="seller-box">
            <div class="avatar">E</div>
            <div><strong>Elena Rodriguez</strong><span>UX/UI Designer · Response time 1 hr</span></div>
          </div>
          ${button("Request Transaction", "#/checkout", "primary")}
        </header>
        <div class="message-stream">
          ${messages.map(([type, text]) => `<p class="bubble ${type}">${text}</p>`).join("")}
          <div class="shared-card">
            <div class="visual design small"></div>
            <strong>Fintech App Redesign</strong>
            <span>Portfolio preview</span>
          </div>
        </div>
        <form class="message-compose">
          <input type="text" placeholder="Type a message..." aria-label="Message" />
          <button type="submit">Send</button>
        </form>
      </article>
    </section>
  `;
}

export function myPage() {
  return `
    <section class="dashboard-layout">
      <aside class="dashboard-menu">
        <a class="active" href="#/mypage">대시보드</a>
        <a href="#/talent-new">프로필 수정</a>
        <a href="#/requests">판매/구매 관리</a>
        <a href="#/chat">메시지 내역</a>
        <a href="#/talents">포트폴리오 관리</a>
        <a href="#/mypage">리뷰 목록</a>
      </aside>
      <div class="dashboard-main">
        <div class="profile-summary">
          <div class="avatar large">A</div>
          <div>
            <h1>${user.name}</h1>
            <p>${user.role}</p>
            <span>${user.location} · ★ 4.9 · 124 reviews</span>
          </div>
          <strong>${formatMoney(user.balance)}</strong>
        </div>
        <div class="stats-grid">
          ${user.stats.map(([label, value]) => `<article><span>${label}</span><strong>${value}</strong></article>`).join("")}
        </div>
        <div class="dashboard-panels">
          <article>
            <h2>최근 거래 내역</h2>
            <p>로고 디자인 제작 <strong>+150,000원</strong></p>
            <p>랜딩페이지 퍼블리싱 <strong>+300,000원</strong></p>
          </article>
          <article>
            <h2>판매/구매 글 관리</h2>
            <p>${talents[0].title}</p>
            <p>${requests[0].title}</p>
          </article>
        </div>
      </div>
    </section>
  `;
}

export function checkoutPage() {
  return `
    <section class="checkout-layout">
      ${pageTitle("Checkout", "Review your request and securely complete your payment.")}
      <div class="checkout-grid">
        <div>
          <article class="summary-card">
            <h2>Request Summary</h2>
            <div class="seller-box">
              <div class="visual design thumb"></div>
              <div><strong>${talents[0].title}</strong><span>by ${talents[0].expert}</span></div>
            </div>
            <dl>
              <div><dt>Delivery</dt><dd>Oct 25, 2026</dd></div>
              <div><dt>Revisions</dt><dd>Up to 3</dd></div>
            </dl>
          </article>
          <article class="summary-card">
            <h2>Payment Method</h2>
            <label class="payment-option active"><input type="radio" checked /> Credit Card <span>Visa ending 4242</span></label>
            <label class="payment-option"><input type="radio" /> Apple Pay</label>
          </article>
        </div>
        <aside class="checkout-card">
          <span>Payment Details</span>
          <dl>
            <div><dt>Service Fee</dt><dd>$850.00</dd></div>
            <div><dt>Platform Fee</dt><dd>$42.50</dd></div>
          </dl>
          <strong>$892.50</strong>
          <p class="secure-note">Secure escrow payment. Funds are released after delivery approval.</p>
          ${button("Pay $892.50 Now", "#/mypage", "primary")}
        </aside>
      </div>
    </section>
  `;
}

function formPage(title, copy, titleLabel, titleValue, submitLabel) {
  return `
    <section class="form-page">
      ${pageTitle("Create", title, copy)}
      <form class="editor-card">
        ${formField(titleLabel, `<input type="text" value="${titleValue}" />`)}
        ${formField("Category", `<select>${["Design", "Development", "Writing", "Consulting"].map((item) => `<option>${item}</option>`).join("")}</select>`)}
        ${formField("Detailed Description", `<textarea rows="6">Describe scope, requirements, timeline, and specific skills needed.</textarea>`)}
        ${formField("Budget / Price", `<input type="text" value="$ 0.00" />`)}
        <label class="upload-box">
          <span>Upload a file or drag and drop</span>
          <small>PNG, JPG, PDF up to 10MB</small>
          <input type="file" />
        </label>
        <div class="form-actions">
          ${button("Cancel", "#/home", "quiet")}
          <button class="button primary" type="submit">${submitLabel}</button>
        </div>
      </form>
    </section>
  `;
}
