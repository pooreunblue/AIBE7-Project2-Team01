import { LoginPage } from "./features/auth/LoginPage.js";
import { SignupPage } from "./features/auth/SignupPage.js";
import { ChatPage } from "./features/chat/ChatPage.js";
import { ErrorPage, NotFoundPage } from "./features/error/ErrorPage.js";
import { HomePage } from "./features/home/HomePage.js";
import { CheckoutPage } from "./features/payment/CheckoutPage.js";
import { PortfolioCreatePage } from "./features/portfolio/PortfolioCreatePage.js";
import { PortfolioPage } from "./features/portfolio/PortfolioPage.js";
import { RequestCreatePage } from "./features/request/RequestCreatePage.js";
import { RequestDetailPage } from "./features/request/RequestDetailPage.js";
import { RequestListPage } from "./features/request/RequestListPage.js";
import { AiSearchPage } from "./features/search/AiSearchPage.js";
import { TalentCreatePage } from "./features/talent/TalentCreatePage.js";
import { TalentDetailPage } from "./features/talent/TalentDetailPage.js";
import { TalentListPage } from "./features/talent/TalentListPage.js";
import { MyTradesPage } from "./features/trade/MyTradesPage.js";
import { MyPage } from "./features/user/MyPage.js";
import { UserProfilePage } from "./features/user/UserProfilePage.js";

const routes = {
  home: HomePage,
  login: LoginPage,
  signup: SignupPage,
  talents: TalentListPage,
  "talent-new": TalentCreatePage,
  requests: RequestListPage,
  "request-new": RequestCreatePage,
  "ai-search": AiSearchPage,
  chat: ChatPage,
  mypage: MyPage,
  "my-trades": MyTradesPage,
  portfolios: PortfolioPage,
  "portfolio-new": PortfolioCreatePage,
  checkout: CheckoutPage,
  "not-found": NotFoundPage,
  error: () => ErrorPage(500),
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

  if (route === "chat" && id) {
    return { route: "chat", content: ChatPage(id) };
  }

  if (route === "users" && id) {
    return { route: "user-profile", content: UserProfilePage(id) };
  }

  const page = routes[route];
  if (!page) {
    return { route: "not-found", content: NotFoundPage() };
  }
  return { route, content: page() };
}
