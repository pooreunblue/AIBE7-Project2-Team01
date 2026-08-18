const apiRoutes = {
  signup: { method: "POST", url: "/signup" },
  login: { method: "POST", url: "/login" },
  member: { method: "GET", url: "/member" },
  talents: { method: "GET", url: "/talents" },
  talentDetail: { method: "GET", url: "/talents/{id}" },
  requests: { method: "GET", url: "/requests" },
  requestDetail: { method: "GET", url: "/requests/{id}" },
  keywordSearch: { method: "GET", url: "/search" },
  aiSearch: { method: "POST", url: "/search/ai" },
  chatrooms: { method: "GET", url: "/chatrooms" },
  trades: { method: "POST", url: "/trades" },
  payments: { method: "POST", url: "/payments" },
  reviews: { method: "POST", url: "/reviews" },
};

const talents = [
  {
    id: 101,
    title: "Spring Boot 쇼핑몰 구축",
    category: "개발",
    owner: "Backend Pro",
    price: 800000,
    priority: "상",
    summary: "JWT 인증, 결제 승인, 관리자 상품 관리까지 포함한 MVP 개발",
  },
  {
    id: 102,
    title: "브랜드 로고 디자인",
    category: "디자인",
    owner: "Visual Lab",
    price: 180000,
    priority: "상",
    summary: "시안 3종과 포트폴리오 기반 레퍼런스 제안",
  },
  {
    id: 103,
    title: "랜딩 페이지 카피라이팅",
    category: "마케팅",
    owner: "Growth Writer",
    price: 220000,
    priority: "중",
    summary: "서비스 목적에 맞춘 헤드라인, CTA, FAQ 문구 작성",
  },
];

const requests = [
  {
    title: "포트폴리오 웹사이트 제작",
    category: "개발",
    budget: 500000,
    status: "제안 받는 중",
  },
  {
    title: "앱 출시 소개 이미지",
    category: "디자인",
    budget: 150000,
    status: "검토 중",
  },
  {
    title: "영문 자기소개서 번역",
    category: "번역",
    budget: 80000,
    status: "모집 중",
  },
];

const portfolios = [
  {
    title: "커머스 API 서버",
    owner: "Backend Pro",
    summary: "회원, 상품, 결제 승인 플로우 구현 사례",
  },
  {
    title: "로컬 브랜드 아이덴티티",
    owner: "Visual Lab",
    summary: "카페, 스튜디오, 행사 로고 제작 포트폴리오",
  },
  {
    title: "SaaS 온보딩 카피",
    owner: "Growth Writer",
    summary: "가입 전환율 개선 목적의 화면 문구 개선",
  },
];

const chatMessages = [
  { from: "seller", body: "요청하신 쇼핑몰 기능 범위를 확인했습니다." },
  { from: "me", body: "JWT 로그인과 결제 승인까지 우선 포함하고 싶어요." },
  { from: "seller", body: "가능합니다. 포트폴리오 링크도 함께 보내드릴게요." },
];

const formatPrice = (value) => `${value.toLocaleString("ko-KR")}원`;

function renderTalents(list = talents) {
  const talentList = document.querySelector("#talentList");
  talentList.innerHTML = list
    .map(
      (talent) => `
        <article class="talent-card">
          <header>
            <h3>${talent.title}</h3>
            <span class="price">${formatPrice(talent.price)}</span>
          </header>
          <p>${talent.summary}</p>
          <div class="badge-row">
            <span class="badge">${talent.category}</span>
            <span class="badge ${talent.priority === "상" ? "high" : "medium"}">우선순위 ${talent.priority}</span>
            <span class="badge">${talent.owner}</span>
          </div>
          <button class="secondary-button" type="button">거래 요청</button>
        </article>
      `,
    )
    .join("");
}

function renderRequests() {
  const requestRows = document.querySelector("#requestRows");
  requestRows.innerHTML = requests
    .map(
      (request) => `
        <tr>
          <td><strong>${request.title}</strong></td>
          <td>${request.category}</td>
          <td>${formatPrice(request.budget)}</td>
          <td><span class="badge">${request.status}</span></td>
          <td><button class="ghost-button" type="button">상세</button></td>
        </tr>
      `,
    )
    .join("");
}

function renderSearchResults(keyword = "Spring Boot로 쇼핑몰 만들어줄 사람") {
  const normalizedKeyword = keyword.trim() || "재능";
  const searchResults = document.querySelector("#searchResults");
  searchResults.innerHTML = talents
    .map(
      (talent, index) => `
        <article class="result-item">
          <h3>${index + 1}. ${talent.title}</h3>
          <p>${normalizedKeyword} 검색어와 ${talent.category} 카테고리, 포트폴리오 적합도를 기준으로 추천되었습니다.</p>
          <div class="badge-row">
            <span class="badge">POST ${apiRoutes.aiSearch.url}</span>
            <span class="badge">Fallback ${apiRoutes.keywordSearch.url}</span>
          </div>
        </article>
      `,
    )
    .join("");
}

