import { LoginPage } from "./features/auth/LoginPage.js";
import { ChatPage } from "./features/chat/ChatPage.js";
import { HomePage } from "./features/home/HomePage.js";
import { CheckoutPage } from "./features/payment/CheckoutPage.js";
import { RequestCreatePage } from "./features/request/RequestCreatePage.js";
import { RequestDetailPage } from "./features/request/RequestDetailPage.js";
import { RequestListPage } from "./features/request/RequestListPage.js";
import { AiSearchPage } from "./features/search/AiSearchPage.js";
import { TalentCreatePage } from "./features/talent/TalentCreatePage.js";
import { TalentDetailPage } from "./features/talent/TalentDetailPage.js";
import { TalentListPage } from "./features/talent/TalentListPage.js";
import { MyPage } from "./features/user/MyPage.js";

const routes = {
  home: HomePage,
  login: LoginPage,
  talents: TalentListPage,
  "talent-new": TalentCreatePage,
  requests: RequestListPage,
  "request-new": RequestCreatePage,
  "ai-search": AiSearchPage,
  chat: ChatPage,
  mypage: MyPage,
  checkout: CheckoutPage,
};

export function parseRoute() {
  const hash = window.location.hash.replace(/^#\/?/, "");
  const [path] = hash.split("?");
  const segments = path.split("/").filter(Boolean);
  return segments.length ? segments : ["home"];
}

export function resolvePage(segments) {
  const [route, id] = segments;

  if (route === "talent" && id) {
    return { route: "talents", content: TalentDetailPage(id) };
  }

  if (route === "request" && id) {
    return { route: "requests", content: RequestDetailPage(id) };
  }

  const page = routes[route] || routes.home;
  return { route: routes[route] ? route : "home", content: page() };
}
