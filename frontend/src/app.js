import { formatMoney, shell } from "./shared/ui/index.js";
import {
  appendSafeHtml,
  escapeHtml,
  safeImageUrl,
  safeUrl,
  setSafeHtml,
} from "./shared/security/xss.js";
import { uploadTempImage } from "./api/uploadApi.js";
import { ErrorPage, errorState } from "./features/error/ErrorPage.js";
import { parseRoute, resolvePage } from "./router.js";
import { login, logout, signup } from "./features/auth/authApi.js";
import { getCurrentUser, getCurrentUserId } from "./auth/currentUser.js";
import { fetchCategories } from "./features/category/categoryApi.js";
import { initChatPage, teardownChatPage } from "./features/chat/ChatPage.js";
import { startChat } from "./features/chat/startChat.js";
import { initCheckoutPage } from "./features/payment/CheckoutPage.js";
import { initAiSearchPage } from "./features/search/AiSearchPage.js";
import {
  createPortfolio,
  deletePortfolio,
  deletePortfolioFile,
  getPortfolio,
  getMyPortfolios,
  getPortfolioFiles,
  setPortfolioThumbnail,
  updatePortfolio,
  updatePortfolioFile,
  uploadPortfolioFile,
} from "./features/portfolio/portfolioApi.js";
import {
  createRequest,
  fetchRequest,
  fetchRequestPage,
  generateRequestPost,
  getRequestFiles,
  setRequestThumbnail,
  updateRequest,
  uploadRequestFile,
} from "./features/request/requestApi.js";
import {
  createTalent,
  deleteTalent,
  fetchTalent,
  fetchTalentPage,
  generateTalentPost,
  getTalentFiles,
  inactiveTalent,
  setTalentThumbnail,
  updateTalent,
  uploadTalentFile,
} from "./features/talent/talentApi.js";
import { getMyPage } from "./features/user/userApi.js";
import { chargeWallet } from "./features/wallet/walletApi.js";

const app = document.querySelector("#app");
let accountMenuOutsideHandler = null;
const portfolioCache = new Map();
let renderSequence = 0;

const isHandlingOAuthSuccess = handleOAuthSuccess();

async function render() {
  const sequence = ++renderSequence;
  teardownChatPage();

  let route = "error";
  let content = ErrorPage(500);
  try {
    ({ route, content } = resolvePage(parseRoute()));
  } catch (error) {
    content = ErrorPage(500, error?.message || "");
  }

  const currentUser = await getCurrentUser({ optional: true });
  if (sequence !== renderSequence) return;
  setSafeHtml(app, shell(content, route, { isLoggedIn: Boolean(currentUser) }));
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
  bindTalentListPage();
  bindTalentDetailPage();
  bindTalentCreatePage();
  bindRequestListPage();
  bindRequestDetailPage();
  bindRequestCreatePage();
  initAiSearchPage();
  initChatPage();
  initCheckoutPage();
  bindErrorPage();

  document.querySelectorAll("form").forEach((form) => {
    form.querySelector("[data-home-ai-submit]")?.addEventListener("click", () => {
      const formData = new FormData(form);
      const query = String(formData.get("query") || "").trim();
      if (query) {
        window.location.hash = `/ai-search?query=${encodeURIComponent(query)}&mode=ai`;
      } else {
        window.location.hash = "/ai-search?mode=ai";
      }
    });

    form.addEventListener("submit", (event) => {
      event.preventDefault();
      if (form.matches("[data-search-form]")) {
        const query = String(new FormData(form).get("query") || "").trim();
        const params = new URLSearchParams();
        if (query) params.set("query", query);
        const suffix = params.toString() ? `?${params}` : "";
        window.location.hash = `/ai-search${suffix}`;
      }
    });
  });
}

// 상세 페이지처럼 스켈레톤을 먼저 그린 뒤 데이터 로딩이 실패했을 때,
// 라우트 영역 전체를 400/500번대 예외 화면으로 교체한다.
function showRouteError(error) {
  const routeView = document.querySelector(".route-view");
  if (!routeView) return;
  setSafeHtml(routeView, errorState(error));
  bindErrorPage();
}