function renderPortfolios() {
  const portfolioList = document.querySelector("#portfolioList");
  portfolioList.innerHTML = portfolios
    .map(
      (portfolio) => `
        <article class="portfolio-item">
          <h3>${portfolio.title}</h3>
          <p>${portfolio.summary}</p>
          <div class="badge-row">
            <span class="badge">${portfolio.owner}</span>
            <span class="badge">GET /portfolios/{id}</span>
          </div>
        </article>
      `,
    )
    .join("");
}

function renderChatMessages() {
  const chatWindow = document.querySelector("#chatMessages");
  chatWindow.innerHTML = chatMessages
    .map(
      (message) => `
        <div class="message ${message.from === "me" ? "me" : ""}">
          ${message.body}
        </div>
      `,
    )
    .join("");
}

function bindNavigation() {
  const navLinks = [...document.querySelectorAll(".nav a")];
  navLinks.forEach((link) => {
    link.addEventListener("click", () => {
      navLinks.forEach((item) => item.classList.remove("active"));
      link.classList.add("active");
    });
  });
}

function bindSearch() {
  const form = document.querySelector("#aiSearchForm");
  const input = document.querySelector("#aiSearchInput");
  const status = document.querySelector("#searchStatus");
  const modeButtons = [...document.querySelectorAll("[data-search-mode]")];
  let searchMode = "ai";

  modeButtons.forEach((button) => {
    button.addEventListener("click", () => {
      searchMode = button.dataset.searchMode;
      modeButtons.forEach((item) => item.classList.remove("active"));
      button.classList.add("active");
      status.textContent =
        searchMode === "ai"
          ? "AI 검색 실패 시 키워드 검색으로 자동 전환됩니다."
          : `${button.textContent} 검색 모드가 선택되었습니다.`;
    });
  });

  form.addEventListener("submit", (event) => {
    event.preventDefault();
    renderSearchResults(input.value);
    status.textContent =
      searchMode === "ai"
        ? `${apiRoutes.aiSearch.method} ${apiRoutes.aiSearch.url} 기준 추천 결과입니다.`
        : `${apiRoutes.keywordSearch.method} ${apiRoutes.keywordSearch.url} 기준 검색 결과입니다.`;
    document.querySelector("#search").scrollIntoView({ behavior: "smooth", block: "start" });
  });
}

function bindFilters() {
  const category = document.querySelector("#talentCategory");
  const filter = document.querySelector("#talentFilter");

  const applyFilter = () => {
    const selectedCategory = category.value;
    const keyword = filter.value.trim().toLowerCase();
    const filtered = talents.filter((talent) => {
      const categoryMatched = selectedCategory === "전체 카테고리" || talent.category === selectedCategory;
      const textMatched = `${talent.title} ${talent.summary} ${talent.owner}`.toLowerCase().includes(keyword);
      return categoryMatched && textMatched;
    });
    renderTalents(filtered);
  };

  category.addEventListener("change", applyFilter);
  filter.addEventListener("input", applyFilter);
}

function bindDrawer() {
  const drawer = document.querySelector("#formDrawer");
  const drawerTitle = document.querySelector("#drawerTitle");
  const labels = {
    "talent-form": "재능 등록",
    "request-form": "구매 요청 등록",
    "portfolio-form": "포트폴리오 아이템 추가",
    talent: "재능 등록",
    request: "구매 요청 등록",
    portfolio: "포트폴리오 아이템 추가",
    chat: "채팅방 생성",
  };

  document.querySelectorAll("[data-open-drawer], [data-view]").forEach((button) => {
    button.addEventListener("click", () => {
      const key = button.dataset.openDrawer || button.dataset.view;
      drawerTitle.textContent = labels[key] || "등록";
      drawer.classList.add("open");
      drawer.setAttribute("aria-hidden", "false");
    });
  });

  document.querySelector("[data-close-drawer]").addEventListener("click", () => {
    drawer.classList.remove("open");
    drawer.setAttribute("aria-hidden", "true");
  });

  document.querySelector(".stack-form").addEventListener("submit", (event) => {
    event.preventDefault();
    drawer.classList.remove("open");
    drawer.setAttribute("aria-hidden", "true");
  });
}

function bindAuthDialog() {
  const dialog = document.querySelector("#authDialog");
  const mode = document.querySelector("#authMode");
  const nicknameField = document.querySelector("#nicknameField");

  document.querySelectorAll("[data-open-modal]").forEach((button) => {
    button.addEventListener("click", () => {
      const isSignup = button.dataset.openModal === "signup";
      mode.textContent = isSignup ? "회원가입" : "로그인";
      nicknameField.hidden = !isSignup;
      dialog.showModal();
    });
  });
}

function bindMessageForm() {
  document.querySelector("#messageForm").addEventListener("submit", (event) => {
    event.preventDefault();
    const input = event.currentTarget.querySelector("input");
    const body = input.value.trim();
    if (!body) return;
    chatMessages.push({ from: "me", body });
    input.value = "";
    renderChatMessages();
  });
}

renderTalents();
renderRequests();
renderSearchResults();
renderPortfolios();
renderChatMessages();
bindNavigation();
bindSearch();
bindFilters();
bindDrawer();
bindAuthDialog();
bindMessageForm();
