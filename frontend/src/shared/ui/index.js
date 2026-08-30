import { escapeHtml, safeUrl } from "../security/xss.js";

export function formatMoney(value) {
  return `${value.toLocaleString("ko-KR")}원`;
}

export function shell(content, route, { isLoggedIn = false } = {}) {
  const links = [
    ["home", "⌂", "Home"],
    ["chat", "□", "Chat"],
  ];
  const headerTitle = escapeHtml(getHeaderTitle(route));
  const safeRoute = escapeHtml(route);

  return `
    <header class="site-header">
      <strong class="header-title">${headerTitle}</strong>
      <nav class="icon-nav" aria-label="main navigation">
        ${links.map(([key, icon, label]) => `<a class="icon-link ${route === key ? "active" : ""}" href="#/${key}" aria-label="${label}"><span aria-hidden="true">${icon}</span></a>`).join("")}
        ${accountControl(isLoggedIn, route)}
      </nav>
    </header>
    <main class="page-shell">
      <section class="route-view" data-route="${safeRoute}">
        ${content}
      </section>
    </main>
    ${footer()}
  `;
}

function getHeaderTitle(route) {
  const titles = {
    home: "Home",
    login: "Login",
    signup: "Sign Up",
    talents: "Talent",
    "talent-new": "Talent Create",
    requests: "Requests",
    "request-new": "Request Create",
    "ai-search": "AI Search",
    chat: "Chat",
    mypage: "My Page",
    portfolios: "Portfolio",
    "portfolio-new": getPortfolioWriteTitle(),
    checkout: "Payment",
  };

  return titles[route] || "TalentPulse";
}

function getPortfolioWriteTitle() {
  return window.location.hash.includes("?id=")
    ? "Portfolio Edit"
    : "Portfolio Create";
}

function accountControl(isLoggedIn, route) {
  if (!isLoggedIn) {
    return `<a class="icon-link ${route === "login" ? "active" : ""}" href="#/login" aria-label="Login"><span aria-hidden="true">○</span></a>`;
  }

  const isActive = route === "mypage" || route === "portfolios";

  return `
    <div class="account-nav">
      <button class="icon-link account-trigger ${isActive ? "active" : ""}" type="button" aria-label="Account menu" aria-expanded="false" aria-controls="account-menu" data-account-trigger>
        <span aria-hidden="true" data-header-avatar>○</span>
      </button>
      <div class="account-menu" id="account-menu" data-account-menu hidden>
        <a href="#/portfolios">포트폴리오</a>
        <a href="#/mypage">마이페이지</a>
        <button type="button" data-logout-button>로그아웃</button>
      </div>
    </div>
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
  const allowedVariant = ["primary", "quiet"].includes(variant) ? variant : "primary";
  return `<a class="button ${allowedVariant}" href="${escapeHtml(safeUrl(href))}">${escapeHtml(label)}</a>`;
}

export function categoryTabs(active = "All", href = "#/talents") {
  const safeHref = escapeHtml(safeUrl(href));
  return `
    <div class="tab-row" role="list" data-category-tabs data-category-active="${escapeHtml(active)}" data-category-href="${safeHref}">
      <a class="tab active" href="${safeHref}">All</a>
    </div>
  `;
}

export function listToolbar(label, activeCategory = "All", href = "#/talents") {
  const safeLabel = escapeHtml(label);
  return `
    <div class="list-toolbar">
      <form class="list-search" data-list-search>
        <input name="keyword" type="search" placeholder="${safeLabel} 검색" aria-label="${safeLabel} 검색" />
        <button type="submit">Search</button>
      </form>
      ${categoryTabs(activeCategory, href)}
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
      <span>${escapeHtml(label)}</span>
      ${control}
    </label>
  `;
}

export function pageTitle(kicker, title, copy = "") {
  return `
    <div class="page-title">
      <span class="kicker">${escapeHtml(kicker)}</span>
      <h1>${escapeHtml(title)}</h1>
      ${copy ? `<p>${escapeHtml(copy)}</p>` : ""}
    </div>
  `;
}