function bindErrorPage() {
  const backButton = document.querySelector("[data-error-back]");
  if (!backButton) return;

  backButton.addEventListener("click", () => {
    if (window.history.length > 1) {
      window.history.back();
    } else {
      window.location.hash = "/home";
    }
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
    if (preview) {
      const image = document.createElement("img");
      image.src = safeImageUrl(previewUrl);
      image.alt = "";
      preview.replaceChildren(image);
    }
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
    setSafeHtml(avatar, `<img src="${escapeHtml(safeImageUrl(myPage.profileImageUrl))}" alt="" />`);
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
  if (!list && !document.querySelector("[data-portfolio-form]")) return;
  bindPortfolioForm();
  bindMarkdownImageUpload();
  bindPortfolioMarkdownPreview();
}

function bindPortfolioMarkdownPreview() {
  const titleInput = document.querySelector("[data-portfolio-title-input]");
  const markdownInput = document.querySelector("[data-portfolio-markdown-input]");
  const preview = document.querySelector("[data-markdown-preview]");
  if (!titleInput || !markdownInput || !preview) return;

  const renderPreview = () => {
    setSafeHtml(preview, renderMarkdown(titleInput.value, markdownInput.value));
  };

  titleInput.addEventListener("input", renderPreview);
  markdownInput.addEventListener("input", renderPreview);
  renderPreview();
}

function bindMarkdownImageUpload() {
  const trigger = document.querySelector("[data-markdown-image-trigger]");
  const input = document.querySelector("[data-markdown-image-input]");
  const textarea = document.querySelector(".portfolio-content-input");
  const message = document.querySelector("[data-portfolio-message], [data-talent-message]");
  if (!trigger || !input || !textarea) return;

  trigger.addEventListener("click", () => {
    input.click();
  });

  input.addEventListener("change", async () => {
    const file = input.files[0];
    if (!file) return;

    try {
      trigger.disabled = true;
      if (message) message.textContent = "이미지를 업로드하는 중입니다.";
      const uploaded = await uploadTempImage(file);
      insertTextAtCursor(textarea, markdownImageText(file.name, uploaded.url));
      if (message) message.textContent = "본문에 이미지를 삽입했습니다.";
    } catch (error) {
      if (message) message.textContent = error.message;
    } finally {
      trigger.disabled = false;
      input.value = "";
      textarea.focus();
    }
  });
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
  const loadMoreButton = document.querySelector("[data-request-load-more]");
  const categoryId = currentCategoryId();
  let keyword = "";
  let nextPage = 0;
  let loading = false;

  const loadPage = async (reset = false) => {
    if (loading) return;
    if (reset) nextPage = 0;

    loading = true;
    if (loadMoreButton) loadMoreButton.disabled = true;
    try {
      const page = await loadRequestList(keyword, categoryId, nextPage, reset);
      nextPage = Number(page.page || 0) + 1;
      if (loadMoreButton) loadMoreButton.hidden = Boolean(page.last);
    } catch {
      if (loadMoreButton) loadMoreButton.hidden = true;
    } finally {
      loading = false;
      if (loadMoreButton) loadMoreButton.disabled = false;
    }
  };

  loadPage(true);

  searchForm?.addEventListener("submit", (event) => {
    event.preventDefault();
    keyword = String(new FormData(searchForm).get("keyword") || searchForm.querySelector("input")?.value || "");
    loadPage(true);
  });

  loadMoreButton?.addEventListener("click", () => loadPage(false));
}

function bindRequestDetailPage() {
  const detail = document.querySelector("[data-request-detail]");
  if (!detail) return;

  loadRequestDetail(detail.dataset.requestDetail);
}

function bindRequestCreatePage() {
  const form = document.querySelector("[data-request-create-form]");
  if (!form) return;

  const requestPostId = getRequestEditId();
  const thumbnailInput = form.querySelector("[data-request-thumbnail-input]");
  const thumbnailTrigger = form.querySelector("[data-request-thumbnail-trigger]");
  const thumbnailPreview = form.querySelector("[data-request-thumbnail-preview]");
  let thumbnailFile = null;
  let thumbnailPreviewUrl = null;
  let existingFiles = [];

  loadRequestCategories().then(async () => {
    if (requestPostId) {
      existingFiles = await loadRequestEditForm(form, requestPostId);
    }
    renderRequestSettingsSummary(form);
    renderTalentThumbnailPreview(thumbnailPreview, existingFiles, thumbnailFile, thumbnailPreviewUrl);
  });
  bindMarkdownImageUpload();
  bindPortfolioMarkdownPreview();
  bindRequestSettingsModal(form);

  thumbnailTrigger?.addEventListener("click", () => thumbnailInput?.click());

  thumbnailInput?.addEventListener("change", () => {
    const message = form.querySelector("[data-request-create-message]");
    thumbnailFile = thumbnailInput.files[0] || null;
    if (thumbnailFile && !isImageFile(thumbnailFile)) {
      if (message) message.textContent = "대표 이미지는 이미지 파일만 선택할 수 있습니다.";
      thumbnailFile = null;
      thumbnailInput.value = "";
    } else if (message) {
      message.textContent = "";
    }
    if (thumbnailPreviewUrl) URL.revokeObjectURL(thumbnailPreviewUrl);
    thumbnailPreviewUrl = thumbnailFile ? URL.createObjectURL(thumbnailFile) : null;
    renderTalentThumbnailPreview(thumbnailPreview, existingFiles, thumbnailFile, thumbnailPreviewUrl);
  });

  form.querySelector("[data-request-ai-generate]")?.addEventListener("click", async (event) => {
    const button = event.currentTarget;
    const message = form.querySelector("[data-request-create-message]");
    if (!validateRequestSettings(form)) {
      if (message) message.textContent = "AI 작성 전에 카테고리와 예산을 입력해 주세요.";
      openRequestSettingsModal(form);
      return;
    }
    const formData = new FormData(form);
    button.disabled = true;
    if (message) message.textContent = "AI가 글을 작성하는 중입니다.";
    try {
      const generated = await generateRequestPost({
        content: formData.get("content"),
        categoryId: formData.get("categoryId"),
        budgetMin: Number(formData.get("budgetMin")),
        budgetMax: Number(formData.get("budgetMax")),
        dueDate: formData.get("dueDate") || null,
      }, thumbnailFile);
      const titleInput = form.querySelector("[data-portfolio-title-input]");
      const contentInput = form.querySelector("[data-portfolio-markdown-input]");
      titleInput.value = generated.title || "";
      contentInput.value = generated.content || "";
      titleInput.dispatchEvent(new Event("input", { bubbles: true }));
      contentInput.dispatchEvent(new Event("input", { bubbles: true }));
      if (message) message.textContent = "AI 작성 결과를 확인하고 수정해 주세요.";
    } catch (error) {
      if (message) message.textContent = error.message;
    } finally {
      button.disabled = false;
    }
  });

  thumbnailPreview?.addEventListener("click", (event) => {
    if (!event.target.closest("[data-remove-selected-talent-thumbnail]")) return;
    thumbnailFile = null;
    if (thumbnailInput) thumbnailInput.value = "";
    if (thumbnailPreviewUrl) {
      URL.revokeObjectURL(thumbnailPreviewUrl);
      thumbnailPreviewUrl = null;
    }
    renderTalentThumbnailPreview(thumbnailPreview, existingFiles, thumbnailFile, thumbnailPreviewUrl);
  });

  requestAnimationFrame(() => openRequestSettingsModal(form));

  form.addEventListener("submit", async (event) => {
    event.preventDefault();

    const message = form.querySelector("[data-request-create-message]");
    const payload = buildRequestPayload(form);
    if (!payload) {
      if (message) message.textContent = "상세정보를 먼저 입력해 주세요.";
      openRequestSettingsModal(form);
      return;
    }

    try {
      if (message) message.textContent = "";
      const request = requestPostId
        ? await updateRequest(requestPostId, payload)
        : await createRequest(payload);
      if (thumbnailFile) {
        const thumbnail = await uploadRequestFile(request.requestPostId, thumbnailFile);
        await setRequestThumbnail(request.requestPostId, thumbnail.requestPostFileId);
      }
      if (thumbnailPreviewUrl) URL.revokeObjectURL(thumbnailPreviewUrl);
      window.location.hash = `/request/${request.requestPostId}`;
    } catch (error) {
      if (message) message.textContent = error.message;
    }
  });
}

async function loadRequestEditForm(form, requestPostId) {
  const submitButton = form.querySelector("button[type='submit']");
  const titleInput = form.querySelector("[data-portfolio-title-input]");
  const contentInput = form.querySelector("[data-portfolio-markdown-input]");
  const message = form.querySelector("[data-request-create-message]");

  if (submitButton) submitButton.textContent = "요청글 수정";

  try {
    const [request, files] = await Promise.all([
      fetchRequest(requestPostId),
      getRequestFiles(requestPostId).catch(() => []),
    ]);

    if (titleInput) {
      titleInput.value = request.title || "";
      titleInput.dispatchEvent(new Event("input", { bubbles: true }));
    }
    if (contentInput) {
      contentInput.value = request.content || "";
      contentInput.dispatchEvent(new Event("input", { bubbles: true }));
    }
    setFormValue(form, "categoryId", request.categoryId);
    setFormValue(form, "budgetMin", request.budgetMin);
    setFormValue(form, "budgetMax", request.budgetMax);
    setFormValue(form, "dueDate", request.dueDate);

    return files;
  } catch (error) {
    if (message) message.textContent = `요청글을 불러오지 못했습니다: ${error.message}`;
    return [];
  }
}

async function loadRequestList(keyword = "", categoryId = "", pageNumber = 0, reset = true) {
  const list = document.querySelector("[data-request-list]");
  if (!list) return { content: [], page: 0, last: true };

  try {
    const page = await fetchRequestPage({ keyword, page: pageNumber });
    const filteredRequests = filterByCategory(page.content, categoryId);
    const requestsWithFiles = await Promise.all(
      filteredRequests.map(async (request) => ({
        ...request,
        files: await getRequestFiles(request.requestPostId).catch(() => []),
      }))
    );

    const cards = requestsWithFiles.map(renderRequestCard).join("");
    if (reset) {
      setSafeHtml(list, cards || `<article class="request-card request-list-card"><div class="card-body"><span class="kicker">EMPTY</span><h3>등록된 의뢰글이 없습니다.</h3><p>검색 조건을 조정하거나 첫 의뢰글을 작성해 보세요.</p></div></article>`);
    } else if (cards) {
      appendSafeHtml(list, "beforeend", cards);
    }
    return page;
  } catch (error) {
    if (reset) {
      setSafeHtml(list, `<article class="request-card request-list-card"><div class="card-body"><span class="kicker">ERROR</span><h3>의뢰글을 불러오지 못했습니다.</h3><p>${escapeHtml(error.message)}</p></div></article>`);
    }
    throw error;
  }
}

async function loadRequestDetail(requestPostId) {
  try {
    const [request, files] = await Promise.all([
      fetchRequest(requestPostId),
      getRequestFiles(requestPostId).catch(() => []),
    ]);
    renderRequestDetail(request, files);
    bindRequestChatButton(request);
  } catch (error) {
    showRouteError(error);
  }
}

async function loadRequestCategories() {
  const select = document.querySelector("[data-request-category-select]");
  if (!select) return;

  try {
    const categories = await fetchCategories();
    setSafeHtml(select, categories.length
      ? `<option value="">카테고리 선택</option>${categories.map((category) => `<option value="${category.categoryId}">${escapeHtml(category.name)}</option>`).join("")}`
      : `<option value="">등록된 카테고리가 없습니다</option>`);
  } catch {
    setSafeHtml(select, `<option value="">카테고리를 불러오지 못했습니다</option>`);
  }
}

function bindRequestSettingsModal(form) {
  const renderSelected = () => renderRequestSettingsSummary(form);

  form.querySelector("[data-request-settings-open]")?.addEventListener("click", () => {
    openRequestSettingsModal(form);
  });

  form.querySelectorAll("[data-request-settings-close]").forEach((button) => {
    button.addEventListener("click", () => closeRequestSettingsModal(form));
  });

  form.querySelector("[data-request-settings-save]")?.addEventListener("click", () => {
    const message = form.querySelector("[data-request-create-message]");
    if (!validateRequestSettings(form)) {
      if (message) message.textContent = "카테고리와 예산을 확인해 주세요.";
      return;
    }
    if (message) message.textContent = "";
    renderRequestSettingsSummary(form);
    closeRequestSettingsModal(form);
  });

  form.querySelector("[data-request-settings-modal]")?.addEventListener("click", (event) => {
    if (event.target === event.currentTarget) closeRequestSettingsModal(form);
  });

  form.querySelector("[data-request-category-select]")?.addEventListener("change", renderSelected);
  form.querySelector('input[name="budgetMin"]')?.addEventListener("input", renderSelected);
  form.querySelector('input[name="budgetMax"]')?.addEventListener("input", renderSelected);
  form.querySelector('input[name="dueDate"]')?.addEventListener("input", renderSelected);
  renderRequestSettingsSummary(form);
}

function openRequestSettingsModal(form) {
  const modal = form.querySelector("[data-request-settings-modal]");
  if (!modal) return;
  modal.hidden = false;
  document.body.classList.add("modal-open");
  form.querySelector("[data-request-category-select]")?.focus();
}

function closeRequestSettingsModal(form) {
  const modal = form.querySelector("[data-request-settings-modal]");
  if (!modal) return;
  modal.hidden = true;
  document.body.classList.remove("modal-open");
}

function buildRequestPayload(form) {
  if (!validateRequestSettings(form)) return null;
  const formData = new FormData(form);
  return {
    title: formData.get("postTitle"),
    content: formData.get("content"),
    categoryId: formData.get("categoryId"),
    budgetMin: Number(formData.get("budgetMin")),
    budgetMax: Number(formData.get("budgetMax")),
    dueDate: formData.get("dueDate") || null,
  };
}

function validateRequestSettings(form) {
  const formData = new FormData(form);
  const categoryId = formData.get("categoryId");
  const budgetMin = Number(formData.get("budgetMin"));
  const budgetMax = Number(formData.get("budgetMax"));
  return Boolean(
    categoryId &&
    Number.isFinite(budgetMin) && budgetMin >= 0 &&
    Number.isFinite(budgetMax) && budgetMax >= budgetMin
  );
}

function renderRequestSettingsSummary(form) {
  const summary = form.querySelector("[data-request-detail-summary]");
  if (!summary) return;

  if (!validateRequestSettings(form)) {
    setSafeHtml(summary, `<span>상세 설정을 입력해 주세요.</span>`);
    return;
  }

  const formData = new FormData(form);
  const category = selectedOptionText(form.querySelector("[data-request-category-select]"));
  const dueDate = formData.get("dueDate");
  setSafeHtml(summary, `
    <span>${escapeHtml(category)}</span>
    <strong>${formatMoney(Number(formData.get("budgetMin")))} - ${formatMoney(Number(formData.get("budgetMax")))}</strong>
    <span>${dueDate ? `마감 ${escapeHtml(dueDate)}` : "일정 협의"}</span>
  `);
}

async function loadCategoryTabs(tabRows) {
  try {
    const categories = await fetchCategories();
    tabRows.forEach((row) => {
      const href = row.dataset.categoryHref || "#/talents";
      const activeCategoryId = currentCategoryId();
      const items = [{ name: "All", href, categoryId: "" }, ...categories.map((category) => ({
        name: category.name,
        categoryId: category.categoryId,
        href: `${href}?categoryId=${category.categoryId}`,
      }))];

      setSafeHtml(row, items.map((item) => `
        <a class="tab ${String(item.categoryId || "") === String(activeCategoryId || "") ? "active" : ""}" href="${escapeHtml(safeUrl(item.href))}">${escapeHtml(item.name)}</a>
      `).join(""));
    });
  } catch {
    tabRows.forEach((row) => {
      const href = row.dataset.categoryHref || "#/talents";
      setSafeHtml(row, `<a class="tab active" href="${escapeHtml(safeUrl(href))}">All</a>`);
    });
  }
}

function bindTalentListPage() {
  const list = document.querySelector("[data-list='talents']");
  if (!list) return;

  const searchForm = document.querySelector("[data-list-search]");
  const loadMoreButton = document.querySelector("[data-talent-load-more]");
  const categoryId = currentCategoryId();
  let keyword = "";
  let nextPage = 0;
  let loading = false;

  const loadPage = async (reset = false) => {
    if (loading) return;
    if (reset) nextPage = 0;

    loading = true;
    if (loadMoreButton) loadMoreButton.disabled = true;
    try {
      const page = await loadTalentList(keyword, categoryId, nextPage, reset);
      nextPage = Number(page.page || 0) + 1;
      if (loadMoreButton) loadMoreButton.hidden = Boolean(page.last);
    } catch {
      if (loadMoreButton) loadMoreButton.hidden = true;
    } finally {
      loading = false;
      if (loadMoreButton) loadMoreButton.disabled = false;
    }
  };

  loadPage(true);

  searchForm?.addEventListener("submit", (event) => {
    event.preventDefault();
    keyword = String(new FormData(searchForm).get("keyword") || searchForm.querySelector("input")?.value || "");
    loadPage(true);
  });

  loadMoreButton?.addEventListener("click", () => loadPage(false));
}

function bindTalentDetailPage() {
  const detail = document.querySelector("[data-talent-detail]");
  if (!detail) return;

  loadTalentDetail(detail.dataset.talentDetail);
}

function bindTalentCreatePage() {
  const form = document.querySelector("[data-talent-form]");
  if (!form) return;

  const thumbnailInput = form.querySelector("[data-talent-thumbnail-input]");
  const thumbnailTrigger = form.querySelector("[data-talent-thumbnail-trigger]");
  const thumbnailPreview = form.querySelector("[data-talent-thumbnail-preview]");
  const talentPostId = getTalentEditId();
  let thumbnailFile = null;
  let thumbnailPreviewUrl = null;
  let existingFiles = [];

  bindMarkdownImageUpload();
  bindPortfolioMarkdownPreview();
  bindTalentSettingsModal(form);
  bindTalentPortfolioModal(form);

  thumbnailTrigger?.addEventListener("click", () => thumbnailInput?.click());

  thumbnailInput?.addEventListener("change", () => {
    const message = form.querySelector("[data-talent-message]");
    thumbnailFile = thumbnailInput.files[0] || null;
    if (thumbnailFile && !isImageFile(thumbnailFile)) {
      if (message) message.textContent = "대표 이미지는 이미지 파일만 선택할 수 있습니다.";
      thumbnailFile = null;
      thumbnailInput.value = "";
    } else if (message) {
      message.textContent = "";
    }
    if (thumbnailPreviewUrl) URL.revokeObjectURL(thumbnailPreviewUrl);
    thumbnailPreviewUrl = thumbnailFile ? URL.createObjectURL(thumbnailFile) : null;
    renderTalentThumbnailPreview(thumbnailPreview, existingFiles, thumbnailFile, thumbnailPreviewUrl);
  });

  form.querySelector("[data-talent-ai-generate]")?.addEventListener("click", async (event) => {
    const button = event.currentTarget;
    const message = form.querySelector("[data-talent-message]");
    if (!validateTalentSettings(form)) {
      if (message) message.textContent = "AI 작성 전에 카테고리와 상세정보를 입력해 주세요.";
      openTalentSettingsModal(form);
      return;
    }
    const formData = new FormData(form);
    button.disabled = true;
    if (message) message.textContent = "AI가 글을 작성하는 중입니다.";
    try {
      const generated = await generateTalentPost({
        content: formData.get("content"),
        categoryId: formData.get("categoryId"),
        price: Number(formData.get("price")),
        estimatedDuration: Number(formData.get("estimatedDuration")),
        durationUnit: formData.get("durationUnit"),
        portfolioId: formData.get("portfolioId") || null,
      }, thumbnailFile);
      const titleInput = form.querySelector("[data-portfolio-title-input]");
      const contentInput = form.querySelector("[data-portfolio-markdown-input]");
      titleInput.value = generated.title || "";
      contentInput.value = generated.content || "";
      titleInput.dispatchEvent(new Event("input", { bubbles: true }));
      contentInput.dispatchEvent(new Event("input", { bubbles: true }));
      if (message) message.textContent = "AI 작성 결과를 확인하고 수정해 주세요.";
    } catch (error) {
      if (message) message.textContent = error.message;
    } finally {
      button.disabled = false;
    }
  });

  thumbnailPreview?.addEventListener("click", (event) => {
    if (!event.target.closest("[data-remove-selected-talent-thumbnail]")) return;
    thumbnailFile = null;
    if (thumbnailInput) thumbnailInput.value = "";
    if (thumbnailPreviewUrl) {
      URL.revokeObjectURL(thumbnailPreviewUrl);
      thumbnailPreviewUrl = null;
    }
    renderTalentThumbnailPreview(thumbnailPreview, existingFiles, thumbnailFile, thumbnailPreviewUrl);
  });

  loadTalentSettingsOptions(form).then(() => {
    if (talentPostId) {
      loadTalentEditForm(form, talentPostId).then((files) => {
        existingFiles = files || [];
        renderTalentThumbnailPreview(thumbnailPreview, existingFiles, thumbnailFile, thumbnailPreviewUrl);
      });
    } else {
      requestAnimationFrame(() => openTalentSettingsModal(form));
    }
  });

  form.addEventListener("submit", async (event) => {
    event.preventDefault();
    const message = form.querySelector("[data-talent-message]");
    const payload = buildTalentPayload(form);

    if (!payload) {
      if (message) message.textContent = "상세정보를 먼저 입력해 주세요.";
      openTalentSettingsModal(form);
      return;
    }

    try {
      if (message) message.textContent = "";
      const talent = talentPostId
        ? await updateTalent(talentPostId, payload)
        : await createTalent(payload);
      if (thumbnailFile) {
        const thumbnail = await uploadTalentFile(talent.talentPostId, thumbnailFile);
        await setTalentThumbnail(talent.talentPostId, thumbnail.talentPostFileId);
      }
      if (thumbnailPreviewUrl) URL.revokeObjectURL(thumbnailPreviewUrl);
      window.location.hash = `/talent/${talent.talentPostId}`;
    } catch (error) {
      if (message) message.textContent = error.message;
    }
  });
}

async function loadTalentList(keyword = "", categoryId = "", pageNumber = 0, reset = true) {
  const list = document.querySelector("[data-list='talents']");
  if (!list) return { content: [], page: 0, last: true };

  try {
    const page = await fetchTalentPage({ keyword, page: pageNumber });
    const filteredTalents = filterByCategory(page.content, categoryId);
    const talentsWithFiles = await Promise.all(
      filteredTalents.map(async (talent) => ({
        ...talent,
        files: await getTalentFiles(talent.talentPostId).catch(() => []),
      }))
    );

    const cards = talentsWithFiles.map(renderTalentCard).join("");
    if (reset) {
      setSafeHtml(list, cards || `<article class="talent-card"><div class="card-body"><span class="kicker">EMPTY</span><h3>등록된 재능글이 없습니다.</h3><p>검색 조건을 조정하거나 첫 재능글을 작성해 보세요.</p></div></article>`);
    } else if (cards) {
      appendSafeHtml(list, "beforeend", cards);
    }
    return page;
  } catch (error) {
    if (reset) {
      setSafeHtml(list, `<article class="talent-card"><div class="card-body"><span class="kicker">ERROR</span><h3>재능글을 불러오지 못했습니다.</h3><p>${escapeHtml(error.message)}</p></div></article>`);
    }
    throw error;
  }
}

async function loadTalentDetail(talentPostId) {
  try {
    const [talent, files] = await Promise.all([
      fetchTalent(talentPostId),
      getTalentFiles(talentPostId).catch(() => []),
    ]);
    renderTalentDetail(talent, files);
    const fileTarget = document.querySelector("[data-talent-files]");
    setSafeHtml(fileTarget, renderTalentDetailFiles(files));
    bindTalentDetailActions(talent);
    loadLinkedPortfolio(talent);
  } catch (error) {
    showRouteError(error);
  }
}

async function loadLinkedPortfolio(talent) {
  const target = document.querySelector("[data-talent-linked-portfolio]");
  if (!target) return;

  if (!talent.portfolioId) {
    target.replaceChildren();
    return;
  }

  try {
    const portfolio = await getPortfolio(talent.portfolioId);
    const files = await getPortfolioFiles(talent.portfolioId).catch(() => []);
    portfolioCache.set(String(portfolio.portfolioId), { ...portfolio, files });
    setSafeHtml(target, renderLinkedPortfolio(portfolio, files));
    bindPortfolioCardOpen(target);
  } catch (error) {
    setSafeHtml(target, `<section class="linked-portfolio-section"><span class="kicker">Portfolio</span><p>연결된 포트폴리오를 불러오지 못했습니다: ${escapeHtml(error.message)}</p></section>`);
  }
}

function renderTalentCard(talent) {
  const thumbnail = getTalentPreviewImage(talent.files || []);
  return `
    <article class="talent-card">
      ${thumbnail ? `
      <a class="visual has-image" href="#/talent/${talent.talentPostId}" aria-label="${escapeHtml(talent.title)} 상세">
        <img src="${escapeHtml(safeImageUrl(thumbnail.fileUrl))}" alt="" />
        <span>${escapeHtml(talent.categoryName || "Talent")}</span>
      </a>
      ` : ""}
      <div class="card-body">
        <div class="meta-line">
          <span>${escapeHtml(authorLabel(talent))}</span>
          <strong>${escapeHtml(talent.status || "-")}</strong>
        </div>
        <h3><a href="#/talent/${talent.talentPostId}">${escapeHtml(talent.title)}</a></h3>
        <p>${escapeHtml(markdownExcerpt(talent.content))}</p>
        <div class="chip-row">
          <span>${formatDuration(talent)}</span>
          <span>${escapeHtml(talent.categoryName || "카테고리")}</span>
        </div>
        <div class="card-action">
          <strong>${formatOptionalMoney(talent.price)}</strong>
          <a class="button quiet" href="#/talent/${talent.talentPostId}">상세보기</a>
        </div>
      </div>
    </article>
  `;
}

function renderTalentDetail(talent, files = []) {
  setText("[data-talent-category]", talent.categoryName || "Talent");
  setText("[data-talent-meta]", `${talent.categoryName || "재능"} · 등록일 ${formatDate(talent.createdAt)}`);
  setText("[data-talent-title]", talent.title);
  renderTalentAuthor(talent);
  hideTalentAuthorMeta();
  setText("[data-talent-price]", formatOptionalMoney(talent.price));
  setText("[data-talent-duration]", `예상 작업기간 ${formatDuration(talent)}`);

  const content = document.querySelector("[data-talent-content]");
  setSafeHtml(content, renderMarkdown("", talent.content || ""));

  const hero = document.querySelector(".detail-hero");
  const thumbnail = getTalentPreviewImage(files);
  if (hero) {
    hero.classList.toggle("has-image", Boolean(thumbnail));
    setSafeHtml(hero, thumbnail
      ? `
        <img src="${escapeHtml(safeImageUrl(thumbnail.fileUrl))}" alt="" />
        <span data-talent-category>${escapeHtml(talent.categoryName || "Talent")}</span>
      `
      : `<span data-talent-category>${escapeHtml(talent.categoryName || "Talent")}</span>`);
  }
}

function renderTalentAuthor(talent) {
  const name = authorLabel(talent);
  setText("[data-talent-author]", name);

  const avatar = document.querySelector("[data-talent-avatar]");
  if (!avatar) return;

  if (talent.authorProfileImageUrl) {
    setSafeHtml(avatar, `<img src="${escapeHtml(safeImageUrl(talent.authorProfileImageUrl))}" alt="" />`);
    return;
  }

  avatar.textContent = String(talent.authorNickname || talent.userId || "?").charAt(0).toUpperCase();
}

function hideTalentAuthorMeta() {
  const status = document.querySelector("[data-talent-status]");
  if (!status) return;
  status.textContent = "";
  status.hidden = true;
}

async function bindTalentDetailActions(talent) {
  const actions = document.querySelector("[data-talent-actions]");
  const chatButton = document.querySelector("[data-talent-chat]");
  if (!actions || !chatButton) return;

  const currentUserId = await getCurrentUserId({ optional: true });
  const isOwner = currentUserId != null && String(talent.userId) === String(currentUserId);

  if (isOwner) {
    chatButton.remove();
    appendSafeHtml(actions, "beforeend", `
      <a class="button quiet" href="#/talent-new?id=${escapeHtml(talent.talentPostId)}">수정하기</a>
      <button class="button quiet" type="button" data-talent-inactive="${escapeHtml(talent.talentPostId)}">비활성화</button>
      <button class="button quiet danger" type="button" data-talent-delete="${escapeHtml(talent.talentPostId)}">삭제</button>
    `);
  } else {
    chatButton.disabled = false;
    chatButton.addEventListener("click", async () => {
      chatButton.disabled = true;
      try {
        await startChat({
          talentPostId: talent.talentPostId,
          otherUserId: talent.userId,
        });
      } catch (error) {
        alert(error.message);
        chatButton.disabled = false;
      }
    });
  }

  actions.querySelector("[data-talent-inactive]")?.addEventListener("click", async (event) => {
    const button = event.currentTarget;
    button.disabled = true;
    try {
      await inactiveTalent(button.dataset.talentInactive);
      await loadTalentDetail(button.dataset.talentInactive);
    } catch (error) {
      alert(error.message);
      button.disabled = false;
    }
  });

  actions.querySelector("[data-talent-delete]")?.addEventListener("click", async (event) => {
    if (!confirm("재능글을 삭제하시겠습니까?")) return;
    const button = event.currentTarget;
    button.disabled = true;
    try {
      await deleteTalent(button.dataset.talentDelete);
      window.location.hash = "/talents";
    } catch (error) {
      alert(error.message);
      button.disabled = false;
    }
  });
}

function bindTalentSettingsModal(form) {
  form.querySelector("[data-talent-settings-open]")?.addEventListener("click", () => {
    openTalentSettingsModal(form);
  });

  form.querySelectorAll("[data-talent-settings-close]").forEach((button) => {
    button.addEventListener("click", () => closeTalentSettingsModal(form));
  });

  form.querySelector("[data-talent-settings-save]")?.addEventListener("click", () => {
    const message = form.querySelector("[data-talent-message]");
    if (!validateTalentSettings(form)) {
      if (message) message.textContent = "카테고리를 선택해 주세요.";
      return;
    }
    if (message) message.textContent = "";
    renderTalentSettingsSummary(form);
    closeTalentSettingsModal(form);
  });

  const modal = form.querySelector("[data-talent-settings-modal]");
  modal?.addEventListener("click", (event) => {
    if (event.target === modal) closeTalentSettingsModal(form);
  });
}

function bindTalentPortfolioModal(form) {
  const modal = form.querySelector("[data-talent-portfolio-modal]");
  const list = form.querySelector("[data-talent-portfolio-list]");

  form.querySelector("[data-talent-portfolio-open]")?.addEventListener("click", () => {
    openTalentPortfolioModal(form);
  });

  form.querySelectorAll("[data-talent-portfolio-close]").forEach((button) => {
    button.addEventListener("click", () => closeTalentPortfolioModal(form));
  });

  form.querySelector("[data-talent-portfolio-clear]")?.addEventListener("click", () => {
    setTalentPortfolioValue(form, "", "");
    closeTalentPortfolioModal(form);
  });

  modal?.addEventListener("click", (event) => {
    if (event.target === modal) closeTalentPortfolioModal(form);
  });

  list?.addEventListener("click", (event) => {
    const button = event.target.closest("[data-select-talent-portfolio]");
    if (!button) return;
    setTalentPortfolioValue(form, button.dataset.selectTalentPortfolio, button.dataset.portfolioTitle || "");
    closeTalentPortfolioModal(form);
  });
}

async function loadTalentSettingsOptions(form) {
  const categorySelect = form.querySelector("[data-talent-category-select]");

  const renderSelected = () => renderTalentSettingsSummary(form);

  try {
    const categories = await fetchCategories();
    if (categorySelect) {
      setSafeHtml(categorySelect, categories.length
        ? `<option value="">카테고리 선택</option>${categories.map((category) => `<option value="${category.categoryId}">${escapeHtml(category.name)}</option>`).join("")}`
        : `<option value="">등록된 카테고리가 없습니다</option>`);
      categorySelect.addEventListener("change", renderSelected);
    }
  } catch {
    setSafeHtml(categorySelect, `<option value="">카테고리를 불러오지 못했습니다</option>`);
  }

  await loadTalentPortfolioOptions(form);

  form.querySelector('input[name="price"]')?.addEventListener("input", renderSelected);
  form.querySelector('input[name="estimatedDuration"]')?.addEventListener("input", renderSelected);
  form.querySelector('select[name="durationUnit"]')?.addEventListener("change", renderSelected);
  renderTalentSettingsSummary(form);
}

async function loadTalentPortfolioOptions(form) {
  const list = form.querySelector("[data-talent-portfolio-list]");
  if (!list) return;

  try {
    const portfolios = await getMyPortfolios();
    const portfoliosWithFiles = await Promise.all(
      portfolios.map(async (portfolio) => ({
        ...portfolio,
        files: await getPortfolioFiles(portfolio.portfolioId).catch(() => []),
      }))
    );

    form.__talentPortfolios = portfoliosWithFiles;
    cachePortfolios(portfoliosWithFiles);
    setSafeHtml(list, portfoliosWithFiles.length
      ? portfoliosWithFiles.map(renderTalentPortfolioOption).join("")
      : `<p>등록된 포트폴리오가 없습니다.</p>`);
    syncSelectedTalentPortfolio(form);
  } catch (error) {
    setSafeHtml(list, `<p>포트폴리오를 불러오지 못했습니다: ${escapeHtml(error.message)}</p>`);
  }
}

async function loadTalentEditForm(form, talentPostId) {
  const message = form.querySelector("[data-talent-message]");
  const submitButton = form.querySelector("[data-talent-submit]");

  try {
    if (message) message.textContent = "기존 재능글을 불러오는 중입니다.";
    if (submitButton) submitButton.textContent = "수정하기";
    const [talent, files] = await Promise.all([
      fetchTalent(talentPostId),
      getTalentFiles(talentPostId),
    ]);

    const titleInput = form.querySelector("[data-portfolio-title-input]");
    const contentInput = form.querySelector("[data-portfolio-markdown-input]");
    if (titleInput) {
      titleInput.value = talent.title || "";
      titleInput.dispatchEvent(new Event("input", { bubbles: true }));
    }
    if (contentInput) {
      contentInput.value = talent.content || "";
      contentInput.dispatchEvent(new Event("input", { bubbles: true }));
    }

    setFormValue(form, "categoryId", talent.categoryId);
    setFormValue(form, "price", talent.price);
    setFormValue(form, "estimatedDuration", talent.estimatedDuration);
    setFormValue(form, "durationUnit", talent.durationUnit || "DAY");
    setFormValue(form, "portfolioId", talent.portfolioId);
    syncSelectedTalentPortfolio(form);
    if (message) message.textContent = "";
    return files;
  } catch (error) {
    if (message) message.textContent = error.message;
    return [];
  }
}

function openTalentSettingsModal(form) {
  const modal = form.querySelector("[data-talent-settings-modal]");
  if (!modal) return;
  modal.hidden = false;
  document.body.classList.add("modal-open");
  form.querySelector("[data-talent-category-select]")?.focus();
}

function closeTalentSettingsModal(form) {
  const modal = form.querySelector("[data-talent-settings-modal]");
  if (!modal) return;
  modal.hidden = true;
  document.body.classList.remove("modal-open");
}

function openTalentPortfolioModal(form) {
  const modal = form.querySelector("[data-talent-portfolio-modal]");
  if (!modal) return;
  modal.hidden = false;
  document.body.classList.add("modal-open");
  modal.querySelector("[data-select-talent-portfolio], [data-talent-portfolio-clear], [data-talent-portfolio-close]")?.focus();
}

function closeTalentPortfolioModal(form) {
  const modal = form.querySelector("[data-talent-portfolio-modal]");
  if (!modal) return;
  modal.hidden = true;
  document.body.classList.remove("modal-open");
}

function setTalentPortfolioValue(form, portfolioId, title) {
  const input = form.querySelector("[data-talent-portfolio-value]");
  if (input) {
    input.value = portfolioId || "";
    input.dataset.portfolioTitle = title || "";
  }
  updateTalentPortfolioSelection(form);
  renderTalentSettingsSummary(form);
}

function syncSelectedTalentPortfolio(form) {
  const input = form.querySelector("[data-talent-portfolio-value]");
  if (!input || !input.value) {
    renderTalentSettingsSummary(form);
    return;
  }

  const selected = (form.__talentPortfolios || []).find((portfolio) => String(portfolio.portfolioId) === String(input.value));
  if (selected) input.dataset.portfolioTitle = selected.title || "";
  updateTalentPortfolioSelection(form);
  renderTalentSettingsSummary(form);
}

function updateTalentPortfolioSelection(form) {
  const selectedId = form.querySelector("[data-talent-portfolio-value]")?.value || "";
  form.querySelectorAll("[data-select-talent-portfolio]").forEach((button) => {
    const isSelected = Boolean(selectedId) && String(button.dataset.selectTalentPortfolio) === String(selectedId);
    button.classList.toggle("is-selected", isSelected);
    button.setAttribute("aria-pressed", String(isSelected));
  });
}

function buildTalentPayload(form) {
  if (!validateTalentSettings(form)) return null;
  const formData = new FormData(form);
  return {
    title: formData.get("postTitle"),
    content: formData.get("content"),
    categoryId: formData.get("categoryId"),
    price: optionalNumber(formData.get("price")),
    estimatedDuration: optionalNumber(formData.get("estimatedDuration")),
    durationUnit: formData.get("estimatedDuration") ? (formData.get("durationUnit") || "DAY") : null,
    portfolioId: optionalValue(formData.get("portfolioId")),
  };
}

function validateTalentSettings(form) {
  const formData = new FormData(form);
  const categoryId = formData.get("categoryId");
  const rawPrice = formData.get("price");
  const rawDuration = formData.get("estimatedDuration");
  return Boolean(
    categoryId &&
    (!rawPrice || Number(rawPrice) >= 0) &&
    (!rawDuration || Number(rawDuration) > 0)
  );
}

function optionalValue(value) {
  return value ? String(value) : null;
}

function renderTalentSettingsSummary(form) {
  const summary = form.querySelector("[data-talent-detail-summary]");
  if (!summary) return;

  const formData = new FormData(form);
  const category = selectedOptionText(form.querySelector("[data-talent-category-select]"));
  const portfolioTitle = form.querySelector("[data-talent-portfolio-value]")?.dataset.portfolioTitle || "";
  const price = optionalNumber(formData.get("price"));
  const duration = optionalNumber(formData.get("estimatedDuration"));
  const durationUnit = durationUnitLabel(formData.get("durationUnit"));

  if (!validateTalentSettings(form)) {
    setSafeHtml(summary, `<span>상세 설정을 입력해 주세요.</span>`);
    return;
  }

  setSafeHtml(summary, `
    <span>${escapeHtml(category)}</span>
    <strong>${formatOptionalMoney(price)}</strong>
    <span>${duration ? `예상 ${duration}${durationUnit}` : "기간 협의"}</span>
    <span>${portfolioTitle ? escapeHtml(portfolioTitle) : "포트폴리오 미연결"}</span>
  `);
}

function optionalNumber(value) {
  if (value == null || value === "") return null;
  const number = Number(value);
  return Number.isFinite(number) ? number : null;
}

function isImageFile(file) {
  return String(file?.type || "").startsWith("image/");
}

function renderTalentThumbnailPreview(container, existingFiles, thumbnailFile = null, previewUrl = null) {
  if (!container) return;

  const existingThumbnail = existingFiles.find((file) => file.thumbnail) || null;
  const imageUrl = previewUrl || existingThumbnail?.fileUrl || "";

  container.hidden = !imageUrl;
  setSafeHtml(container, imageUrl
    ? `
      <img src="${escapeHtml(safeImageUrl(imageUrl))}" alt="" />
      <div>
        <span>대표 이미지</span>
        <strong>${escapeHtml(thumbnailFile?.name || existingThumbnail?.originalFileName || "대표 이미지")}</strong>
      </div>
      ${thumbnailFile ? `<button type="button" aria-label="대표 이미지 선택 취소" data-remove-selected-talent-thumbnail>x</button>` : ""}
    `
    : "");
}

function renderTalentDetailFiles(files) {
  const materialFiles = files.filter((file) => !file.thumbnail);
  if (!materialFiles.length) return "";

  return `
    <section class="talent-file-section">
      <h2>재능 자료</h2>
      <div class="talent-file-grid">
        ${materialFiles.map(renderTalentDetailFile).join("")}
      </div>
    </section>
  `;
}

function renderTalentDetailFile(file) {
  const isImage = String(file.contentType || "").startsWith("image/");
  return `
    <a class="talent-file-card ${isImage ? "is-image" : ""}" href="${escapeHtml(safeUrl(file.fileUrl))}" target="_blank" rel="noopener noreferrer">
      ${isImage ? `<img src="${escapeHtml(safeImageUrl(file.fileUrl))}" alt="" />` : `<span>${fileIcon(file.contentType)}</span>`}
      <div>
        <strong>${escapeHtml(file.originalFileName)}</strong>
        <small>${formatFileSize(Number(file.fileSize || 0))}${file.thumbnail ? " · 대표" : ""}</small>
      </div>
    </a>
  `;
}

function renderTalentPortfolioOption(portfolio) {
  const image = getPortfolioFilePreviewImage(portfolio);
  return `
    <button class="talent-portfolio-option ${image ? "has-media" : "text-only"}" type="button" aria-pressed="false" data-select-talent-portfolio="${escapeHtml(portfolio.portfolioId)}" data-portfolio-title="${escapeHtml(portfolio.title || "")}">
      ${image ? `<span class="talent-portfolio-thumb"><img src="${escapeHtml(safeImageUrl(image.fileUrl))}" alt="" /></span>` : ""}
      <div>
        <strong>${escapeHtml(portfolio.title || "제목 없는 포트폴리오")}</strong>
        <span>${escapeHtml(markdownExcerpt(portfolio.description || "") || "설명 없음")}</span>
        <small>첨부 ${(portfolio.files || []).length}개 · 업데이트 ${formatDate(portfolio.updatedAt)}</small>
      </div>
    </button>
  `;
}

function getPortfolioFilePreviewImage(portfolio) {
  const imageFiles = (portfolio.files || []).filter((file) => String(file.contentType || "").startsWith("image/"));
  return imageFiles.find((file) => file.thumbnail) || imageFiles[0] || null;
}

function renderLinkedPortfolio(portfolio, files) {
  const image = getPortfolioPreviewImage({ ...portfolio, files });
  return `
    <section class="linked-portfolio-section">
      <div class="linked-portfolio-head">
        <span class="kicker">Linked Portfolio</span>
      </div>
      <button class="linked-portfolio-card ${image ? "has-media" : "text-only"}" type="button" data-portfolio-detail="${escapeHtml(portfolio.portfolioId)}" aria-label="${escapeHtml(portfolio.title)} 포트폴리오 상세 보기">
        ${image ? `<img src="${escapeHtml(safeImageUrl(image.fileUrl))}" alt="" />` : ""}
        <div>
          <h2>${escapeHtml(portfolio.title)}</h2>
          <p>${escapeHtml(markdownExcerpt(portfolio.description || ""))}</p>
          <small>첨부 ${files.length}개 · 업데이트 ${formatDate(portfolio.updatedAt)}</small>
        </div>
      </button>
    </section>
  `;
}

function getTalentPreviewImage(files) {
  const imageFiles = files.filter((file) => String(file.contentType || "").startsWith("image/"));
  return imageFiles.find((file) => file.thumbnail) || imageFiles[0] || null;
}

function getRequestPreviewImage(files) {
  const imageFiles = files.filter((file) => String(file.contentType || "").startsWith("image/"));
  return imageFiles.find((file) => file.thumbnail) || imageFiles[0] || null;
}

function selectedOptionText(select) {
  if (!select || !select.value) return "";
  return select.options[select.selectedIndex]?.textContent || "";
}

function setFormValue(form, name, value) {
  const input = form.elements[name];
  if (!input || value == null) return;
  input.value = String(value);
  input.dispatchEvent(new Event("change", { bubbles: true }));
}

function getTalentEditId() {
  const query = window.location.hash.split("?")[1] || "";
  return new URLSearchParams(query).get("id");
}

function getRequestEditId() {
  const query = window.location.hash.split("?")[1] || "";
  return new URLSearchParams(query).get("id");
}

function currentCategoryId() {
  const query = window.location.hash.split("?")[1] || "";
  return new URLSearchParams(query).get("categoryId") || "";
}

function filterByCategory(posts, categoryId) {
  if (!categoryId) return posts;
  return posts.filter((post) => String(post.categoryId || "") === String(categoryId));
}

function markdownExcerpt(markdown) {
  return String(markdown || "")
    .replace(/!\[[^\]]*]\([^)]*\)/g, "")
    .replace(/\[([^\]]+)]\([^)]*\)/g, "$1")
    .replace(/```[\s\S]*?```/g, "")
    .replace(/`([^`]+)`/g, "$1")
    .replace(/^#{1,6}\s*/gm, "")
    .replace(/^>\s*/gm, "")
    .replace(/[*_~]/g, "")
    .replace(/^\s*[-+]\s+/gm, "")
    .replace(/^\s*\d+\.\s+/gm, "")
    .replace(/\s+/g, " ")
    .trim()
    .slice(0, 120);
}

function formatDuration(talent) {
  if (!talent.estimatedDuration) return "협의";
  return `${Number(talent.estimatedDuration)}${durationUnitLabel(talent.durationUnit)}`;
}

function formatOptionalMoney(value) {
  return value == null ? "가격 협의" : formatMoney(Number(value));
}

function durationUnitLabel(unit) {
  const labels = { DAY: "일", WEEK: "주", MONTH: "개월" };
  return labels[unit] || "일";
}

function renderRequestCard(request) {
  const thumbnail = getRequestPreviewImage(request.files || []);
  return `
    <article class="request-card request-list-card">
      ${thumbnail ? `
      <a class="visual has-image" href="#/request/${request.requestPostId}" aria-label="${escapeHtml(request.title)} 상세">
        <img src="${escapeHtml(safeImageUrl(thumbnail.fileUrl))}" alt="" />
        <span>${escapeHtml(request.categoryName || "Request")}</span>
      </a>
      ` : ""}
      <div class="card-body">
        <div class="meta-line">
          <span>${escapeHtml(authorLabel(request))}</span>
          <strong>${escapeHtml(request.status || "-")}</strong>
        </div>
        <h3><a href="#/request/${request.requestPostId}">${escapeHtml(request.title)}</a></h3>
        <p>${escapeHtml(markdownExcerpt(request.content))}</p>
        <div class="chip-row">
          <span>${escapeHtml(request.categoryName || "카테고리")}</span>
          <span>${request.dueDate ? `마감 ${escapeHtml(request.dueDate)}` : "일정 협의"}</span>
        </div>
        <div class="card-action">
          <strong>${formatBudget(request)}</strong>
          <a class="button quiet" href="#/request/${request.requestPostId}">상세보기</a>
        </div>
      </div>
    </article>
  `;
}

function renderRequestDetail(request, files = []) {
  setText("[data-request-category]", request.categoryName || "Request");
  setText("[data-request-meta-line]", `${request.categoryName || "의뢰"} · ${request.status || "-"} · 등록일 ${formatDate(request.createdAt)}`);
  setText("[data-request-title]", request.title);
  setText("[data-request-budget]", formatBudget(request));
  setText("[data-request-meta]", `${authorLabel(request)} · 등록일 ${formatDate(request.createdAt)}`);

  const content = document.querySelector("[data-request-content]");
  setSafeHtml(content, renderMarkdown("", request.content || ""));

  const hero = document.querySelector(".detail-hero");
  const thumbnail = getRequestPreviewImage(files);
  if (hero) {
    hero.classList.toggle("has-image", Boolean(thumbnail));
    setSafeHtml(hero, thumbnail
      ? `
        <img src="${escapeHtml(safeImageUrl(thumbnail.fileUrl))}" alt="" />
        <span data-request-category>${escapeHtml(request.categoryName || "Request")}</span>
      `
      : `<span data-request-category>${escapeHtml(request.categoryName || "Request")}</span>`);
  }
}

async function bindRequestChatButton(request) {
  const button = document.querySelector("[data-request-chat]");
  if (!button) return;

  const currentUserId = await getCurrentUserId({ optional: true });
  const isOwner = currentUserId != null && String(request.userId) === String(currentUserId);

  button.disabled = false;
  button.textContent = isOwner ? "수정하기" : "요청자와 채팅";
  button.addEventListener("click", async () => {
    button.disabled = true;
    if (isOwner) {
      window.location.hash = `/request-new?id=${request.requestPostId}`;
      return;
    }

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

function authorLabel(post) {
  return post.authorNickname || post.nickname || "작성자";
}

function formatBudget(request) {
  return `${formatMoney(Number(request.budgetMin || 0))} - ${formatMoney(Number(request.budgetMax || 0))}`;
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
    setSafeHtml(avatar, `<img src="${escapeHtml(safeImageUrl(myPage.profileImageUrl))}" alt="" />`);
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
    cachePortfolios(portfoliosWithFiles);
    setSafeHtml(preview, renderPortfolioPreviewCards(portfoliosWithFiles));
    bindPortfolioCardOpen(preview);
  } catch (error) {
    setSafeHtml(preview, `<article class="portfolio-card"><span>ERROR</span><h3>포트폴리오를 불러오지 못했습니다.</h3><p>${escapeHtml(error.message)}</p></article>`);
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
    cachePortfolios(portfoliosWithFiles);
    setSafeHtml(list, portfolios.length
      ? renderPortfolioPreviewCards(portfoliosWithFiles, { showEdit: true })
      : `<article class="portfolio-card text-only"><h3>등록된 포트폴리오가 없습니다.</h3><p>마이페이지에서 포트폴리오 정보를 확인할 수 있습니다.</p></article>`);
    bindPortfolioCardOpen(list);
    bindPortfolioCardActions(list);
  } catch (error) {
    setSafeHtml(list, `<article class="portfolio-card text-only"><h3>포트폴리오를 불러오지 못했습니다.</h3><p>${escapeHtml(error.message)}</p></article>`);
  }
}

function bindPortfolioForm() {
  const form = document.querySelector("[data-portfolio-form]");
  if (!form) return;

  const portfolioId = getPortfolioEditId();
  const titleInput = form.querySelector("[data-portfolio-title-input]");
  const descriptionInput = form.querySelector('textarea[name="description"]');
  const fileInput = form.querySelector('input[name="portfolioFiles"]');
  const fileName = form.querySelector("[data-portfolio-file-name]");
  const selectedFileList = form.querySelector("[data-selected-portfolio-files]");
  const submitButton = form.querySelector("[data-portfolio-submit]");
  let selectedFiles = [];
  let existingFiles = [];

  if (portfolioId) {
    loadPortfolioEditForm({
      portfolioId,
      titleInput,
      descriptionInput,
      selectedFileList,
      fileName,
      submitButton,
    }).then((files) => {
      existingFiles = files;
    });
  }

  fileInput?.addEventListener("change", () => {
    selectedFiles = [...selectedFiles, ...Array.from(fileInput.files || [])];
    fileInput.value = "";
    renderPortfolioEditorFiles(selectedFileList, existingFiles, selectedFiles, portfolioId);
    updateSelectedPortfolioFileSummary(fileName, existingFiles, selectedFiles);
  });

  selectedFileList?.addEventListener("click", (event) => {
    const removeButton = event.target.closest("[data-remove-selected-file]");
    if (removeButton) {
      selectedFiles.splice(Number(removeButton.dataset.removeSelectedFile), 1);
      renderPortfolioEditorFiles(selectedFileList, existingFiles, selectedFiles, portfolioId);
      updateSelectedPortfolioFileSummary(fileName, existingFiles, selectedFiles);
      return;
    }

    bindPortfolioEditorExistingFileAction(event, portfolioId, async () => {
      existingFiles = await getPortfolioFiles(portfolioId);
      renderPortfolioEditorFiles(selectedFileList, existingFiles, selectedFiles, portfolioId);
      updateSelectedPortfolioFileSummary(fileName, existingFiles, selectedFiles);
    });
  });

  form.addEventListener("submit", async (event) => {
    event.preventDefault();

    const message = form.querySelector("[data-portfolio-message]");
    const formData = new FormData(form);

    try {
      if (message) message.textContent = "";
      const payload = {
        title: formData.get("postTitle"),
        description: formData.get("description"),
      };
      const portfolio = portfolioId
        ? await updatePortfolio(portfolioId, payload)
        : await createPortfolio(payload);
      if (selectedFiles.length) {
        const uploadedFiles = [];
        for (const file of selectedFiles) {
          uploadedFiles.push(await uploadPortfolioFile(portfolio.portfolioId, file));
        }
        if (message) {
          message.textContent = `${uploadedFiles.length}개 파일까지 업로드했습니다.`;
        }
      }
      form.reset();
      selectedFiles = [];
      renderPortfolioEditorFiles(selectedFileList, [], selectedFiles, portfolioId);
      updateSelectedPortfolioFileSummary(fileName, [], selectedFiles);
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

async function loadPortfolioEditForm({
  portfolioId,
  titleInput,
  descriptionInput,
  selectedFileList,
  fileName,
  submitButton,
}) {
  const message = document.querySelector("[data-portfolio-message]");

  try {
    if (message) message.textContent = "기존 포트폴리오를 불러오는 중입니다.";
    if (submitButton) submitButton.textContent = "수정하기";

    const [portfolio, files] = await Promise.all([
      getPortfolio(portfolioId),
      getPortfolioFiles(portfolioId),
    ]);

    if (titleInput) {
      titleInput.value = portfolio.title || "";
      titleInput.dispatchEvent(new Event("input", { bubbles: true }));
    }
    if (descriptionInput) {
      descriptionInput.value = portfolio.description || "";
      descriptionInput.dispatchEvent(new Event("input", { bubbles: true }));
    }

    renderPortfolioEditorFiles(selectedFileList, files, [], portfolioId);
    updateSelectedPortfolioFileSummary(fileName, files, []);
    if (message) message.textContent = "";
    return files;
  } catch (error) {
    if (message) message.textContent = error.message;
    return [];
  }
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

function renderPortfolioPreviewCards(portfolios, options = {}) {
  if (!portfolios.length) {
    return `<article class="portfolio-card"><span>EMPTY</span><h3>등록된 포트폴리오가 없습니다.</h3><p>포트폴리오 관리에서 첫 항목을 등록해 보세요.</p></article>`;
  }

  return portfolios
    .map((portfolio) => `
      <article class="portfolio-card is-clickable ${options.showEdit ? "has-edit" : ""} ${getPortfolioPreviewImage(portfolio) ? "has-media" : "text-only"}">
        ${options.showEdit ? `
          <button class="portfolio-edit-button" type="button" aria-label="${escapeHtml(portfolio.title)} 관리 메뉴" aria-expanded="false" data-portfolio-action-trigger="${escapeHtml(portfolio.portfolioId)}">
            <span aria-hidden="true">✎</span>
          </button>
          <div class="portfolio-action-menu" data-portfolio-action-menu="${escapeHtml(portfolio.portfolioId)}" hidden>
            <a href="#/portfolio-new?id=${escapeHtml(portfolio.portfolioId)}">수정</a>
            <button type="button" data-portfolio-delete="${escapeHtml(portfolio.portfolioId)}">삭제</button>
          </div>
        ` : ""}
        <button class="portfolio-card-open" type="button" data-portfolio-detail="${escapeHtml(portfolio.portfolioId)}">
          ${renderPortfolioPreviewMedia(portfolio)}
          <div>
            <h3>${escapeHtml(portfolio.title)}</h3>
            <p>${escapeHtml(markdownExcerpt(portfolio.description || ""))}</p>
          </div>
        </button>
      </article>
    `)
    .join("");
}

function bindPortfolioCardActions(root = document) {
  root.querySelectorAll("[data-portfolio-action-trigger]").forEach((trigger) => {
    trigger.addEventListener("click", () => {
      const menu = root.querySelector(`[data-portfolio-action-menu="${trigger.dataset.portfolioActionTrigger}"]`);
      if (!menu) return;

      const willOpen = menu.hidden;
      closePortfolioActionMenus(root);
      menu.hidden = !willOpen;
      trigger.setAttribute("aria-expanded", String(willOpen));
    });
  });

  root.querySelectorAll("[data-portfolio-delete]").forEach((button) => {
    button.addEventListener("click", async () => {
      button.disabled = true;
      try {
        await deletePortfolio(button.dataset.portfolioDelete);
        await loadPortfolioList();
      } catch (error) {
        alert(error.message);
        button.disabled = false;
      }
    });
  });
}

function closePortfolioActionMenus(root = document) {
  root.querySelectorAll("[data-portfolio-action-menu]").forEach((menu) => {
    menu.hidden = true;
  });
  root.querySelectorAll("[data-portfolio-action-trigger]").forEach((trigger) => {
    trigger.setAttribute("aria-expanded", "false");
  });
}

function cachePortfolios(portfolios) {
  portfolios.forEach((portfolio) => {
    portfolioCache.set(String(portfolio.portfolioId), portfolio);
  });
}

function bindPortfolioCardOpen(root = document) {
  root.querySelectorAll("[data-portfolio-detail]").forEach((card) => {
    card.addEventListener("click", () => {
      openPortfolioModal(card.dataset.portfolioDetail);
    });
  });
}

function openPortfolioModal(portfolioId) {
  const portfolio = portfolioCache.get(String(portfolioId));
  if (!portfolio) return;

  const modal = getPortfolioModal();
  const content = modal.querySelector("[data-portfolio-modal-content]");
  setSafeHtml(content, renderPortfolioModalContent(portfolio));
  modal.hidden = false;
  document.body.classList.add("modal-open");
  modal.querySelector("[data-portfolio-modal-close]")?.focus();
}

function closePortfolioModal() {
  const modal = document.querySelector("[data-portfolio-modal]");
  if (!modal) return;

  modal.hidden = true;
  document.body.classList.remove("modal-open");
}

function getPortfolioModal() {
  let modal = document.querySelector("[data-portfolio-modal]");
  if (modal) return modal;

  modal = document.createElement("div");
  modal.className = "modal-backdrop";
  modal.dataset.portfolioModal = "";
  modal.hidden = true;
  setSafeHtml(modal, `
    <div class="portfolio-detail-modal" role="dialog" aria-modal="true" aria-label="포트폴리오 상세">
      <div class="modal-head">
        <span class="kicker">Portfolio</span>
        <button class="modal-close" type="button" data-portfolio-modal-close aria-label="포트폴리오 상세 닫기">x</button>
      </div>
      <div data-portfolio-modal-content></div>
    </div>
  `);

  modal.addEventListener("click", (event) => {
    if (event.target === modal || event.target.closest("[data-portfolio-modal-close]")) {
      closePortfolioModal();
    }
  });

  document.addEventListener("keydown", (event) => {
    if (event.key === "Escape" && !modal.hidden) {
      closePortfolioModal();
    }
  });

  document.body.append(modal);
  return modal;
}

function renderPortfolioModalContent(portfolio) {
  return `
    <div class="markdown-preview portfolio-detail-markdown">
      ${renderMarkdown(portfolio.title, portfolio.description || "")}
    </div>
    ${renderPortfolioModalFiles(portfolio.files || [])}
  `;
}

function renderPortfolioModalFiles(files) {
  const attachmentFiles = files.filter((file) => !String(file.contentType || "").startsWith("image/"));
  if (!attachmentFiles.length) return "";

  return `
    <div class="portfolio-detail-files">
      <h3>첨부파일</h3>
      ${attachmentFiles.map((file) => `
        <a href="${escapeHtml(safeUrl(file.fileUrl))}" target="_blank" rel="noopener noreferrer">
          <span>${escapeHtml(file.originalFileName)}</span>
          <small>${formatFileSize(Number(file.fileSize || 0))}</small>
        </a>
      `).join("")}
    </div>
  `;
}

function renderPortfolioPreviewMedia(portfolio) {
  const image = getPortfolioPreviewImage(portfolio);
  if (!image) return "";

  return `
    <div class="portfolio-card-media">
      <img src="${escapeHtml(safeImageUrl(image.fileUrl))}" alt="" />
      ${image.thumbnail ? `<small>대표</small>` : ""}
    </div>
  `;
}

function getPortfolioPreviewImage(portfolio) {
  const imageFiles = (portfolio.files || []).filter((file) => String(file.contentType || "").startsWith("image/"));
  const fileImage = imageFiles.find((file) => file.thumbnail) || imageFiles[0] || null;
  if (fileImage) return fileImage;

  const markdownImageUrl = getFirstMarkdownImageUrl(portfolio.description);
  return markdownImageUrl
    ? {
      fileUrl: markdownImageUrl,
      thumbnail: false,
    }
    : null;
}

function renderPortfolioManageCard(portfolio) {
  return `
    <article class="summary-card portfolio-manage-card">
      <div>
        <span class="kicker">Portfolio</span>
        <h2>${escapeHtml(portfolio.title)}</h2>
        <p>${escapeHtml(markdownExcerpt(portfolio.description || ""))}</p>
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
      <a class="portfolio-file-link" href="${escapeHtml(safeUrl(file.fileUrl))}" target="_blank" rel="noopener noreferrer">
        ${isImage ? `<img src="${escapeHtml(safeImageUrl(file.fileUrl))}" alt="" />` : `<span>${fileIcon(file.contentType)}</span>`}
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

