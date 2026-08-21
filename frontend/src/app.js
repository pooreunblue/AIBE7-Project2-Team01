import { shell } from "./components.js";
import {
  aiSearchPage,
  chatPage,
  checkoutPage,
  homePage,
  loginPage,
  myPage,
  requestDetailPage,
  requestNewPage,
  requestsPage,
  talentDetailPage,
  talentNewPage,
  talentsPage,
} from "./pages.js";

const app = document.querySelector("#app");

const routes = {
  home: homePage,
  login: loginPage,
  talents: talentsPage,
  "talent-new": talentNewPage,
  requests: requestsPage,
  "request-new": requestNewPage,
  "ai-search": aiSearchPage,
  chat: chatPage,
  mypage: myPage,
  checkout: checkoutPage,
};

function parseRoute() {
  const hash = window.location.hash.replace(/^#\/?/, "");
  const [path] = hash.split("?");
  const segments = path.split("/").filter(Boolean);
  return segments.length ? segments : ["home"];
}

function resolvePage(segments) {
  const [route, id] = segments;

  if (route === "talent" && id) {
    return { route: "talents", content: talentDetailPage(id) };
  }

  if (route === "request" && id) {
    return { route: "requests", content: requestDetailPage(id) };
  }

  const page = routes[route] || routes.home;
  return { route: routes[route] ? route : "home", content: page() };
}

function render() {
  const { route, content } = resolvePage(parseRoute());
  app.innerHTML = shell(content, route);
  bindPageEvents();
  window.scrollTo({ top: 0, behavior: "instant" });
}

function bindPageEvents() {
  document.querySelectorAll("form").forEach((form) => {
    form.addEventListener("submit", (event) => {
      event.preventDefault();
      if (form.matches("[data-search-form]")) {
        window.location.hash = "/ai-search";
      }
    });
  });

  const messageForm = document.querySelector(".message-compose");
  if (messageForm) {
    messageForm.addEventListener("submit", (event) => {
      event.preventDefault();
      const input = messageForm.querySelector("input");
      const value = input.value.trim();
      if (!value) return;
      const bubble = document.createElement("p");
      bubble.className = "bubble me";
      bubble.textContent = value;
      document.querySelector(".message-stream").append(bubble);
      input.value = "";
    });
  }
}

if (!window.location.hash) {
  window.location.hash = "/home";
}

window.addEventListener("hashchange", render);
render();
