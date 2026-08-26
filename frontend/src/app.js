import { shell } from "./shared/ui/index.js";
import { parseRoute, resolvePage } from "./router.js";
import { login } from "./features/auth/authApi.js";
import { initChatPage, teardownChatPage } from "./features/chat/ChatPage.js";

const app = document.querySelector("#app");

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
