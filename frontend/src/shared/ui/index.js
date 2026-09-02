import { escapeHtml, safeUrl } from "../security/xss.js";

export function formatMoney(value) {
  return `${value.toLocaleString("ko-KR")}원`;
}

export function shell(content, route, { isLoggedIn = false, canGoBack = false } = {}) {
  const links = [
    ["talents", "재능글", "재능글 목록"],
    ["requests", "요청글", "요청글 목록"],
    ["ai-search", "AI 매칭", "AI 매칭"],
    ["chat", "채팅", "채팅"],
  ];
  const safeRoute = escapeHtml(route);

  return `
    <header class="site-header">
      ${backControl(canGoBack)}
      <a class="header-logo" href="#/home" aria-label="메인 화면으로 이동">
        <img src="assets/knotty-logo.png" alt="Knotty" />
      </a>
      <nav class="icon-nav" aria-label="main navigation">
        ${links.map(([key, icon, label]) => headerLink(key, icon, label, route)).join("")}
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

function backControl(canGoBack) {
  if (!canGoBack) {
    return "";
  }

  return `
    <button class="header-back-button" type="button" aria-label="뒤로가기" data-header-back>
      <span aria-hidden="true">‹</span>
    </button>
  `;
}

function headerLink(key, icon, label, route) {
  const activeClass = route === key ? "active" : "";
  const textKeys = new Set(["talents", "requests", "ai-search", "chat"]);
  const linkClass = textKeys.has(key) ? "header-text-link" : "icon-link";

  return `
    <a class="${linkClass} ${activeClass}" href="#/${key}" aria-label="${escapeHtml(label)}">
      <span aria-hidden="true">${escapeHtml(icon)}</span>
    </a>
  `;
}

function accountControl(isLoggedIn, route) {
  if (!isLoggedIn) {
    return `<a class="icon-link ${route === "login" ? "active" : ""}" href="#/login" aria-label="Login"><span aria-hidden="true">○</span></a>`;
  }

  const isActive = route === "mypage" || route === "my-trades" || route === "portfolios";

  return `
    <a class="icon-link ${isActive ? "active" : ""}" href="#/mypage" aria-label="마이페이지">
      <span aria-hidden="true" data-header-avatar>○</span>
    </a>
  `;
}

export function footer() {
  return `
    <footer class="site-footer">
      <strong>Knotty</strong>
      <span>© 2026 Knotty. Premium Marketplace.</span>
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

export function listToolbar(label, activeCategory = "All", href = "#/talents", { withFilters = false } = {}) {
  const safeLabel = escapeHtml(label);
  return `
    <div class="list-toolbar">
      <form class="list-search" data-list-search>
        <input name="keyword" type="search" placeholder="${safeLabel} 검색" aria-label="${safeLabel} 검색" />
        <button type="submit">Search</button>
        ${withFilters ? `<button class="list-filters-button" type="button" data-list-filters>Filters</button>` : ""}
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