function getPortfolioEditId() {
  const query = window.location.hash.split("?")[1] || "";
  return new URLSearchParams(query).get("id");
}

function updateSelectedPortfolioFileSummary(element, existingFiles, selectedFiles) {
  if (!element) return;

  const totalLength = existingFiles.length + selectedFiles.length;
  element.textContent = totalLength
    ? `기존 ${existingFiles.length}개 · 추가 ${selectedFiles.length}개`
    : "선택사항 · 여러 이미지와 작업 자료를 함께 올릴 수 있습니다.";
}

async function bindPortfolioEditorExistingFileAction(event, portfolioId, refresh) {
  if (!portfolioId) return;

  const deleteButton = event.target.closest("[data-portfolio-file-delete]");
  const thumbnailButton = event.target.closest("[data-portfolio-file-thumbnail]");
  const updateInput = event.target.closest("[data-portfolio-file-update]");

  if (deleteButton) {
    deleteButton.disabled = true;
    try {
      await deletePortfolioFile(portfolioId, deleteButton.dataset.portfolioFileDelete);
      await refresh();
    } catch (error) {
      alert(error.message);
      deleteButton.disabled = false;
    }
    return;
  }

  if (thumbnailButton) {
    thumbnailButton.disabled = true;
    try {
      await setPortfolioThumbnail(portfolioId, thumbnailButton.dataset.portfolioFileThumbnail);
      await refresh();
    } catch (error) {
      alert(error.message);
      thumbnailButton.disabled = false;
    }
    return;
  }

  if (updateInput) {
    const file = updateInput.files[0];
    if (!file) return;
    try {
      await updatePortfolioFile(portfolioId, updateInput.dataset.portfolioFileUpdate, file);
      updateInput.value = "";
      await refresh();
    } catch (error) {
      alert(error.message);
      updateInput.value = "";
    }
  }
}

