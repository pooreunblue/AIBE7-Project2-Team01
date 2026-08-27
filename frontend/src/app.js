import { formatMoney, shell } from "./shared/ui/index.js";
import { uploadTempImage } from "./api/uploadApi.js";
import { parseRoute, resolvePage } from "./router.js";
import { login, logout, signup } from "./features/auth/authApi.js";
import { setAccessToken, setRefreshToken } from "./auth/tokenStorage.js";
import { getCurrentUserId } from "./auth/currentUser.js";
import { fetchCategories } from "./features/category/categoryApi.js";
import { initChatPage, teardownChatPage } from "./features/chat/ChatPage.js";
import { startChat } from "./features/chat/startChat.js";
import { initCheckoutPage } from "./features/payment/CheckoutPage.js";
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
import { createRequest, fetchRequest, fetchRequests } from "./features/request/requestApi.js";
import {
  createTalent,
  deleteTalent,
  deleteTalentFile,
  fetchTalent,
  fetchTalents,
  getTalentFiles,
  inactiveTalent,
  setTalentThumbnail,
  updateTalent,
  updateTalentFile,
  uploadTalentFile,
} from "./features/talent/talentApi.js";
import { getMyPage } from "./features/user/userApi.js";
import { chargeWallet } from "./features/wallet/walletApi.js";

const app = document.querySelector("#app");
let accountMenuOutsideHandler = null;
const portfolioCache = new Map();

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
  bindTalentListPage();
  bindTalentDetailPage();
  bindTalentCreatePage();
  bindRequestListPage();
  bindRequestDetailPage();
  bindRequestCreatePage();
  initChatPage();
  initCheckoutPage();

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
    preview.innerHTML = renderMarkdown(titleInput.value, markdownInput.value);
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

function bindTalentListPage() {
  const list = document.querySelector("[data-list='talents']");
  if (!list) return;

  const searchForm = document.querySelector("[data-list-search]");
  loadTalentList();

  searchForm?.addEventListener("submit", (event) => {
    event.preventDefault();
    const keyword = new FormData(searchForm).get("keyword") || searchForm.querySelector("input")?.value || "";
    loadTalentList(String(keyword));
  });
}

function bindTalentDetailPage() {
  const detail = document.querySelector("[data-talent-detail]");
  if (!detail) return;

  loadTalentDetail(detail.dataset.talentDetail);
}

function bindTalentCreatePage() {
  const form = document.querySelector("[data-talent-form]");
  if (!form) return;

  const fileInput = form.querySelector('input[name="talentFiles"]');
  const fileName = form.querySelector("[data-talent-file-name]");
  const selectedFileList = form.querySelector("[data-selected-talent-files]");
  let selectedFiles = [];
  let existingFiles = [];

  bindMarkdownImageUpload();
  bindPortfolioMarkdownPreview();
  bindTalentSettingsModal(form);

  fileInput?.addEventListener("change", () => {
    selectedFiles = [...selectedFiles, ...Array.from(fileInput.files || [])];
    fileInput.value = "";
    renderTalentEditorFiles(selectedFileList, existingFiles, selectedFiles, getTalentEditId());
    updateSelectedTalentFileSummary(fileName, existingFiles, selectedFiles);
  });

  selectedFileList?.addEventListener("click", (event) => {
    const removeButton = event.target.closest("[data-remove-selected-talent-file]");
    if (removeButton) {
      selectedFiles.splice(Number(removeButton.dataset.removeSelectedTalentFile), 1);
      renderTalentEditorFiles(selectedFileList, existingFiles, selectedFiles, getTalentEditId());
      updateSelectedTalentFileSummary(fileName, existingFiles, selectedFiles);
      return;
    }

    bindTalentEditorExistingFileAction(event, async () => {
      existingFiles = await getTalentFiles(getTalentEditId());
      renderTalentEditorFiles(selectedFileList, existingFiles, selectedFiles, getTalentEditId());
      updateSelectedTalentFileSummary(fileName, existingFiles, selectedFiles);
    });
  });

  const talentPostId = getTalentEditId();
  loadTalentSettingsOptions(form).then(() => {
    if (talentPostId) {
      loadTalentEditForm(form, talentPostId).then((files) => {
        existingFiles = files;
        renderTalentEditorFiles(selectedFileList, existingFiles, selectedFiles, talentPostId);
        updateSelectedTalentFileSummary(fileName, existingFiles, selectedFiles);
      });
    } else {
      requestAnimationFrame(() => openTalentSettingsModal(form));
      renderTalentEditorFiles(selectedFileList, existingFiles, selectedFiles, null);
      updateSelectedTalentFileSummary(fileName, existingFiles, selectedFiles);
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
      if (selectedFiles.length) {
        for (const file of selectedFiles) {
          await uploadTalentFile(talent.talentPostId, file);
        }
      }
      window.location.hash = `/talent/${talent.talentPostId}`;
    } catch (error) {
      if (message) message.textContent = error.message;
    }
  });
}

