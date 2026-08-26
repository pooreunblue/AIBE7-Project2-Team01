import { formatMoney, shell } from "./shared/ui/index.js";
import { parseRoute, resolvePage } from "./router.js";
import { login, logout, signup } from "./features/auth/authApi.js";
import { createPortfolio, deletePortfolio, getMyPortfolios } from "./features/portfolio/portfolioApi.js";
import { getMyPage } from "./features/user/userApi.js";
import { chargeWallet } from "./features/wallet/walletApi.js";

const app = document.querySelector("#app");
let accountMenuOutsideHandler = null;

function render() {
  teardownChatPage();
  const { route, content } = resolvePage(parseRoute());
  app.innerHTML = shell(content, route);
  bindPageEvents();
  window.scrollTo({ top: 0, behavior: "instant" });
}

function bindPageEvents() {
  bindHomeFlow();
  bindLoginForm();
  bindSignupForm();
  bindAccountMenu();
  bindMyPage();
  bindPortfolioPage();

  document.querySelectorAll("form").forEach((form) => {
    form.addEventListener("submit", (event) => {
      event.preventDefault();
      if (form.matches("[data-search-form]")) {
        window.location.hash = "/ai-search";
      }
    });
  });
}

function bindSignupForm() {
  const signupForm = document.querySelector("[data-signup-form]");
  if (!signupForm) return;

  const fileInput = signupForm.querySelector('input[name="profileImage"]');
  const fileName = signupForm.querySelector("[data-profile-file-name]");

  fileInput?.addEventListener("change", () => {
    fileName.textContent = fileInput.files[0]?.name || "선택사항 · JPG, PNG 파일";
  });

  signupForm.addEventListener("submit", async (event) => {
    event.preventDefault();

    const message = signupForm.querySelector("[data-signup-message]");
    const formData = new FormData(signupForm);

    try {
      message.textContent = "";
      await signup({
        email: formData.get("email"),
        password: formData.get("password"),
        nickname: formData.get("nickname"),
      });
      window.location.hash = "/login";
    } catch (error) {
      message.textContent = error.message;
    }
  });
}

function bindMyPage() {
  const summary = document.querySelector("[data-my-page-summary]");
  if (!summary) return;

  bindWalletChargeModal();
  loadMyPage();
  loadPortfolioPreview();
}

function bindWalletChargeModal() {
  const modal = document.querySelector("[data-wallet-charge-modal]");
  const form = document.querySelector("[data-wallet-charge-form]");
  const openButton = document.querySelector("[data-wallet-charge-open]");
  const closeButtons = document.querySelectorAll("[data-wallet-charge-close]");
  const message = document.querySelector("[data-wallet-charge-message]");
  const amountInput = form?.querySelector('input[name="amount"]');
  if (!modal || !form || !openButton || !amountInput) return;

  const openModal = () => {
    modal.hidden = false;
    if (message) message.textContent = "";
    amountInput.focus();
  };

  const closeModal = () => {
    modal.hidden = true;
    form.reset();
    if (message) message.textContent = "";
  };

  openButton.addEventListener("click", openModal);
  closeButtons.forEach((button) => button.addEventListener("click", closeModal));

  modal.addEventListener("click", (event) => {
    if (event.target === modal) {
      closeModal();
    }
  });

  form.addEventListener("submit", async (event) => {
    event.preventDefault();

    const amount = Number(new FormData(form).get("amount"));
    if (!Number.isFinite(amount) || amount < 1000) {
      if (message) message.textContent = "1,000원 이상 입력해 주세요.";
      return;
    }

    try {
      if (message) message.textContent = "";
      const wallet = await chargeWallet(amount);
      setText("[data-my-page-wallet]", formatMoney(Number(wallet.balance || 0)));
      closeModal();
      await loadMyPage();
    } catch (error) {
      if (message) message.textContent = error.message;
    }
  });
}

function bindPortfolioPage() {
  const list = document.querySelector("[data-portfolio-list]");
  if (!list) return;

  loadPortfolioList();
  bindPortfolioForm();
}

async function loadMyPage() {
  const message = document.querySelector("[data-my-page-message]");

  try {
    const myPage = await getMyPage();
    renderMyPage(myPage);
    if (message) {
      message.textContent = "";
    }
  } catch (error) {
    if (message) {
      message.textContent = error.message;
    }
  }
}

function renderMyPage(myPage) {
  setText("[data-my-page-nickname]", myPage.nickname);
  setText("[data-my-page-email]", myPage.email);
  setText("[data-my-page-created-at]", `가입일 ${formatDate(myPage.createdAt)}`);
  setText("[data-my-page-wallet]", formatMoney(Number(myPage.walletBalance || 0)));
}

async function loadPortfolioPreview() {
  const preview = document.querySelector("[data-portfolio-preview]");
  if (!preview) return;

  try {
    const portfolios = await getMyPortfolios();
    preview.innerHTML = renderPortfolioPreviewCards(portfolios.slice(0, 6));
  } catch (error) {
    preview.innerHTML = `<article class="portfolio-card"><span>ERROR</span><h3>포트폴리오를 불러오지 못했습니다.</h3><p>${error.message}</p></article>`;
  }
}