function renderPortfolioEditorFiles(container, existingFiles, selectedFiles, portfolioId = null) {
  if (!container) return;

  container.hidden = existingFiles.length + selectedFiles.length === 0;
  setSafeHtml(container, [
    ...existingFiles.map((file) => renderExistingPortfolioEditorFile(file, portfolioId)),
    ...selectedFiles.map((file, index) => renderSelectedPortfolioFile(file, index)),
  ].join(""));
}

function renderExistingPortfolioEditorFile(file, portfolioId) {
  const isImage = String(file.contentType || "").startsWith("image/");

  return `
    <div class="selected-file-item existing-file-item">
      <div>
        <strong>${escapeHtml(file.originalFileName)}</strong>
        <small>기존 파일 · ${escapeHtml(file.contentType || "file")} · ${formatFileSize(Number(file.fileSize || 0))}${file.thumbnail ? " · 대표" : ""}</small>
      </div>
      <div class="inline-file-actions">
        <a href="${escapeHtml(safeUrl(file.fileUrl))}" target="_blank" rel="noopener noreferrer">보기</a>
        ${portfolioId && isImage && !file.thumbnail ? `<button type="button" data-portfolio-file-thumbnail="${escapeHtml(file.portfolioFileId)}">대표</button>` : ""}
        ${portfolioId ? `
          <label>
            교체
            <input type="file" data-portfolio-file-update="${escapeHtml(file.portfolioFileId)}" />
          </label>
          <button type="button" data-portfolio-file-delete="${escapeHtml(file.portfolioFileId)}">삭제</button>
        ` : ""}
      </div>
    </div>
  `;
}

