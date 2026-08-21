import { categories } from "./data.js";

export function formatMoney(value) {
  return `${value.toLocaleString("ko-KR")}원`;
}

export function shell(content, route) {
  const links = [
    ["home", "Talent"],
    ["requests", "Requests"],
    ["ai-search", "AI Search"],
    ["chat", "Chat"],
    ["mypage", "My Page"],
  ];

  return `
    <header class="site-header">
      <a class="brand" href="#/home" aria-label="TalentPulse home">TalentPulse</a>
      <nav class="top-nav" aria-label="main navigation">
        ${links.map(([key, label]) => `<a class="${route === key ? "active" : ""}" href="#/${key}">${label}</a>`).join("")}
      </nav>
      <div class="header-tools">
        <input class="header-search" type="search" placeholder="Search..." aria-label="Search" />
        <a class="icon-link" href="#/mypage" aria-label="Notifications">⌁</a>
        <a class="icon-link" href="#/chat" aria-label="Messages">□</a>
        <a class="icon-link" href="#/login" aria-label="Account">○</a>
      </div>
    </header>
    <main class="page-shell">
      <section class="route-view" data-route="${route}">
        ${content}
      </section>
    </main>
    ${footer()}
  `;
}

export function footer() {
  return `
    <footer class="site-footer">
      <strong>TalentPulse</strong>
      <div>
        <a href="#/home">Terms</a>
        <a href="#/home">Privacy</a>
        <a href="#/home">Help</a>
        <a href="#/home">Contact</a>
      </div>
      <span>© 2026 TalentPulse. Premium Marketplace.</span>
    </footer>
  `;
}

export function button(label, href, variant = "primary") {
  return `<a class="button ${variant}" href="${href}">${label}</a>`;
}

export function categoryTabs(active = "Design") {
  return `
    <div class="tab-row" role="list">
      ${categories.map((category) => `<a class="tab ${category === active ? "active" : ""}" href="#/talents?category=${category}">${category}</a>`).join("")}
    </div>
  `;
}

export function talentCard(talent, featured = false) {
  return `
    <article class="talent-card ${featured ? "featured" : ""}">
      <a class="visual ${talent.category.toLowerCase()}" href="#/talent/${talent.id}" aria-label="${talent.title} detail">
        <span>${talent.category}</span>
      </a>
      <div class="card-body">
        <div class="meta-line">
          <span>${talent.expert}</span>
          <strong>★ ${talent.rating}</strong>
        </div>
        <h3><a href="#/talent/${talent.id}">${talent.title}</a></h3>
        <p>${talent.summary}</p>
        <div class="chip-row">
          ${talent.tags.map((tag) => `<span>${tag}</span>`).join("")}
        </div>
        <div class="card-action">
          <strong>${formatMoney(talent.price)}</strong>
          ${button("View Profile", `#/talent/${talent.id}`, "quiet")}
        </div>
      </div>
    </article>
  `;
}

export function requestCard(request) {
  return `
    <article class="request-card">
      <span class="kicker">${request.requester}</span>
      <h3><a href="#/request/${request.id}">${request.title}</a></h3>
      <p>${request.summary}</p>
      <dl>
        <div><dt>Budget</dt><dd>${request.budget}</dd></div>
        <div><dt>Due</dt><dd>${request.due}</dd></div>
      </dl>
      ${button("Apply", `#/request/${request.id}`, "quiet")}
    </article>
  `;
}

export function formField(label, control) {
  return `
    <label class="field">
      <span>${label}</span>
      ${control}
    </label>
  `;
}

export function pageTitle(kicker, title, copy = "") {
  return `
    <div class="page-title">
      <span class="kicker">${kicker}</span>
      <h1>${title}</h1>
      ${copy ? `<p>${copy}</p>` : ""}
    </div>
  `;
}
