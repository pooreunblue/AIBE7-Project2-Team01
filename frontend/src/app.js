import { formatMoney, shell } from "./shared/ui/index.js";
import { parseRoute, resolvePage } from "./router.js";
import { login, logout, signup } from "./features/auth/authApi.js";
import { setAccessToken, setRefreshToken } from "./auth/tokenStorage.js";
import { fetchCategories } from "./features/category/categoryApi.js";
import { initChatPage, teardownChatPage } from "./features/chat/ChatPage.js";
import { startChat } from "./features/chat/startChat.js";
import {
  createPortfolio,
  deletePortfolio,
  deletePortfolioFile,
  getMyPortfolios,
  getPortfolioFiles,
  setPortfolioThumbnail,
  updatePortfolioFile,
  uploadPortfolioFile,
} from "./features/portfolio/portfolioApi.js";
import { createRequest, fetchRequest, fetchRequests } from "./features/request/requestApi.js";
import { getMyPage } from "./features/user/userApi.js";
import { chargeWallet } from "./features/wallet/walletApi.js";

const app = document.querySelector("#app");
let accountMenuOutsideHandler = null;

handleOAuthSuccess();

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
  bindHeaderProfileImage();
  bindMyPage();
  bindPortfolioPage();
  bindCategoryTabs();
  bindRequestListPage();
  bindRequestDetailPage();
  bindRequestCreatePage();
  initChatPage();

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
  const preview = signupForm.querySelector("[data-profile-preview]");
  let previewUrl = null;

  fileInput?.addEventListener("change", () => {
    if (previewUrl) {
      URL.revokeObjectURL(previewUrl);
      previewUrl = null;
    }

    const file = fileInput.files[0];
    if (!file) {
      if (preview) preview.textContent = "○";
      if (fileName) fileName.textContent = "선택사항 · JPG, PNG 파일";
      return;
    }

    previewUrl = URL.createObjectURL(file);
    if (preview) preview.innerHTML = `<img src="${previewUrl}" alt="" />`;
    if (fileName) fileName.textContent = file.name;
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
        profileImage: fileInput?.files[0] || null,
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

async function bindHeaderProfileImage() {
  const avatar = document.querySelector("[data-header-avatar]");
  if (!avatar) return;

  try {
    const myPage = await getMyPage();
    renderHeaderAvatar(myPage);
  } catch {
    avatar.textContent = "○";
  }
}

function renderHeaderAvatar(myPage) {
  const avatar = document.querySelector("[data-header-avatar]");
  if (!avatar) return;

  if (myPage.profileImageUrl) {
    avatar.innerHTML = `<img src="${escapeHtml(myPage.profileImageUrl)}" alt="" />`;
    return;
  }

  avatar.textContent = (myPage.nickname || myPage.email || "?").charAt(0).toUpperCase();
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
  if (list) {
    loadPortfolioList();
  }
  bindPortfolioForm();
}

function bindCategoryTabs() {
  const tabRows = document.querySelectorAll("[data-category-tabs]");
  if (!tabRows.length) return;

  loadCategoryTabs(tabRows);
}

function bindRequestListPage() {
  const list = document.querySelector("[data-request-list]");
  if (!list) return;

  const searchForm = document.querySelector("[data-list-search]");
  loadRequestList();

  searchForm?.addEventListener("submit", (event) => {
    event.preventDefault();
    const keyword = new FormData(searchForm).get("keyword") || searchForm.querySelector("input")?.value || "";
    loadRequestList(String(keyword));
  });
}

function bindRequestDetailPage() {
  const detail = document.querySelector("[data-request-detail]");
  if (!detail) return;

  loadRequestDetail(detail.dataset.requestDetail);
}

function bindRequestCreatePage() {
  const form = document.querySelector("[data-request-create-form]");
  if (!form) return;

  loadRequestCategories();

  form.addEventListener("submit", async (event) => {
    event.preventDefault();

    const message = form.querySelector("[data-request-create-message]");
    const formData = new FormData(form);
    const budgetMin = Number(formData.get("budgetMin"));
    const budgetMax = Number(formData.get("budgetMax"));

    if (budgetMax < budgetMin) {
      if (message) message.textContent = "최대 예산은 최소 예산보다 커야 합니다.";
      return;
    }

    try {
      if (message) message.textContent = "";
      const request = await createRequest({
        title: formData.get("title"),
        content: formData.get("content"),
        categoryId: Number(formData.get("categoryId")),
        budgetMin,
        budgetMax,
      });
      window.location.hash = `/request/${request.requestPostId}`;
    } catch (error) {
      if (message) message.textContent = error.message;
    }
  });
}

async function loadRequestList(keyword = "") {
  const list = document.querySelector("[data-request-list]");
  if (!list) return;

  try {
    const requests = await fetchRequests(keyword);
    list.innerHTML = requests.length
      ? requests.map(renderRequestCard).join("")
      : `<article class="request-card"><span class="kicker">EMPTY</span><h3>등록된 의뢰글이 없습니다.</h3><p>첫 의뢰글을 작성해 보세요.</p></article>`;
  } catch (error) {
    list.innerHTML = `<article class="request-card"><span class="kicker">ERROR</span><h3>의뢰글을 불러오지 못했습니다.</h3><p>${escapeHtml(error.message)}</p></article>`;
  }
}

async function loadRequestDetail(requestPostId) {
  try {
    const request = await fetchRequest(requestPostId);
    renderRequestDetail(request);
    bindRequestChatButton(request);
  } catch (error) {
    setText("[data-request-category]", "ERROR");
    setText("[data-request-title]", "의뢰글을 불러오지 못했습니다.");
    setText("[data-request-content]", error.message);
  }
}

async function loadRequestCategories() {
  const select = document.querySelector("[data-request-category-select]");
  if (!select) return;

  try {
    const categories = await fetchCategories();
    select.innerHTML = categories.length
      ? categories.map((category) => `<option value="${category.categoryId}">${escapeHtml(category.name)}</option>`).join("")
      : `<option value="">등록된 카테고리가 없습니다</option>`;
  } catch {
    select.innerHTML = `<option value="">카테고리를 불러오지 못했습니다</option>`;
  }
}

async function loadCategoryTabs(tabRows) {
  try {
    const categories = await fetchCategories();
    tabRows.forEach((row) => {
      const href = row.dataset.categoryHref || "#/talents";
      const active = row.dataset.categoryActive || "All";
      const items = [{ name: "All", href }, ...categories.map((category) => ({
        name: category.name,
        href: `${href}?categoryId=${category.categoryId}`,
      }))];

      row.innerHTML = items.map((item) => `
        <a class="tab ${item.name === active ? "active" : ""}" href="${item.href}">${escapeHtml(item.name)}</a>
      `).join("");
    });
  } catch {
    tabRows.forEach((row) => {
      const href = row.dataset.categoryHref || "#/talents";
      row.innerHTML = `<a class="tab active" href="${href}">All</a>`;
    });
  }
}

function renderRequestCard(request) {
  return `
    <article class="request-card">
      <span class="kicker">${escapeHtml(request.categoryName || "의뢰")}</span>
      <h3><a href="#/request/${request.requestPostId}">${escapeHtml(request.title)}</a></h3>
      <p>${escapeHtml(request.content)}</p>
      <dl>
        <div><dt>Budget</dt><dd>${formatBudget(request)}</dd></div>
        <div><dt>Status</dt><dd>${escapeHtml(request.status || "-")}</dd></div>
      </dl>
      <a class="button quiet" href="#/request/${request.requestPostId}">상세보기</a>
    </article>
  `;
}

function renderRequestDetail(request) {
  setText("[data-request-category]", `${request.categoryName || "의뢰"} · ${request.status || "-"}`);
  setText("[data-request-title]", request.title);
  setText("[data-request-content]", request.content);
  setText("[data-request-budget]", formatBudget(request));
  setText("[data-request-meta]", `작성자 #${request.userId} · 등록일 ${formatDate(request.createdAt)}`);
}

function bindRequestChatButton(request) {
  const button = document.querySelector("[data-request-chat]");
  if (!button) return;

  button.disabled = false;
  button.addEventListener("click", async () => {
    button.disabled = true;
    try {
      await startChat({
        requestPostId: request.requestPostId,
        otherUserId: request.userId,
      });
    } catch (error) {
      alert(error.message);
      button.disabled = false;
    }
  });
}

function formatBudget(request) {
  return `${formatMoney(Number(request.budgetMin || 0))} - ${formatMoney(Number(request.budgetMax || 0))}`;
}

function escapeHtml(value) {
  return String(value ?? "").replace(/[&<>"']/g, (char) => (
    { "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[char]
  ));
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
  renderMyPageAvatar(myPage);
  setText("[data-my-page-nickname]", myPage.nickname);
  setText("[data-my-page-email]", myPage.email);
  setText("[data-my-page-created-at]", `가입일 ${formatDate(myPage.createdAt)}`);
  setText("[data-my-page-wallet]", formatMoney(Number(myPage.walletBalance || 0)));
}

function renderMyPageAvatar(myPage) {
  const avatar = document.querySelector("[data-my-page-avatar]");
  if (!avatar) return;

  if (myPage.profileImageUrl) {
    avatar.innerHTML = `<img src="${escapeHtml(myPage.profileImageUrl)}" alt="" />`;
    return;
  }

  avatar.textContent = (myPage.nickname || myPage.email || "?").charAt(0).toUpperCase();
}

async function loadPortfolioPreview() {
  const preview = document.querySelector("[data-portfolio-preview]");
  if (!preview) return;

  try {
    const portfolios = await getMyPortfolios();
    const portfoliosWithFiles = await Promise.all(
      portfolios.slice(0, 6).map(async (portfolio) => ({
        ...portfolio,
        files: await getPortfolioFiles(portfolio.portfolioId),
      }))
    );
    preview.innerHTML = renderPortfolioPreviewCards(portfoliosWithFiles);
  } catch (error) {
    preview.innerHTML = `<article class="portfolio-card"><span>ERROR</span><h3>포트폴리오를 불러오지 못했습니다.</h3><p>${error.message}</p></article>`;
  }
}

async function loadPortfolioList() {
  const list = document.querySelector("[data-portfolio-list]");
  if (!list) return;

  try {
    const portfolios = await getMyPortfolios();
    const portfoliosWithFiles = await Promise.all(
      portfolios.map(async (portfolio) => ({
        ...portfolio,
        files: await getPortfolioFiles(portfolio.portfolioId),
      }))
    );
    list.innerHTML = portfolios.length
      ? renderPortfolioPreviewCards(portfoliosWithFiles)
      : `<article class="portfolio-card text-only"><h3>등록된 포트폴리오가 없습니다.</h3><p>마이페이지에서 포트폴리오 정보를 확인할 수 있습니다.</p></article>`;
  } catch (error) {
    list.innerHTML = `<article class="portfolio-card text-only"><h3>포트폴리오를 불러오지 못했습니다.</h3><p>${escapeHtml(error.message)}</p></article>`;
  }
}

function bindPortfolioForm() {
  const form = document.querySelector("[data-portfolio-form]");
  if (!form) return;

  const fileInput = form.querySelector('input[name="portfolioFiles"]');
  const fileName = form.querySelector("[data-portfolio-file-name]");

  fileInput?.addEventListener("change", () => {
    const files = Array.from(fileInput.files || []);
    if (fileName) {
      fileName.textContent = files.length
        ? formatSelectedFiles(files)
        : "선택사항 · 여러 이미지와 작업 자료를 함께 올릴 수 있습니다.";
    }
  });

  form.addEventListener("submit", async (event) => {
    event.preventDefault();

    const message = form.querySelector("[data-portfolio-message]");
    const formData = new FormData(form);

    try {
      if (message) message.textContent = "";
      const portfolio = await createPortfolio({
        title: formData.get("title"),
        description: formData.get("description"),
      });
      const files = Array.from(fileInput?.files || []);
      if (files.length) {
        const uploadedFiles = [];
        for (const file of files) {
          uploadedFiles.push(await uploadPortfolioFile(portfolio.portfolioId, file));
        }
        if (message) {
          message.textContent = `${uploadedFiles.length}개 파일까지 업로드했습니다.`;
        }
      }
      form.reset();
      if (fileName) fileName.textContent = "선택사항 · 여러 이미지와 작업 자료를 함께 올릴 수 있습니다.";
      const list = document.querySelector("[data-portfolio-list]");
      if (list) {
        await loadPortfolioList();
      } else {
        window.location.hash = "/portfolios";
      }
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

function bindPortfolioFileActions() {
  document.querySelectorAll("[data-portfolio-file-delete]").forEach((button) => {
    button.addEventListener("click", async () => {
      await deletePortfolioFile(button.dataset.portfolioId, button.dataset.portfolioFileDelete);
      await loadPortfolioList();
    });
  });

  document.querySelectorAll("[data-portfolio-file-thumbnail]").forEach((button) => {
    button.addEventListener("click", async () => {
      await setPortfolioThumbnail(button.dataset.portfolioId, button.dataset.portfolioFileThumbnail);
      await loadPortfolioList();
    });
  });

  document.querySelectorAll("[data-portfolio-file-update]").forEach((input) => {
    input.addEventListener("change", async () => {
      const file = input.files[0];
      if (!file) return;
      await updatePortfolioFile(input.dataset.portfolioId, input.dataset.portfolioFileUpdate, file);
      input.value = "";
      await loadPortfolioList();
    });
  });
}

function renderPortfolioPreviewCards(portfolios) {
  if (!portfolios.length) {
    return `<article class="portfolio-card"><span>EMPTY</span><h3>등록된 포트폴리오가 없습니다.</h3><p>포트폴리오 관리에서 첫 항목을 등록해 보세요.</p></article>`;
  }

  return portfolios
    .map((portfolio) => `
      <a class="portfolio-card ${getPortfolioPreviewImage(portfolio) ? "has-media" : "text-only"}" href="#/portfolios">
        ${renderPortfolioPreviewMedia(portfolio)}
        <div>
          <h3>${escapeHtml(portfolio.title)}</h3>
          <p>${escapeHtml(portfolio.description)}</p>
        </div>
      </a>
    `)
    .join("");
}

function renderPortfolioPreviewMedia(portfolio) {
  const image = getPortfolioPreviewImage(portfolio);
  if (!image) return "";

  return `
    <div class="portfolio-card-media">
      <img src="${escapeHtml(image.fileUrl)}" alt="" />
      ${image.thumbnail ? `<small>대표</small>` : ""}
    </div>
  `;
}

function getPortfolioPreviewImage(portfolio) {
  const imageFiles = (portfolio.files || []).filter((file) => String(file.contentType || "").startsWith("image/"));
  return imageFiles.find((file) => file.thumbnail) || imageFiles[0] || null;
}

function renderPortfolioManageCard(portfolio) {
  return `
    <article class="summary-card portfolio-manage-card">
      <div>
        <span class="kicker">Portfolio</span>
        <h2>${escapeHtml(portfolio.title)}</h2>
        <p>${escapeHtml(portfolio.description)}</p>
        ${renderPortfolioFiles(portfolio.portfolioId, portfolio.files || [])}
      </div>
      <div class="card-action">
        <span>${formatDate(portfolio.createdAt)}</span>
        <button class="button quiet" type="button" data-portfolio-delete="${portfolio.portfolioId}">삭제</button>
      </div>
    </article>
  `;
}

function renderPortfolioFiles(portfolioId, files) {
  if (!files.length) {
    return `<div class="portfolio-file-list empty">첨부된 파일이 없습니다.</div>`;
  }

  return `
    <div class="portfolio-file-list">
      ${files.map((file) => renderPortfolioFileItem(portfolioId, file)).join("")}
    </div>
  `;
}

function renderPortfolioFileItem(portfolioId, file) {
  const isImage = String(file.contentType || "").startsWith("image/");
  return `
    <div class="portfolio-file-item">
      <a class="portfolio-file-link" href="${escapeHtml(file.fileUrl)}" target="_blank" rel="noreferrer">
        ${isImage ? `<img src="${escapeHtml(file.fileUrl)}" alt="" />` : `<span>${fileIcon(file.contentType)}</span>`}
        <strong>${escapeHtml(file.originalFileName)}</strong>
        ${file.thumbnail ? `<small>대표</small>` : ""}
      </a>
      <div class="portfolio-file-actions">
        ${isImage && !file.thumbnail ? `<button class="button quiet" type="button" data-portfolio-id="${portfolioId}" data-portfolio-file-thumbnail="${file.portfolioFileId}">대표</button>` : ""}
        <label class="button quiet">
          교체
          <input type="file" data-portfolio-id="${portfolioId}" data-portfolio-file-update="${file.portfolioFileId}" />
        </label>
        <button class="button quiet" type="button" data-portfolio-id="${portfolioId}" data-portfolio-file-delete="${file.portfolioFileId}">삭제</button>
      </div>
    </div>
  `;
}

function fileIcon(contentType) {
  if (String(contentType || "").includes("pdf")) return "PDF";
  return "FILE";
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

function formatFileSize(bytes) {
  if (!Number.isFinite(bytes) || bytes <= 0) return "0 KB";
  if (bytes < 1024 * 1024) return `${Math.ceil(bytes / 1024)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

function formatSelectedFiles(files) {
  const totalSize = files.reduce((sum, file) => sum + file.size, 0);
  if (files.length === 1) {
    return `${files[0].name} · ${formatFileSize(files[0].size)}`;
  }

  return `${files[0].name} 외 ${files.length - 1}개 · 총 ${formatFileSize(totalSize)}`;
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

function handleOAuthSuccess() {
  if (window.location.pathname !== "/oauth2/success") return;

  const params = new URLSearchParams(window.location.search);
  const accessToken = params.get("accessToken");
  const refreshToken = params.get("refreshToken");

  if (accessToken && refreshToken) {
    setAccessToken(accessToken);
    setRefreshToken(refreshToken);
  }

  window.history.replaceState(null, "", "/index.html#/home");
}