async function loadTalentList(keyword = "") {
  const list = document.querySelector("[data-list='talents']");
  if (!list) return;

  try {
    const talents = await fetchTalents(keyword);
    list.innerHTML = talents.length
      ? talents.map(renderTalentCard).join("")
      : `<article class="talent-card"><div class="card-body"><span class="kicker">EMPTY</span><h3>등록된 재능글이 없습니다.</h3><p>첫 재능글을 작성해 보세요.</p></div></article>`;
  } catch (error) {
    list.innerHTML = `<article class="talent-card"><div class="card-body"><span class="kicker">ERROR</span><h3>재능글을 불러오지 못했습니다.</h3><p>${escapeHtml(error.message)}</p></div></article>`;
  }
}

async function loadTalentDetail(talentPostId) {
  try {
    const [talent, files] = await Promise.all([
      fetchTalent(talentPostId),
      getTalentFiles(talentPostId),
    ]);
    renderTalentDetail(talent, files);
    bindTalentDetailActions(talent);
    loadLinkedPortfolio(talent);
  } catch (error) {
    setText("[data-talent-category]", "ERROR");
    setText("[data-talent-meta]", "재능글을 불러오지 못했습니다.");
    setText("[data-talent-title]", error.message);
    const content = document.querySelector("[data-talent-content]");
    if (content) content.innerHTML = "";
  }
}

async function loadLinkedPortfolio(talent) {
  const target = document.querySelector("[data-talent-linked-portfolio]");
  if (!target) return;

  if (!talent.portfolioId) {
    target.innerHTML = "";
    return;
  }

  try {
    const [portfolio, files] = await Promise.all([
      getPortfolio(talent.portfolioId),
      getPortfolioFiles(talent.portfolioId),
    ]);
    target.innerHTML = renderLinkedPortfolio(portfolio, files);
  } catch (error) {
    target.innerHTML = `<section class="linked-portfolio-section"><span class="kicker">Portfolio</span><p>연결된 포트폴리오를 불러오지 못했습니다: ${escapeHtml(error.message)}</p></section>`;
  }
}