function renderSelectedPortfolioFile(file, index) {
  return `
    <div class="selected-file-item">
      <div>
        <strong>${escapeHtml(file.name)}</strong>
        <small>추가 예정 · ${escapeHtml(file.type || "file")} · ${formatFileSize(file.size)}</small>
      </div>
      <button type="button" aria-label="${escapeHtml(file.name)} 제거" data-remove-selected-file="${index}">x</button>
    </div>
  `;
}

function markdownImageText(fileName, url) {
  const alt = fileName.replace(/\.[^.]+$/, "") || "portfolio image";
  return `\n![${alt}](${url})\n`;
}

function getFirstMarkdownImageUrl(markdown) {
  const match = String(markdown || "").match(/!\[[^\]]*]\(([^)\s]+)(?:\s+"[^"]*")?\)/);
  if (!match) return null;
  return safeImageUrl(match[1]) || null;
}

function insertTextAtCursor(textarea, text) {
  const start = textarea.selectionStart;
  const end = textarea.selectionEnd;
  const before = textarea.value.slice(0, start);
  const after = textarea.value.slice(end);

  textarea.value = `${before}${text}${after}`;
  const cursor = start + text.length;
  textarea.setSelectionRange(cursor, cursor);
  textarea.dispatchEvent(new Event("input", { bubbles: true }));
}