async function loadPortfolioList() {
  const list = document.querySelector("[data-portfolio-list]");
  if (!list) return;

  try {
    const portfolios = await getMyPortfolios();
    list.innerHTML = portfolios.length
      ? portfolios.map(renderPortfolioManageCard).join("")
      : `<article class="summary-card empty-state"><h2>등록된 포트폴리오가 없습니다.</h2><p>첫 포트폴리오를 등록해 보세요.</p></article>`;
    bindPortfolioDeleteButtons();
  } catch (error) {
    list.innerHTML = `<article class="summary-card empty-state"><h2>포트폴리오를 불러오지 못했습니다.</h2><p>${error.message}</p></article>`;
  }
}

function bindPortfolioForm() {
  const form = document.querySelector("[data-portfolio-form]");
  if (!form) return;

  form.addEventListener("submit", async (event) => {
    event.preventDefault();

    const message = form.querySelector("[data-portfolio-message]");
    const formData = new FormData(form);

    try {
      if (message) message.textContent = "";
      await createPortfolio({
        title: formData.get("title"),
        description: formData.get("description"),
      });
      form.reset();
      await loadPortfolioList();
    } catch (error) {
      if (message) message.textContent = error.message;
    }
  });
}

function bindPortfolioDeleteButtons() {
  document.querySelectorAll("[data-portfolio-delete]").forEach((button) => {
    button.addEventListener("click", async () => {
      await deletePortfolio(button.dataset.portfolioDelete);
      await loadPortfolioList();
    });
  });
}

function renderPortfolioPreviewCards(portfolios) {
  if (!portfolios.length) {
    return `<article class="portfolio-card"><span>EMPTY</span><h3>등록된 포트폴리오가 없습니다.</h3><p>포트폴리오 관리에서 첫 항목을 등록해 보세요.</p></article>`;
  }

  return portfolios
    .map((portfolio, index) => `
      <a class="portfolio-card" href="#/portfolios">
        <span>${String(index + 1).padStart(2, "0")}</span>
        <h3>${portfolio.title}</h3>
        <p>${portfolio.description}</p>
      </a>
    `)
    .join("");
}

function renderPortfolioManageCard(portfolio) {
  return `
    <article class="summary-card portfolio-manage-card">
      <div>
        <span class="kicker">Portfolio</span>
        <h2>${portfolio.title}</h2>
        <p>${portfolio.description}</p>
      </div>
      <div class="card-action">
        <span>${formatDate(portfolio.createdAt)}</span>
        <button class="button quiet" type="button" data-portfolio-delete="${portfolio.portfolioId}">삭제</button>
      </div>
    </article>
  `;
}

function setText(selector, value) {
  const element = document.querySelector(selector);
  if (element) {
    element.textContent = value ?? "-";
  }
}

function formatDate(value) {
  if (!value) return "-";

  return new Intl.DateTimeFormat("ko-KR", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
  }).format(new Date(value));
}

function bindAccountMenu() {
  if (accountMenuOutsideHandler) {
    document.removeEventListener("click", accountMenuOutsideHandler);
    accountMenuOutsideHandler = null;
  }

  const trigger = document.querySelector("[data-account-trigger]");
  const menu = document.querySelector("[data-account-menu]");
  if (!trigger || !menu) return;

  trigger.addEventListener("click", () => {
    const isOpen = trigger.getAttribute("aria-expanded") === "true";
    trigger.setAttribute("aria-expanded", String(!isOpen));
    menu.hidden = isOpen;
  });

  accountMenuOutsideHandler = (event) => {
    if (trigger.contains(event.target) || menu.contains(event.target)) return;
    trigger.setAttribute("aria-expanded", "false");
    menu.hidden = true;
  };
  document.addEventListener("click", accountMenuOutsideHandler);

  document.querySelector("[data-logout-button]")?.addEventListener("click", async () => {
    await logout();
    window.location.hash = "/home";
    render();
  });
}

function bindLoginForm() {
  const loginForm = document.querySelector("[data-login-form]");
  if (!loginForm) return;

  loginForm.addEventListener("submit", async (event) => {
    event.preventDefault();

    const message = loginForm.querySelector("[data-form-message]");
    const formData = new FormData(loginForm);

    try {
      message.textContent = "";
      await login({
        email: formData.get("email"),
        password: formData.get("password"),
      });
      window.location.hash = "/home";
    } catch (error) {
      message.textContent = error.message;
    }
  });
}

function bindHomeFlow() {
  const flow = document.querySelector("[data-primary-flow]");
  if (!flow) return;

  const triggers = [...flow.querySelectorAll("[data-flow-trigger]")];
  const panels = [...flow.querySelectorAll("[data-flow-options]")];

  triggers.forEach((trigger) => {
    trigger.addEventListener("click", () => {
      const target = trigger.dataset.flowTrigger;
      const isOpen = trigger.getAttribute("aria-expanded") === "true";

      triggers.forEach((item) => {
        item.classList.remove("active");
        item.setAttribute("aria-expanded", "false");
      });

      panels.forEach((panel) => {
        panel.hidden = true;
        panel.classList.remove("open");
      });

      if (isOpen) return;

      const panel = flow.querySelector(`[data-flow-options="${target}"]`);
      trigger.classList.add("active");
      trigger.setAttribute("aria-expanded", "true");
      panel.hidden = false;
      requestAnimationFrame(() => panel.classList.add("open"));
    });
  });
}

if (!window.location.hash) {
  window.location.hash = "/home";
}

window.addEventListener("hashchange", render);
render();