function renderTalentCard(talent) {
  return `
    <article class="talent-card">
      <a class="visual ${talentVisualClass(talent)}" href="#/talent/${talent.talentPostId}" aria-label="${escapeHtml(talent.title)} 상세">
        <span>${escapeHtml(talent.categoryName || "Talent")}</span>
      </a>
      <div class="card-body">
        <div class="meta-line">
          <span>작성자 #${escapeHtml(talent.userId)}</span>
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
  setText("[data-talent-author]", `작성자 #${talent.userId}`);
  setText("[data-talent-status]", `${talent.status || "-"} · ${talent.portfolioId ? `포트폴리오 #${talent.portfolioId}` : "포트폴리오 미연결"}`);
  setText("[data-talent-price]", formatOptionalMoney(talent.price));
  setText("[data-talent-duration]", `예상 작업기간 ${formatDuration(talent)}`);

  const avatar = document.querySelector("[data-talent-avatar]");
  if (avatar) avatar.textContent = String(talent.userId || "?").charAt(0);

  const content = document.querySelector("[data-talent-content]");
  if (content) content.innerHTML = renderMarkdown("", talent.content || "");

  const hero = document.querySelector(".detail-hero");
  const cover = getTalentPreviewImage(files);
  if (hero && cover) {
    hero.classList.add("has-image");
    hero.innerHTML = `<img src="${escapeHtml(cover.fileUrl)}" alt="" /><span>${escapeHtml(talent.categoryName || "Talent")}</span>`;
  }

  const fileTarget = document.querySelector("[data-talent-files]");
  if (fileTarget) fileTarget.innerHTML = renderTalentDetailFiles(files);
}

function bindTalentDetailActions(talent) {
  const actions = document.querySelector("[data-talent-actions]");
  const chatButton = document.querySelector("[data-talent-chat]");
  if (!actions || !chatButton) return;

  const currentUserId = getCurrentUserId();
  const isOwner = currentUserId != null && Number(talent.userId) === Number(currentUserId);

  if (isOwner) {
    chatButton.remove();
    actions.insertAdjacentHTML("beforeend", `
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

async function loadTalentSettingsOptions(form) {
  const categorySelect = form.querySelector("[data-talent-category-select]");
  const portfolioSelect = form.querySelector("[data-talent-portfolio-select]");

  const renderSelected = () => renderTalentSettingsSummary(form);

  try {
    const categories = await fetchCategories();
    if (categorySelect) {
      categorySelect.innerHTML = categories.length
        ? `<option value="">카테고리 선택</option>${categories.map((category) => `<option value="${category.categoryId}">${escapeHtml(category.name)}</option>`).join("")}`
        : `<option value="">등록된 카테고리가 없습니다</option>`;
      categorySelect.addEventListener("change", renderSelected);
    }
  } catch {
    if (categorySelect) categorySelect.innerHTML = `<option value="">카테고리를 불러오지 못했습니다</option>`;
  }

  try {
    const portfolios = await getMyPortfolios();
    if (portfolioSelect) {
      portfolioSelect.innerHTML = portfolios.length
        ? `<option value="">포트폴리오 선택</option>${portfolios.map((portfolio) => `<option value="${portfolio.portfolioId}">${escapeHtml(portfolio.title)}</option>`).join("")}`
        : `<option value="">등록된 포트폴리오가 없습니다</option>`;
      portfolioSelect.addEventListener("change", renderSelected);
    }
  } catch {
    if (portfolioSelect) portfolioSelect.innerHTML = `<option value="">포트폴리오를 불러오지 못했습니다</option>`;
  }

  form.querySelector('input[name="price"]')?.addEventListener("input", renderSelected);
  form.querySelector('input[name="estimatedDuration"]')?.addEventListener("input", renderSelected);
  form.querySelector('select[name="durationUnit"]')?.addEventListener("change", renderSelected);
  renderTalentSettingsSummary(form);
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

    const titleInput = form.querySelector('input[name="title"]');
    const contentInput = form.querySelector('textarea[name="content"]');
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
    renderTalentSettingsSummary(form);
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

function buildTalentPayload(form) {
  if (!validateTalentSettings(form)) return null;
  const formData = new FormData(form);
  return {
    title: formData.get("title"),
    content: formData.get("content"),
    categoryId: Number(formData.get("categoryId")),
    price: optionalNumber(formData.get("price")),
    estimatedDuration: optionalNumber(formData.get("estimatedDuration")),
    durationUnit: formData.get("estimatedDuration") ? (formData.get("durationUnit") || "DAY") : null,
    portfolioId: optionalNumber(formData.get("portfolioId")),
  };
}

function validateTalentSettings(form) {
  const formData = new FormData(form);
  const categoryId = Number(formData.get("categoryId"));
  const rawPrice = formData.get("price");
  const rawDuration = formData.get("estimatedDuration");
  return Boolean(
    Number.isFinite(categoryId) && categoryId > 0 &&
    (!rawPrice || Number(rawPrice) >= 0) &&
    (!rawDuration || Number(rawDuration) > 0)
  );
}

function renderTalentSettingsSummary(form) {
  const summary = form.querySelector("[data-talent-detail-summary]");
  if (!summary) return;

  const formData = new FormData(form);
  const category = selectedOptionText(form.querySelector("[data-talent-category-select]"));
  const portfolio = selectedOptionText(form.querySelector("[data-talent-portfolio-select]"));
  const price = optionalNumber(formData.get("price"));
  const duration = optionalNumber(formData.get("estimatedDuration"));
  const durationUnit = durationUnitLabel(formData.get("durationUnit"));

  if (!validateTalentSettings(form)) {
    summary.innerHTML = `<span>상세 설정을 입력해 주세요.</span>`;
    return;
  }

  summary.innerHTML = `
    <span>${escapeHtml(category)}</span>
    <strong>${formatOptionalMoney(price)}</strong>
    <span>${duration ? `예상 ${duration}${durationUnit}` : "기간 협의"}</span>
    <span>${portfolio ? escapeHtml(portfolio) : "포트폴리오 미연결"}</span>
  `;
}

function optionalNumber(value) {
  if (value == null || value === "") return null;
  const number = Number(value);
  return Number.isFinite(number) ? number : null;
}

async function bindTalentEditorExistingFileAction(event, refresh) {
  const deleteButton = event.target.closest("[data-talent-file-delete]");
  const thumbnailButton = event.target.closest("[data-talent-file-thumbnail]");
  const updateInput = event.target.closest("[data-talent-file-update]");

  if (deleteButton) {
    await deleteTalentFile(deleteButton.dataset.talentId, deleteButton.dataset.talentFileDelete);
    await refresh();
    return;
  }

  if (thumbnailButton) {
    await setTalentThumbnail(thumbnailButton.dataset.talentId, thumbnailButton.dataset.talentFileThumbnail);
    await refresh();
    return;
  }

  if (updateInput) {
    const file = updateInput.files[0];
    if (!file) return;
    await updateTalentFile(updateInput.dataset.talentId, updateInput.dataset.talentFileUpdate, file);
    updateInput.value = "";
    await refresh();
  }
}

function renderTalentEditorFiles(container, existingFiles, selectedFiles, talentPostId) {
  if (!container) return;

  container.hidden = existingFiles.length + selectedFiles.length === 0;
  container.innerHTML = [
    ...existingFiles.map((file) => renderExistingTalentEditorFile(file, talentPostId)),
    ...selectedFiles.map((file, index) => renderSelectedTalentFile(file, index)),
  ].join("");
}

function renderExistingTalentEditorFile(file, talentPostId) {
  const isImage = String(file.contentType || "").startsWith("image/");
  return `
    <div class="selected-file-item existing-file-item">
      <div>
        <strong>${escapeHtml(file.originalFileName)}</strong>
        <small>기존 파일 · ${escapeHtml(file.contentType || "file")} · ${formatFileSize(Number(file.fileSize || 0))}${file.thumbnail ? " · 대표" : ""}</small>
      </div>
      <div class="inline-file-actions">
        ${isImage && !file.thumbnail ? `<button type="button" data-talent-id="${escapeHtml(talentPostId)}" data-talent-file-thumbnail="${escapeHtml(file.talentPostFileId)}">대표</button>` : ""}
        <label>
          교체
          <input type="file" data-talent-id="${escapeHtml(talentPostId)}" data-talent-file-update="${escapeHtml(file.talentPostFileId)}" />
        </label>
        <button type="button" data-talent-id="${escapeHtml(talentPostId)}" data-talent-file-delete="${escapeHtml(file.talentPostFileId)}">삭제</button>
      </div>
    </div>
  `;
}

function renderSelectedTalentFile(file, index) {
  return `
    <div class="selected-file-item">
      <div>
        <strong>${escapeHtml(file.name)}</strong>
        <small>추가 예정 · ${escapeHtml(file.type || "file")} · ${formatFileSize(file.size)}</small>
      </div>
      <button type="button" aria-label="${escapeHtml(file.name)} 제거" data-remove-selected-talent-file="${index}">x</button>
    </div>
  `;
}

function updateSelectedTalentFileSummary(element, existingFiles, selectedFiles) {
  if (!element) return;

  const totalLength = existingFiles.length + selectedFiles.length;
  element.textContent = totalLength
    ? `기존 ${existingFiles.length}개 · 추가 ${selectedFiles.length}개`
    : "선택사항 · 대표 이미지와 참고 자료를 함께 올릴 수 있습니다.";
}

function renderTalentDetailFiles(files) {
  if (!files.length) return "";

  return `
    <section class="talent-file-section">
      <h2>재능 자료</h2>
      <div class="talent-file-grid">
        ${files.map(renderTalentDetailFile).join("")}
      </div>
    </section>
  `;
}

function renderTalentDetailFile(file) {
  const isImage = String(file.contentType || "").startsWith("image/");
  return `
    <a class="talent-file-card ${isImage ? "is-image" : ""}" href="${escapeHtml(file.fileUrl)}" target="_blank" rel="noreferrer">
      ${isImage ? `<img src="${escapeHtml(file.fileUrl)}" alt="" />` : `<span>${fileIcon(file.contentType)}</span>`}
      <div>
        <strong>${escapeHtml(file.originalFileName)}</strong>
        <small>${formatFileSize(Number(file.fileSize || 0))}${file.thumbnail ? " · 대표" : ""}</small>
      </div>
    </a>
  `;
}

function renderLinkedPortfolio(portfolio, files) {
  const image = getPortfolioPreviewImage({ files });
  return `
    <section class="linked-portfolio-section">
      <div class="linked-portfolio-head">
        <span class="kicker">Linked Portfolio</span>
        <a class="button quiet" href="#/portfolios">내 포트폴리오 보기</a>
      </div>
      <article class="linked-portfolio-card">
        ${image ? `<img src="${escapeHtml(image.fileUrl)}" alt="" />` : `<div class="linked-portfolio-empty">Portfolio</div>`}
        <div>
          <h2>${escapeHtml(portfolio.title)}</h2>
          <p>${escapeHtml(markdownExcerpt(portfolio.description || ""))}</p>
          <small>첨부 ${files.length}개 · 업데이트 ${formatDate(portfolio.updatedAt)}</small>
        </div>
      </article>
    </section>
  `;
}

function getTalentPreviewImage(files) {
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

function markdownExcerpt(markdown) {
  return String(markdown || "")
    .replace(/!\[[^\]]*]\([^)]*\)/g, "")
    .replace(/[#*_`>~-]/g, "")
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

function talentVisualClass(talent) {
  const category = String(talent.categoryName || "").toLowerCase();
  if (category.includes("개발") || category.includes("dev")) return "development";
  if (category.includes("글") || category.includes("write")) return "writing";
  if (category.includes("마케팅") || category.includes("market")) return "marketing";
  return "";
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
    cachePortfolios(portfoliosWithFiles);
    preview.innerHTML = renderPortfolioPreviewCards(portfoliosWithFiles);
    bindPortfolioCardOpen(preview);
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
    cachePortfolios(portfoliosWithFiles);
    list.innerHTML = portfolios.length
      ? renderPortfolioPreviewCards(portfoliosWithFiles, { showEdit: true })
      : `<article class="portfolio-card text-only"><h3>등록된 포트폴리오가 없습니다.</h3><p>마이페이지에서 포트폴리오 정보를 확인할 수 있습니다.</p></article>`;
    bindPortfolioCardOpen(list);
    bindPortfolioCardActions(list);
  } catch (error) {
    list.innerHTML = `<article class="portfolio-card text-only"><h3>포트폴리오를 불러오지 못했습니다.</h3><p>${escapeHtml(error.message)}</p></article>`;
  }
}

function bindPortfolioForm() {
  const form = document.querySelector("[data-portfolio-form]");
  if (!form) return;

  const portfolioId = getPortfolioEditId();
  const titleInput = form.querySelector('input[name="title"]');
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
    renderPortfolioEditorFiles(selectedFileList, existingFiles, selectedFiles);
    updateSelectedPortfolioFileSummary(fileName, existingFiles, selectedFiles);
  });

  selectedFileList?.addEventListener("click", (event) => {
    const button = event.target.closest("[data-remove-selected-file]");
    if (!button) return;

    selectedFiles.splice(Number(button.dataset.removeSelectedFile), 1);
    renderPortfolioEditorFiles(selectedFileList, existingFiles, selectedFiles);
    updateSelectedPortfolioFileSummary(fileName, existingFiles, selectedFiles);
  });

  form.addEventListener("submit", async (event) => {
    event.preventDefault();

    const message = form.querySelector("[data-portfolio-message]");
    const formData = new FormData(form);

    try {
      if (message) message.textContent = "";
      const payload = {
        title: formData.get("title"),
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
      renderPortfolioEditorFiles(selectedFileList, [], selectedFiles);
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

    renderPortfolioEditorFiles(selectedFileList, files, []);
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
            <p>${escapeHtml(portfolio.description)}</p>
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
  content.innerHTML = renderPortfolioModalContent(portfolio);
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
  modal.innerHTML = `
    <div class="portfolio-detail-modal" role="dialog" aria-modal="true" aria-label="포트폴리오 상세">
      <div class="modal-head">
        <span class="kicker">Portfolio</span>
        <button class="modal-close" type="button" data-portfolio-modal-close aria-label="포트폴리오 상세 닫기">x</button>
      </div>
      <div data-portfolio-modal-content></div>
    </div>
  `;

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
  const image = getPortfolioPreviewImage(portfolio);

  return `
    ${image ? `<img class="portfolio-detail-cover" src="${escapeHtml(image.fileUrl)}" alt="" />` : ""}
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
        <a href="${escapeHtml(file.fileUrl)}" target="_blank" rel="noreferrer">
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

function renderPortfolioEditorFiles(container, existingFiles, selectedFiles) {
  if (!container) return;

  container.hidden = existingFiles.length + selectedFiles.length === 0;
  container.innerHTML = [
    ...existingFiles.map(renderExistingPortfolioEditorFile),
    ...selectedFiles.map((file, index) => renderSelectedPortfolioFile(file, index)),
  ].join("");
}

function renderExistingPortfolioEditorFile(file) {
  return `
    <a class="selected-file-item existing-file-item" href="${escapeHtml(file.fileUrl)}" target="_blank" rel="noreferrer">
      <div>
        <strong>${escapeHtml(file.originalFileName)}</strong>
        <small>기존 파일 · ${escapeHtml(file.contentType || "file")} · ${formatFileSize(Number(file.fileSize || 0))}</small>
      </div>
    </a>
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
      blocks.push(`<img src="${escapeHtml(image[2])}" alt="${escapeHtml(image[1])}" />`);
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