function renderMarkdown(title, markdown) {
  const blocks = [];
  const safeTitle = escapeHtml(title).trim();

  if (safeTitle) {
    blocks.push(`<h1>${safeTitle}</h1>`);
  }

  const lines = markdown.split(/\n/);
  let paragraph = [];

  const flushParagraph = () => {
    if (!paragraph.length) return;
    blocks.push(`<p>${renderInlineMarkdown(paragraph.join(" "))}</p>`);
    paragraph = [];
  };

  lines.forEach((line) => {
    const trimmed = line.trim();

    if (!trimmed) {
      flushParagraph();
      return;
    }

    const image = trimmed.match(/^!\[(.*?)]\((.*?)\)$/);
    if (image) {
      flushParagraph();
      const imageUrl = safeImageUrl(image[2]);
      if (imageUrl) {
        blocks.push(`<img src="${escapeHtml(imageUrl)}" alt="${escapeHtml(image[1])}" />`);
      }
      return;
    }

    const heading = trimmed.match(/^(#{1,3})\s+(.+)$/);
    if (heading) {
      flushParagraph();
      blocks.push(`<h${heading[1].length + 1}>${renderInlineMarkdown(heading[2])}</h${heading[1].length + 1}>`);
      return;
    }

    paragraph.push(trimmed);
  });

  flushParagraph();

  return blocks.length ? blocks.join("") : `<p>작성한 내용이 여기에 미리보기로 표시됩니다.</p>`;
}

function renderInlineMarkdown(value) {
  return escapeHtml(value)
    .replace(/`([^`]+)`/g, "<code>$1</code>")
    .replace(/\*\*([^*]+)\*\*/g, "<strong>$1</strong>");
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

if (!isHandlingOAuthSuccess && !window.location.hash) {
  window.location.hash = "/home";
}

if (!isHandlingOAuthSuccess) {
  window.addEventListener("hashchange", render);
  render();
}

function handleOAuthSuccess() {
  if (window.location.pathname !== "/oauth2/success") return false;

  refreshAfterOAuth()
    .catch(() => {
      // refreshToken만 먼저 저장된 상황이어도 홈 진입 후 기존 인증 복구 로직이 다시 처리한다.
    })
    .finally(() => {
      window.location.replace("/index.html#/home");
    });

  return true;
}

async function refreshAfterOAuth() {
  const { apiRequest } = await import("./api/api.js");
  await apiRequest("/auth/refresh", {
    method: "POST",
    skipAuthRefresh: true,
  });
}
